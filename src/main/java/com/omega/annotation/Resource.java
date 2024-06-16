package com.omega.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class Resource
 *
 * @author KennySo
 * @date 2024/6/16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {

    String name() default "";

    Class<?> type() default java.lang.Object.class;
}
