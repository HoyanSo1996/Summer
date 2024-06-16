package com.omega.component.service;

import com.omega.annotation.Autowired;
import com.omega.annotation.Component;
import com.omega.component.dao.UserDAO;

/**
 * Class UserService
 *
 * @author KennySo
 * @date 2024/6/14
 */
@Component("userService")
public class UserService {

    @Autowired
    private UserDAO userDAO;

    public void sayHi() {
        userDAO.sayHi();
    }
}
