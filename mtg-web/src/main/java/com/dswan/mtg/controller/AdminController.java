package com.dswan.mtg.controller;

import com.dswan.mtg.domain.entity.UserRole;
import com.dswan.mtg.repository.UserRepository;
import com.dswan.mtg.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @Value("${mtg.api.refreshEndpoint}")
    private String apiRefreshEndpoint;

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("apiRefreshEndpoint", apiRefreshEndpoint);
        return "admin/admin-panel";
    }

    @PostMapping("/promote")
    public String promoteUser(@RequestParam String username, RedirectAttributes redirectAttributes) {
        userDetailsService.updateRole(username, UserRole.ROLE_USER);
        redirectAttributes.addFlashAttribute("message", username + " promoted to USER");
        return "redirect:/admin/users";
    }
}