package com.omega.component.aspect;

import com.omega.annotation.Component;

/**
 * Class SmartDog
 *
 * @author KennySo
 * @date 2024/6/17
 */
@Component
public class SmartDog implements SmartAnimal {

    @Override
    public double getSum(double num1, double num2) {
        double result = num1 + num2;
        System.out.println("getSum() 方法内部打印 result = " + result);
        return result;
    }

    @Override
    public double getSub(double num1, double num2) {
        double result = num1 - num2;
        System.out.println("getSub() 方法内部打印 result = " + result);
        return result;
    }
}
