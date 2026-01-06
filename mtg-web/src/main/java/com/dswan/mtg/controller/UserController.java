package com.dswan.mtg.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "login";
    }

    @GetMapping("/register")
    public String registrationPage(Model model) {
        model.addAttribute("pageTitle", "Registration");
        return "register";
    }

    @GetMapping("/decks")
    public String decks(Model model){
        model.addAttribute("pageTitle", "User Decks");
        return "decks";
    }

    @GetMapping("/user/deck/new")
    public String newDeck(Model model){
        return "new-deck";
    }
}