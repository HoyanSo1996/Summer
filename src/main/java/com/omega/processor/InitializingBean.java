package com.omega.processor;

/**
 * Class InitializingBean
 *
 * @author KennySo
 * @date 2024/6/17
 */
public interface InitializingBean {

    void afterPropertiesSet() throws Exception;
}
