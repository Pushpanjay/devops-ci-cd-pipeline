package com.devops.service;

import com.devops.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    UserService service = new UserService();

    @Test
    public void testValidUser() {
        User user = new User("John", 25);
        assertEquals("Valid User", service.validateUser(user));
    }

    @Test
    public void testUnderageUser() {
        User user = new User("John", 15);
        assertEquals("Underage", service.validateUser(user));
    }

    @Test
    public void testInvalidName() {
        User user = new User("", 25);
        assertEquals("Invalid Name", service.validateUser(user));
    }
}
