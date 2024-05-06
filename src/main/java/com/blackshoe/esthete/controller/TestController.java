package com.blackshoe.esthete.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    @GetMapping("/test")
    public ResponseEntity<?> Test() {

        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        return ResponseEntity.ok("Login user : " + name + "!");
    }
}
