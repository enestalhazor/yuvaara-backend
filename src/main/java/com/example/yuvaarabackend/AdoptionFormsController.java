package com.example.yuvaarabackend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> addForm(@NotBlank(message = "Message is required") @RequestParam String message,
                                     @NotNull(message = "Adoption listing id is required") @RequestParam Integer adoptionListId) throws IOException {

        Integer userId = RequestContext.getUserId();

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("info", "You must be logged in to do this"));
        }

        try {
            if(repository.addForm(message, userId, adoptionListId)) {
                return ResponseEntity.ok(Map.of("info", "Application submitted successfully"));
            }

            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("info", "Something went wrong, please try again"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getForms() {

        Integer userId = RequestContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("info", "You must be logged in to do this"));
        }

        try {
            List<Map<String, Object>> forms = repository.getForms(userId);
            if (forms.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(forms);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFormById(@NotNull(message = "Id is required") @PathVariable Integer id) {

        Integer userId = RequestContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("info", "You must be logged in to do this"));
        }

        try {
            if (repository.deleteFormById(id, userId)) {
                return ResponseEntity.ok(Map.of("info", "Application withdrawn successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("info", "Application not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        }
    }
}