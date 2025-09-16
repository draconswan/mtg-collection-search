package com.dswan.mtg.controller;

import com.dswan.mtg.domain.entity.AuthRequestDto;
import com.dswan.mtg.domain.entity.UserDetailsDto;
import com.dswan.mtg.domain.entity.UserRole;
import com.dswan.mtg.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

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

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        RedirectAttributes redirectAttributes) {
        try {
            UserDetailsDto userDetails = userDetailsService.authenticate(username, password);
            redirectAttributes.addFlashAttribute("message", "Login successful");
            return "redirect:/search/input"; // or wherever you want to land
        } catch (AuthenticationException ex) {
            redirectAttributes.addFlashAttribute("error", "Invalid credentials");
            return "redirect:/user/login";
        }
    }
}