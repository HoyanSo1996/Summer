package com.omega.ioc;

import com.omega.annotation.*;
import com.omega.processor.BeanPostProcessor;
import com.omega.processor.InitializingBean;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class SummerApplicationContext
 *
 * @author KennySo
 * @date 2024/6/14
 */
public class SummerApplicationContext<T> {

    private final Class<T> configClass;
    private final ClassLoader configClassLoader;

    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    public SummerApplicationContext(Class<T> configClass) {
        this.configClass = configClass;
        this.configClassLoader = configClass.getClassLoader();

        initBeanDefinitionMap();

        // beanPostProcessorList 的初始化可以和 beanDefinitionMap 放在一起
        initBeanPostProcessorList();

        /*
           singletonObjects 的初始化和 beanDefinitionMap 的初始化中分开的原因是,
           bean 在初始化后需要进行 依赖注入、后置处理器 操作, 这些需要所有加了 @component 的
           bean 初始化完后才能进行.
         */
        initSingletonObjects();
    }

    public void initBeanDefinitionMap() {
        // 1.获取要扫描的包路径
        ComponentScan componentScan = configClass.getDeclaredAnnotation(ComponentScan.class);
        String scanPath = componentScan.value();
        scanPath = scanPath.replace(".", "/");

        // 2.获取待扫描的文件夹
        String path = this.configClassLoader.getResource(scanPath).getPath();
        File targetDirectory = new File(path);

        // 3.对目标文件夹进行扫描
        scanDirectory(targetDirectory, scanPath);
    }

    /**
     * 扫描目标文件夹下的所有文件
     * @param directoryFile 目标文件夹
     */
    public void scanDirectory(File directoryFile, String scanPath) {
        File[] files = directoryFile.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                scanBean(file, scanPath);
            } else {
                scanDirectory(file, scanPath);
            }
        }
    }

    /**
     * 扫描java文件
     * @param file 文件
     */
    public void scanBean(File file, String scanPath) {
        if (file.getName().endsWith(".class")) {
            String fileAbsolutePath = file.getAbsolutePath().replace("\\", "/");
            String fileName = fileAbsolutePath
                    .substring(fileAbsolutePath.lastIndexOf("/") + 1, fileAbsolutePath.indexOf(".class"));
            String classAllPath = fileAbsolutePath
                    .substring(fileAbsolutePath.indexOf(scanPath), fileAbsolutePath.indexOf(".class")).replace("/", ".");
            // System.out.println("fileName = " + fileName + ", classAllPath = " + classAllPath);

            try {
                // 轻量级加载
                Class<?> clazz = this.configClassLoader.loadClass(classAllPath);

                // 获取类上存在 @Component注解 的 Bean
                if (clazz.isAnnotationPresent(Component.class)) {
                    // 1.将类信息封装到 BeanDefinition
                    BeanDefinition beanDefinition = new BeanDefinition();
                    if (clazz.isAnnotationPresent(Scope.class)) {
                        String scopeValue = clazz.getDeclaredAnnotation(Scope.class).value();
                        if (!"singleton".equalsIgnoreCase(scopeValue) && !"prototype".equalsIgnoreCase(scopeValue)) {
                            throw new RuntimeException("Not exist scope : " + scopeValue);
                        }
                        beanDefinition.setScope(scopeValue);
                    } else {
                        beanDefinition.setScope("singleton");
                    }
                    beanDefinition.setClazz(clazz);

                    // 2.将 BeanDefinition 放入 BeanDefinitionMap 中
                    String beanName = clazz.getDeclaredAnnotation(Component.class).value();
                    if ("".equals(beanName)) {
                        beanName = StringUtils.uncapitalize(fileName);
                    }
                    beanDefinitionMap.put(beanName, beanDefinition);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void initSingletonObjects() {
        Enumeration<String> beanDefinitionKeys = beanDefinitionMap.keys();
        while (beanDefinitionKeys.hasMoreElements()) {
            // 获取 beanName
            String beanName = beanDefinitionKeys.nextElement();
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

            // 不把 后置处理器的实现类 放入单例池
            // *******************************
            if (BeanPostProcessor.class.isAssignableFrom(beanDefinition.getClazz())) {
                continue;
            }
            // *******************************

            if ("singleton".equalsIgnoreCase(beanDefinition.getScope())) {
                Object bean = createBean(beanDefinition, beanName);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    public void initBeanPostProcessorList() {
        Enumeration<String> beanDefinitionKeys = beanDefinitionMap.keys();
        while (beanDefinitionKeys.hasMoreElements()) {
            // 获取 beanName
            String beanName = beanDefinitionKeys.nextElement();
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            Class<?> clazz = beanDefinition.getClazz();
            // 判断当前 clazz 有无实现 BeanPostProcessor
            // 这里不能使用 instanceof 来进行判断, 因为 clazz 是个类对象, 不是实例对象. 这里可以把 isAssignableFrom() 当做一个语法
            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                try {
                    BeanPostProcessor beanPostProcessor = (BeanPostProcessor) clazz.newInstance();
                    beanPostProcessorList.add(beanPostProcessor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    public Object createBean(BeanDefinition beanDefinition, String beanName) {
        Class<?> beanClazz = beanDefinition.getClazz();
        try {
            // 1. 实例化 Bean
            Object instance = beanClazz.getDeclaredConstructor().newInstance();

            // 2. 对 bean 进行自动装配
            for (Field declaredField : beanClazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    // 按类型进行装配
                    Class<?> fieldClazz = declaredField.getType();
                    Object bean = getBean(fieldClazz);
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);

                } else if (declaredField.isAnnotationPresent(Resource.class)) {
                    // todo 按字段名进行匹配
                }
            }

            // 3. 在初始化前执行 后置处理器的before方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                Object current = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
                /* ======= 细节 ======= */
                if (current != null) {
                    instance = current;
                }
                /* ======= 细节 ======= */
            }

            // 4. bean 执行 初始化方法
            /*
               功能: 创建好 Bean 后, 判断是否需要进行初始化
               Tips: 这是容器中常用的一种做法, 根据该类是否实现了某个接口, 来判断是否要执行某个业务逻辑, 是java基础的接口编程的实际运用.
                     例如 Serializable 接口, 是一个标记接口, 用来判断是否需要对类进行序列化
             */
            if (instance instanceof InitializingBean) {
                InitializingBean bean = (InitializingBean) instance;
                bean.afterPropertiesSet();
            }

            // 5. 在初始化前执行 后置处理器的before方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                Object current = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
                if (current != null) {
                    instance = current;
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get bean by bean name
     */
    public Object getBean(String beanName) {
        if (!beanDefinitionMap.containsKey(beanName)) {
            throw new NullPointerException("Bean " + beanName + " does not exist.");
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if ("singleton".equalsIgnoreCase(beanDefinition.getScope())) {
            // 单例
            return this.singletonObjects.get(beanName);
        } else {
            // 多例
            return createBean(beanDefinition, beanName);
        }
    }

    /**
     * get bean by bean type
     */
    private Object getBean(Class<?> clazz) {
        Enumeration<String> beanDefinitionKeys = beanDefinitionMap.keys();
        while (beanDefinitionKeys.hasMoreElements()) {
            String beanName = beanDefinitionKeys.nextElement();
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getClazz() == clazz) {
                return getBean(beanName);
            }
        }
        return null;
    }
}
