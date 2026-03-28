package com.devops.service;

import com.devops.model.User;

public class UserService {

    public String validateUser(User user) {
        if (user.getAge() < 18) {
            return "Underage";
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            return "Invalid Name";
        }
        return "Valid User";
    }
}
