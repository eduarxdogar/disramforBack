package com.disramfor.api.controller;

import com.disramfor.api.dto.AutoPartDTO;
import com.disramfor.api.service.AutoPartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AutoPartController {

    private final AutoPartService autoPartService;


    @GetMapping("/filters/types")
    public ResponseEntity<List<String>> getTypes() {
        return ResponseEntity.ok(autoPartService.getProductTypes());
    }

    @GetMapping("/filters/brands")
    public ResponseEntity<List<String>> getBrands(@RequestParam String type) {
        return ResponseEntity.ok(autoPartService.getBrands(type));
    }

    @GetMapping("/filters/models")
    public ResponseEntity<List<String>> getModels(@RequestParam String type, @RequestParam String brand) {
        return ResponseEntity.ok(autoPartService.getModels(type, brand));
    }

    @GetMapping("/filters/engines")
    public ResponseEntity<List<String>> getEngines(@RequestParam String type,
            @RequestParam String brand,
            @RequestParam String model) {
        return ResponseEntity.ok(autoPartService.getEngines(type, brand, model));
    }


    @GetMapping({ "/autoparts", "/productos" })
    public ResponseEntity<Page<AutoPartDTO>> searchParts(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String engine,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        String searchTerm = q;
        if (searchTerm == null && term != null)
            searchTerm = term;
        if (searchTerm == null && search != null)
            searchTerm = search;

        System.out.println("Filtros: type=" + type + ", brand=" + brand + ", model=" + model + ", engine=" + engine
                + ", search=" + searchTerm);

        return ResponseEntity.ok(autoPartService.searchParts(type, brand, model, engine, searchTerm, pageable));
    }
}
