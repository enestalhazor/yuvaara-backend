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
    public ResponseEntity<?> addList(@NotBlank(message = "Name is required") @RequestParam String name,
                                     @NotBlank(message = "Species is required") @RequestParam String species,
                                     @NotBlank(message = "Breed is required") @RequestParam String breed,
                                     @NotNull(message = "Age is required") @RequestParam Integer age,
                                     @NotBlank(message = "Gender is required") @RequestParam String gender,
                                     @NotBlank(message = "Color is required") @RequestParam String color,
                                     @NotBlank(message = "Status is required") @RequestParam String status,
                                     @NotNull(message = "Photo is required") @RequestParam(value = "photo_url") MultipartFile photoUrl,
                                     @NotBlank(message = "Location is required") @RequestParam String location
    ) throws IOException {

        Integer user_id = RequestContext.getUserId();
        if (user_id == null) {
            return ResponseEntity.status(401).body(Map.of("info", "You must be logged in to do this"));
        }

        String fileName = "";
        if (photoUrl != null && !photoUrl.isEmpty()) {

            String contentType = photoUrl.getContentType();
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.status(400).body(Map.of("info", "Please upload a valid image file"));
            }

            Path uploadDir = Paths.get(System.getProperty("user.dir"), "petphotos");
            Files.createDirectories(uploadDir);

            fileName = Paths.get(photoUrl.getOriginalFilename()).getFileName().toString();
            Path uploadPath = uploadDir.resolve(fileName);
            photoUrl.transferTo(uploadPath.toFile());
        }

        try {
            if(repository.addList(user_id, name, species, breed, age, gender, color, status, fileName, location)) {
                return ResponseEntity.ok(Map.of("info", "Listing created successfully"));
            }

            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("info", "Something went wrong, please try again"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getLists() {

        try {
            List<Map<String, Object>> lists = repository.getLists();
            if (lists.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("info", "No listings found"));
            }
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getListsByUserId() {

        Integer userId = RequestContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("info", "You must be logged in to do this"));
        }

        try {
            List<Map<String, Object>> lists = repository.getListsByUserId(userId);
            if (lists.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("info", "You have no listings yet"));
            }
            return ResponseEntity.ok(lists);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        }
    }

    @GetMapping("/{term}")
    public ResponseEntity<?> filterLists(@PathVariable String term) {
        List<Map<String, Object>> result = repository.filterLists(term);

        if(result.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("info", "No results found"));
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListById(@PathVariable Integer id) {

        Integer userId = RequestContext.getUserId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("info", "You must be logged in to do this"));
        }

        try {
            if (repository.deleteListById(id, userId)) {
                return ResponseEntity.ok(Map.of("info", "Listing deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("info", "Listing not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        }
    }
}