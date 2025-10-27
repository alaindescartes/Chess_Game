package com.backend.chess_backend.controllers;

import  org.springframework.web.bind.annotation.*;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @GetMapping("/hello")
    public String hello(){
        return "hello from backend";
    }
}