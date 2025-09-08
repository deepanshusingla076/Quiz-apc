package com.qwizz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "QWIZZ - Interactive Quiz Platform");
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About QWIZZ");
        return "about";
    }

    @GetMapping("/features")
    public String features(Model model) {
        model.addAttribute("pageTitle", "Features - QWIZZ");
        return "features";
    }
}
