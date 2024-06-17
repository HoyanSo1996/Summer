package com.omega.component.aspect;

import com.omega.annotation.Component;

/**
 * Class SmartAnimalAspect
 *
 * @author KennySo
 * @date 2024/6/10
 */
@Component
public class SmartAnimalAspect {

    public static void showBeginLog() {
        System.out.println("前置通知...");
    }

    public static void showSuccessEndLog() {
        System.out.println("后置通知...");
    }
}
