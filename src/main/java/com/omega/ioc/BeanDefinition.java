package com.omega.ioc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class BeanDefinition
 * 用于封装 Bean 的消息
 *
 * @author KennySo
 * @date 2024/6/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanDefinition {

    private String scope;
    private Class<?> clazz;
}
