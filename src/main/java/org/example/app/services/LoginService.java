package org.example.app.services;

import org.apache.log4j.Logger;
import org.example.web.dto.LoginForm;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LoginService {

    private final Logger logger = Logger.getLogger(LoginService.class);

    public boolean authenticate(LoginForm loginForm) {
        logger.info("try auth with user-form: " + loginForm);
        return Objects.equals("root", loginForm.getUsername()) && Objects.equals("123", loginForm.getPassword());
    }
}
