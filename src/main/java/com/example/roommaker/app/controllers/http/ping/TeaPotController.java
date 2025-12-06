package com.example.roommaker.app.controllers.http.ping;

import com.example.roommaker.app.domain.exceptions.TeapotException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teapot")
public class TeaPotController {
    @GetMapping
    public void teaPot() {
        throw new TeapotException();
    }
}
