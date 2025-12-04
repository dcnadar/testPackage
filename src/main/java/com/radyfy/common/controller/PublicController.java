package com.radyfy.common.controller;

import com.radyfy.common.service.common.MemoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/public")
public class PublicController {

    private MemoryService memoryService;

    public PublicController(
            MemoryService memoryService
    ){
        this.memoryService = memoryService;
    }

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    private ResponseEntity<String> healthCheck() {

        return new ResponseEntity<>("healthy", HttpStatus.OK);
    }

    @RequestMapping(value = "/memory/cache/invalidate", method = RequestMethod.GET)
    private ResponseEntity<String> invalidateCache() {
        this.memoryService.invalidateAll();
        return new ResponseEntity<>("healthy", HttpStatus.OK);
    }
}
