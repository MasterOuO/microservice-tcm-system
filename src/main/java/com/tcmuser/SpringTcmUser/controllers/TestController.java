package com.tcmuser.SpringTcmUser.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {
    @GetMapping("/test")
    public String testPage() {
        return "test"; // 這會去 templates 找 test.html
    }
}