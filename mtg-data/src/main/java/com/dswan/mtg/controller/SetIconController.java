package com.dswan.mtg.controller;

import com.dswan.mtg.repository.SetRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SetIconController {

    private final SetRepository setRepository;

    @Cacheable("setIcon")
    @GetMapping(value = "/set-icon/{code}.svg", produces = "image/svg+xml")
    public ResponseEntity<String> getIcon(@PathVariable String code) {
        return setRepository.findById(code)
                .map(set -> ResponseEntity.ok(set.getIconSvg()))
                .orElse(ResponseEntity.notFound().build());
    }
}