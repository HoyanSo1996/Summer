package com.omega.processor;

import com.sun.istack.internal.Nullable;

/**
 * Class BeanPostProcessor
 *
 * @author KennySo
 * @date 2024/6/17
 */
public interface BeanPostProcessor {

    @Nullable
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Nullable
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
