package com.example.yuvaarabackend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/adoptionforms")
public class AdoptionFormsController {

    private AdoptionFormsRepository repository;
    public AdoptionFormsController(AdoptionFormsRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> addForm(@NotBlank(message = "FullName cannot be empty") @RequestParam String message, @NotNull(message = "Adoption list id cannot be empty") @RequestParam Integer adoption_list_id) throws IOException {

        Integer user_id = RequestContext.getUserId();

        if (user_id == null) {
            return ResponseEntity.status(401).body(Map.of("info", "Unauthorized"));
        }

        try {
            if(repository.add(message, user_id, adoption_list_id))
            {
                return ResponseEntity.ok(Map.of("info", "Form created"));
            }

            return ResponseEntity.status(500).body(Map.of("info", "Form could not be created"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("info", "Bad request " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getForms() {

        Integer id = RequestContext.getUserId();

        if (id == null) {
            return ResponseEntity.status(401).body(Map.of("info", "Unauthorized"));
        }

        try {
            List<Map<String, Object>> products = repository.forms();
            if (products == null) {
                return ResponseEntity.status(404).body(Map.of("info", "No forms found"));
            }
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("info", "Error fetching forms: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Integer id) {

        if (id == null) {
            return ResponseEntity.status(401).body(Map.of("info", "Unauthorized"));
        }
        try {
            if (repository.deleteFormById(id)) {
                return ResponseEntity.ok(Map.of("info", "Form deleted"));
            } else {
                return ResponseEntity.status(404).body(Map.of("info", "Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body(Map.of("info", "DB error"));
        }
    }
}
