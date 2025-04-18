package com.tripleyuan.winter.web;

import com.tripleyuan.winter.annotation.*;
import com.tripleyuan.winter.context.ApplicationContext;
import com.tripleyuan.winter.context.BeanDefinition;
import com.tripleyuan.winter.context.ConfigurableApplicationContext;
import com.tripleyuan.winter.exception.ErrorResponseException;
import com.tripleyuan.winter.exception.NestedRuntimeException;
import com.tripleyuan.winter.exception.ServerErrorException;
import com.tripleyuan.winter.io.PropertyResolver;
import com.tripleyuan.winter.utils.JsonUtils;
import com.tripleyuan.winter.utils.PathUtils;
import com.tripleyuan.winter.utils.StreamUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tripleyuan.winter.utils.ClassUtils.getAnnotation;
import static com.tripleyuan.winter.utils.WebUtils.DEFAULT_PARAM_VALUE;

@Slf4j
public class DispatcherServlet extends HttpServlet {

    private ApplicationContext applicationContext;
    private ViewResolver viewResolver;

    // the path of static resource
    private String resourcePath;
    // site favicon
    private String faviconPath;

    private List<Dispatcher> getDispatchers = new ArrayList<>();
    private List<Dispatcher> postDispatchers = new ArrayList<>();

    public DispatcherServlet(ApplicationContext applicationContext, PropertyResolver propertyResolver) {
        this.applicationContext = applicationContext;
        // todo
        // this.viewResolver = applicationContext.getBean(ViewResolver.class);
        this.resourcePath = propertyResolver.getProperty("${winter.web.static-path:/static}");
        this.faviconPath = propertyResolver.getProperty("${winter.web.favicon-path:/favicon.ico}");
        if (!this.resourcePath.endsWith("/")) {
            this.resourcePath = this.resourcePath + "/";
        }
    }

    @Override
    public void init() throws ServletException {
        log.info("Init {}", getClass().getName());
        // scan @Controller and @RestController
        ConfigurableApplicationContext cac = (ConfigurableApplicationContext) applicationContext;
        for (BeanDefinition def : cac.findBeanDefinitions(Object.class)) {
            Class<?> beanClass = def.getBeanClass();
            Controller controller = beanClass.getAnnotation(Controller.class);
            RestController restController = beanClass.getAnnotation(RestController.class);
            if (controller != null && restController != null) {
                throw new ServletException("@Controller and @RestController cannot be used together on class " + beanClass.getName());
            }

            Object bean = def.getInstance();
            if (controller != null) {
                addController(false, def.getName(), bean);
            }
            if (restController != null) {
                addController(true, def.getName(), bean);
            }
        }
    }

    @Override
    public void destroy() {
        this.applicationContext.close();
    }

    private void addController(boolean isRest, String beanName, Object bean) throws ServletException {
        log.info("Add {} Controller '{}'", isRest ? "Rest" : "MVC", bean.getClass().getName());
        addMethods(isRest, beanName, bean, bean.getClass());
    }

    private void addMethods(boolean isRest, String beanName, Object bean, Class<?> clazz) throws ServletException {
        for (Method method : clazz.getDeclaredMethods()) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping != null) {
                checkMethod(method);
                this.getDispatchers.add(new Dispatcher(isRest, bean, method, getMapping.value()));
            }

            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (postMapping != null) {
                checkMethod(method);
                this.postDispatchers.add(new Dispatcher(isRest, bean, method, postMapping.value()));
            }
        }

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            addMethods(isRest, beanName, bean, superclass);
        }
    }

    private void checkMethod(Method method) throws ServletException {
        int mod = method.getModifiers();
        if (Modifier.isStatic(mod)) {
            throw new ServletException("Cannot do URL mapping to static method: " + method);
        }
        method.setAccessible(true);
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.equals(faviconPath) || uri.startsWith(resourcePath)) {
            doResource(uri, req, resp);
        } else {
            doService(req, resp, this.getDispatchers);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doService(req, resp, this.postDispatchers);
    }

    private void doResource(String uri, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletContext servletContext = req.getServletContext();
        try (InputStream in = servletContext.getResourceAsStream(uri)) {
            if (in == null) {
                resp.sendError(404, "Not Found");
                return;
            }
            // guess content type
            String file = uri;
            int i = file.lastIndexOf("/");
            if (i > -1) {
                file = file.substring(i + 1);
            }
            String mimeType = servletContext.getMimeType(file);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            resp.setContentType(mimeType);
            StreamUtils.copy(in, resp.getOutputStream());
        }
    }

    private void doService(HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers)
            throws ServletException, IOException {
        String url = req.getRequestURI();
        try {
            doService(url, req, resp, dispatchers);
        } catch (ErrorResponseException e) {
            log.warn("process request failed with status {} : {}", e.statusCode, url, e);
            if (!resp.isCommitted()) {
                resp.resetBuffer();
                resp.sendError(e.statusCode);
            }
        } catch (RuntimeException | ServletException | IOException e) {
            log.warn("process request failed: {}", url, e);
            throw e;
        } catch (Exception e) {
            log.warn("process request failed: {}", url, e);
            throw new NestedRuntimeException(e);
        }
    }

    private void doService(String url, HttpServletRequest req, HttpServletResponse resp, List<Dispatcher> dispatchers)
            throws Exception {
        for (Dispatcher dispatcher : dispatchers) {
            Result result = dispatcher.process(url, req, resp);
            if (result.processed) {
                handleResult(url, req, resp, dispatcher, result);
                return;
            }
        }
        // No handler found
        resp.sendError(404, "Not Found");
    }

    private void handleResult(String uri, HttpServletRequest req, HttpServletResponse resp,
                              Dispatcher dispatcher, Result result) throws ServletException, IOException {
        if (dispatcher.isRest()) {
            // send rest response
            if (!resp.isCommitted()) {
                resp.setContentType("application/json");
            }
            Object retObj = result.getReturnObj();
            if (dispatcher.isResponseBody()) {
                // send as response body
                if (retObj instanceof String) {
                    PrintWriter pw = resp.getWriter();
                    pw.write((String) retObj);
                    pw.flush();
                } else if (retObj instanceof byte[]) {
                    ServletOutputStream out = resp.getOutputStream();
                    out.write((byte[]) retObj);
                    out.flush();
                } else {
                    throw new ServletException("Unable to process REST Result when handle url: " + uri);
                }
            } else if (!dispatcher.isVoid()) {
                // output json
                PrintWriter pw = resp.getWriter();
                JsonUtils.writeJson(pw, retObj);
            }
        } else {
            // todo process MVC
        }
    }

    @Slf4j
    @ToString
    @Getter
    static class Dispatcher {
        static final Result NOT_PROCESSED = new Result(false, null);

        private boolean isRest;
        private boolean isResponseBody;
        private boolean isVoid;
        private Pattern urlPattern;
        private Object controller;
        private Method handlerMethod;
        private Param[] methodParameters;

        public Dispatcher(boolean isRest, Object controller, Method method, String urlPattern) throws ServletException {
            this.isRest = isRest;
            this.isResponseBody = method.getAnnotation(ResponseBody.class) != null;
            this.isVoid = method.getReturnType() == void.class;
            this.urlPattern = PathUtils.compile(urlPattern);
            this.controller = controller;
            this.handlerMethod = method;

            // resolve method parameters
            Parameter[] parameters = method.getParameters();
            Annotation[][] paramAnnos = method.getParameterAnnotations();
            this.methodParameters = new Param[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                this.methodParameters[i] = new Param(method, parameters[i], paramAnnos[i]);
            }

            log.info("Mapping {} to Controller {}.{}", urlPattern, controller.getClass().getName(), method.getName());
            for (Param mp : methodParameters) {
                log.debug("> parameter: {}", mp);
            }
        }

        public Result process(String uri, HttpServletRequest req, HttpServletResponse resp) throws Exception {
            Matcher matcher = urlPattern.matcher(uri);
            if (!matcher.matches()) {
                return NOT_PROCESSED;
            }

            // resolve values for each parameter
            Object[] args = new Object[methodParameters.length];
            for (int i = 0; i < methodParameters.length; i++) {
                Param p = methodParameters[i];
                switch (p.paramType) {
                    case PATH_VARIABLE:
                        String pathStr = matcher.group(p.name);
                        args[i] = convert(pathStr, p.classType);
                        break;
                    case REQUEST_PARAM:
                        String paramStr = getOrDefault(req, p.name, p.defaultValue);
                        args[i] = convert(paramStr, p.classType);
                        break;
                    case REQUEST_BODY:
                        BufferedReader reader = req.getReader();
                        args[i] = JsonUtils.readJson(reader, p.classType);
                        break;
                    case SERVLET_VARIABLE:
                        if (p.classType == HttpServletRequest.class) {
                            args[i] = req;
                        } else if (p.classType == HttpServletResponse.class) {
                            args[i] = resp;
                        } else if (p.classType == HttpSession.class) {
                            args[i] = req.getSession();
                        } else if (p.classType == ServletContext.class) {
                            args[i] = req.getServletContext();
                        } else {
                            throw new ServerErrorException("Could not determine argument type: " + p.classType);
                        }
                        break;
                    default:
                        throw new ServerErrorException("Unsupported Parameter Type: " + p.paramType);
                }
            }

            // invoke handler method
            Object result = null;
            try {
                result = this.handlerMethod.invoke(this.controller, args);
            } catch (InvocationTargetException e) {
                // why do this?
                Throwable t = e.getCause();
                if (t instanceof Exception) {
                    throw (Exception) t;
                }
                throw e;
            } catch (ReflectiveOperationException e) {
                throw new ServerErrorException(e);
            }
            return new Result(true, result);
        }

        private String getOrDefault(HttpServletRequest req, String name, String defaultValue) {
            String value = req.getParameter(name);
            if (value == null) {
                if (Objects.equals(defaultValue, DEFAULT_PARAM_VALUE)) {
                    throw new ServerErrorException("Missing required parameter: " + name);
                }
                return defaultValue;
            }
            return value;
        }

        private Object convert(String str, Class<?> classType) {
            if (classType == String.class) {
                return str;
            } else if (classType == boolean.class || classType == Boolean.class) {
                return Boolean.valueOf(str);
            } else if (classType == int.class || classType == Integer.class) {
                return Integer.valueOf(str);
            } else if (classType == long.class || classType == Long.class) {
                return Long.valueOf(str);
            } else if (classType == byte.class || classType == Byte.class) {
                return Byte.valueOf(str);
            } else if (classType == short.class || classType == Short.class) {
                return Short.valueOf(str);
            } else if (classType == float.class || classType == Float.class) {
                return Float.valueOf(str);
            } else if (classType == double.class || classType == Double.class) {
                return Double.valueOf(str);
            } else {
                throw new ServerErrorException("Could not determine argument type: " + classType);
            }
        }

    }

    @ToString
    @Getter
    static class Param {
        private String name;
        private ParamType paramType;
        private Class<?> classType;
        private String defaultValue;

        public Param(Method method, Parameter parameter, Annotation[] annotations) throws ServletException {
            PathVariable pathVariable = getAnnotation(annotations, PathVariable.class);
            RequestParam requestParam = getAnnotation(annotations, RequestParam.class);
            RequestBody requestBody = getAnnotation(annotations, RequestBody.class);

            // check, only one type annotation can be present.
            int total = (pathVariable != null ? 1 : 0) + (requestParam != null ? 1 : 0) + (requestBody != null ? 1 : 0);
            if (total > 1) {
                String message = String.format("Annotation @PathVariable, @RequestParam and @RequestBody cannot be combined at method '%d'",
                        method.getName());
                throw new ServletException(message);
            }

            this.classType = parameter.getType();
            if (pathVariable != null) {
                name = pathVariable.value();
                paramType = ParamType.PATH_VARIABLE;
            } else if (requestParam != null) {
                name = requestParam.value();
                paramType = ParamType.REQUEST_PARAM;
                defaultValue = requestParam.defaultValue();
            } else if (requestBody != null) {
                this.paramType = ParamType.REQUEST_BODY;
            } else {
                this.paramType = ParamType.SERVLET_VARIABLE;
                // check servlet variable type:
                if (this.classType != HttpServletRequest.class && this.classType != HttpServletResponse.class && this.classType != HttpSession.class
                        && this.classType != ServletContext.class) {
                    throw new ServerErrorException("(Missing annotation?) Unsupported argument type: " + classType + " at method: " + method);
                }
            }
        }
    }

    @Getter
    @AllArgsConstructor
    @ToString
    static class Result {
        private boolean processed;
        private Object returnObj;
    }

    enum ParamType {

        /**
         * 路径参数，从URL中提取
         */
        PATH_VARIABLE,

        /**
         * URL参数，从URL Query或Form表单提取
         */
        REQUEST_PARAM,

        /**
         * REST请求参数，从Post传递的JSON提取
         */
        REQUEST_BODY,

        /**
         * HttpServletRequest等Servlet API提供的参数，直接从DispatcherServlet的方法参数获得。
         */
        SERVLET_VARIABLE;
    }
}
