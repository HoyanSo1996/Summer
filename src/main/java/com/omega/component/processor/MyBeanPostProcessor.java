package com.omega.component.processor;

import com.omega.annotation.Component;
import com.omega.processor.BeanPostProcessor;

/**
 * Class MyBeanPostProcessor
 *
 * @author KennySo
 * @date 2024/6/17
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println(beanName + " 执行了 postProcessBeforeInitialization()...");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println(beanName + " 执行了 postProcessAfterInitialization()...");
        return bean;
    }

}
