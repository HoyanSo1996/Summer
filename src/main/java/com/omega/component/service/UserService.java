package com.omega.component.service;

import com.omega.annotation.Autowired;
import com.omega.annotation.Component;
import com.omega.component.dao.UserDAO;
import com.omega.processor.InitializingBean;

/**
 * Class UserService
 *
 * @author KennySo
 * @date 2024/6/14
 */
@Component("userService")
public class UserService implements InitializingBean {

    @Autowired
    private UserDAO userDAO;

    public void sayHi() {
        userDAO.sayHi();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("userService init()...");
    }
}
