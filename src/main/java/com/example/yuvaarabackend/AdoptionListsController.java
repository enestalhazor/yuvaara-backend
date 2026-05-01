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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/adoptionlists")
public class AdoptionListsController {

    private AdoptionListsRepository repository;
    public AdoptionListsController(AdoptionListsRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> addList(@NotBlank(message = "Name cannot be empty") @RequestParam String name, @NotBlank(message = "Species cannot be empty") @RequestParam String species,
            @NotBlank(message = "Breed cannot be empty") @RequestParam String breed, @NotNull(message = "Age cannot be empty") @RequestParam Integer age, @NotBlank(message = "Gender cannot be empty") @RequestParam String gender,
            @NotBlank(message = "Color cannot be empty") @RequestParam String color, @NotBlank(message = "Status cannot be empty") @RequestParam String status,
            @NotNull(message = "Photo URL cannot be empty") @RequestParam(value = "photo_url") MultipartFile photoUrl, @NotBlank(message = "Location cannot be empty") @RequestParam String location
    ) throws IOException {

        Integer user_id = RequestContext.getUserId();
        if (user_id == null) {
            return ResponseEntity.status(401).body(Map.of("info", "Unauthorized"));
        }

        String fileName = "";
        if (photoUrl != null && !photoUrl.isEmpty()) {

            String contentType = photoUrl.getContentType();
            if (!"image/jpeg".equalsIgnoreCase(contentType)) {
                return ResponseEntity.status(400).body(Map.of("info", "This is not JPEG file"));
            }

            Path uploadDir = Paths.get(System.getProperty("user.dir"), "petphotos");
            Files.createDirectories(uploadDir);

            fileName = Paths.get(photoUrl.getOriginalFilename()).getFileName().toString();
            Path uploadPath = uploadDir.resolve(fileName);
            photoUrl.transferTo(uploadPath.toFile());
        }

        try {

            if(repository.addList(user_id, name, species, breed, age, gender, color, status, fileName, location))
            {
                return ResponseEntity.ok(Map.of("info", "List created"));
            }

            return ResponseEntity.status(500).body(Map.of("info", "Failed to create list"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("info", "Bad request " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getLists() {

        Integer userId = RequestContext.getUserId();

        try {
            List<Map<String, Object>> lists = repository.getLists();
            if (lists.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("info", "No lists found"));
            }
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("info", "Internal server error"));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getListsByUserId() {

        Integer userId = RequestContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("info", "Unauthorized"));
        }

        try {
            List<Map<String, Object>> lists = repository.getListsByUserId(userId);
            if (lists.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("info", "No lists found"));
            }
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("info", "Internal server error"));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterLists(
            @RequestParam(required = false) List<String> species,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) String gender
    ) {
        List<Map<String, Object>> result = repository.filterLists(species, age, gender);

        if(result.isEmpty())
        {
            return ResponseEntity.status(404).body(Map.of("info", "No lists found"));
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListById(@PathVariable Integer id) {

        Integer userId = RequestContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("info", "Unauthorized"));
        }

        try {
            if (repository.deleteListById(id, userId)) {
                return ResponseEntity.ok(Map.of("info", "List deleted"));
            } else {
                return ResponseEntity.status(404).body(Map.of("info", "Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("info", "DB error"));
        }
    }
}
