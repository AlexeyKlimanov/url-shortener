package io.github.AlexeyKlimanov.url_shortener.controller;

import io.github.AlexeyKlimanov.url_shortener.dto.HelloResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class HelloController {
    @GetMapping("/hello")
    public HelloResponse hello(){
        return new HelloResponse("Hello everybody!", 1);
    }
}
