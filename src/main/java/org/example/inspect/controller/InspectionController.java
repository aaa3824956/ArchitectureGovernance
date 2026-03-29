package org.example.inspect.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/inspection")
public class InspectionController {

    @GetMapping("helloworld")
    public String getMethodName() {
        return "hello world";
    }
}