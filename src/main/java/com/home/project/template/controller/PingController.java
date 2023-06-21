package com.home.project.template.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<Void> ping() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
