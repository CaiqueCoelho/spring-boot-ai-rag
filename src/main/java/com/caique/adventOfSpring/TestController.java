package com.caique.adventOfSpring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spi2")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
