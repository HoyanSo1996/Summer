package com.omega.component.entity;

import com.omega.annotation.Component;
import com.omega.processor.InitializingBean;

/**
 * Class Car
 *
 * @author KennySo
 * @date 2024/6/17
 */
@Component
public class Car implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Car init()...");
    }
}
