package com.omega;

import com.omega.component.aspect.SmartAnimal;
import com.omega.component.service.UserService;
import com.omega.config.SummerConfig;
import com.omega.ioc.SummerApplicationContext;

/**
 * Class ${NAME}
 *
 * @author KennySo
 * @date 2024/6/11
 */
public class Main {

    public static void main(String[] args) {
        SummerApplicationContext<SummerConfig> ioc = new SummerApplicationContext<>(SummerConfig.class);

        // UserService userService = (UserService) ioc.getBean("userService");
        // UserService userService2 = (UserService) ioc.getBean("userService");
        // System.out.println("userService = " + userService);
        // System.out.println("userService2 = " + userService2);
        // UserDAO userDAO = (UserDAO) ioc.getBean("userDAO");
        // UserDAO userDAO2 = (UserDAO) ioc.getBean("userDAO");
        // System.out.println("userDAO = " + userDAO);
        // System.out.println("userDAO2 = " + userDAO2);

        // UserService userService = (UserService) ioc.getBean("userService");
        // userService.sayHi();
        // System.out.println("ok~");

        SmartAnimal smartDog = (SmartAnimal) ioc.getBean("smartDog");
        smartDog.getSum(10, 8);
        smartDog.getSub(10, 8);
    }
}