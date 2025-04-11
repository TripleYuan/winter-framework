package com.tripleyuan.winter.demo.bean;

import com.tripleyuan.winter.annotation.Autowired;
import com.tripleyuan.winter.annotation.Component;
import lombok.Getter;

@Component
@Getter
public class MonkeyBean {

    private BananaBean bananaBean;

    public MonkeyBean(@Autowired BananaBean bananaBean) {
        this.bananaBean = bananaBean;
    }

}
