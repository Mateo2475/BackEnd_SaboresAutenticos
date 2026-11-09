package com.primer.api.appgpt.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class holaController {

    @GetMapping("/saludo")
    public String decirHola() {
        return "¡Hola Mundo desde Spring Boot con Java 21!";
    }
}
