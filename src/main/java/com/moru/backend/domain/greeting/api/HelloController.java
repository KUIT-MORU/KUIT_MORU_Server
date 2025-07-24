package com.moru.backend.domain.greeting.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Hello", description = "/ 접근시 Hello, MORU! 보여준다.")
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "Hello, MORU!";
    }
}
