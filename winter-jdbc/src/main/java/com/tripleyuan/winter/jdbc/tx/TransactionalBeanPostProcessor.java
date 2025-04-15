package com.tripleyuan.winter.jdbc.tx;

import com.tripleyuan.winter.annotation.Transactional;
import com.tripleyuan.winter.aop.AnnotationProxyBeanPostProcessor;

public class TransactionalBeanPostProcessor extends AnnotationProxyBeanPostProcessor<Transactional> {
}
