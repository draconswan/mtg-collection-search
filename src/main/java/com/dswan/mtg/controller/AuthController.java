package com.dswan.mtg.controller;

import com.dswan.mtg.domain.entity.AuthRequestDto;
import com.dswan.mtg.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public String register(@ModelAttribute AuthRequestDto request, RedirectAttributes redirectAttributes) {
        userDetailsService.register(request.getEmail(), request.getUsername(), request.getPassword());
        redirectAttributes.addFlashAttribute("message", "Registration successful");
        return "redirect:/user/login";
    }
}