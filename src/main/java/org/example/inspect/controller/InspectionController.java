package org.example.inspect.controller;

import org.example.inspect.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inspection")
public class InspectionController {

    @Autowired
    private InspectionService inspectionService;

    @PostMapping("/{id}/start")
    public void startInspection(@PathVariable Long id){
        inspectionService.startInspection(id);
    }

}
