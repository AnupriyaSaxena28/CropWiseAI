package com.cropwise.backend.controller;

import com.cropwise.backend.dto.SchemeDto;
import com.cropwise.backend.service.SchemesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schemes")
public class SchemesController {

    private final SchemesService schemes;
    public SchemesController(SchemesService schemes) { this.schemes = schemes; }

    @GetMapping
    public List<SchemeDto> all() { return schemes.all(); }
}
