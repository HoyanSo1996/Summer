package com.omega.component.processor;

import com.omega.annotation.Component;
import com.omega.component.aspect.SmartAnimalAspect;
import com.omega.processor.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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

        // 对 bean 进行 AOP 处理
        // 1. 先将切面类进行解析，然后将需要被切面类里的信息保存到一个map中
        // map1 {key: beanName, value: map2{key: methodName,  value: map3{key: 通知类型, value: 切面类的类对象}}}
        if ("smartDog".equals(beanName)) {
            Class<?> beanClazz = bean.getClass();
            Object proxyInstance = Proxy.newProxyInstance(
                    beanClazz.getClassLoader(),
                    beanClazz.getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String methodName = method.getName();
                            Object result = null;
                            // 由于 动态代理 的增强会作用在 目标对象 的所有方法, 所以这里要进行判断
                            // 2. 在 map2 中查询是否是有需要代理的方法
                            if ("getSum".equals(methodName)) {
                                // 3. 在 map3 中查看方法的通知类型, 和切面类对象, 用切面类对象进行方法的反射调用
                                // if("before".equals(key)) {}
                                SmartAnimalAspect.showBeginLog();
                                result = method.invoke(bean, args);
                                // if("afterReturning".equals(key)) {}
                                SmartAnimalAspect.showSuccessEndLog();
                            } else {
                                result = method.invoke(bean, args);
                            }
                            return result;
                        }
                    });
            return proxyInstance;
        }
        return bean;
    }
}