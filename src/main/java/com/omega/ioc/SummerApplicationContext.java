package com.omega.ioc;

import com.omega.annotation.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
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


    public SummerApplicationContext(Class<T> configClass) {
        this.configClass = configClass;
        this.configClassLoader = configClass.getClassLoader();

        initBeanDefinitionMap();
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
            if ("singleton".equalsIgnoreCase(beanDefinition.getScope())) {
                Object bean = createBean(beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    public Object createBean(BeanDefinition beanDefinition) {
        Class<?> beanClazz = beanDefinition.getClazz();
        try {
            Object instance = beanClazz.getDeclaredConstructor().newInstance();

            // 对 bean 进行自动装配
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
            return createBean(beanDefinition);
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
