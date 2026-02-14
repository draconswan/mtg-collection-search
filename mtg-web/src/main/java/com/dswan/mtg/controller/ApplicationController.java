package com.dswan.mtg.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ApplicationController {

    @RequestMapping(value = {"/", "/**/{path:[^\\.]*}"})
    public String redirect() {
        return "redirect:/search/input";
    }
}
