package com.jcw.checklist.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("prod")
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
} 