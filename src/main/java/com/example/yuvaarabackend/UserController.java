package com.example.yuvaarabackend;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    @PostMapping
    public ResponseEntity<?> Register(@NotBlank(message = "FullName cannot be empty") @RequestParam String fullname,
                                      @NotBlank(message = "Email cannot be empty") @RequestParam String email,
                                      @NotBlank(message = "Phone cannot be empty") @RequestParam String phone,
                                      @NotBlank(message = "Password cannot be empty") @Size(min = 6, message = "Password should be more than 6 characters") @RequestParam String password,
                                      @NotBlank(message = "Address cannot be empty") @RequestParam String address,
                                      @RequestParam(value = "date_of_birth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                      @RequestParam(value = "profile_picture_url", required = false) MultipartFile profilePic) throws IOException {

        try {
            String fileName = "";
            if (profilePic != null && !profilePic.isEmpty()) {

                String contentType = profilePic.getContentType();
                if (!"image/jpeg".equalsIgnoreCase(contentType)) {
                    return ResponseEntity.status(400).body(Map.of("info", "This is not JPEG file"));
                }

                Path uploadDir = Paths.get(System.getProperty("user.dir"), "userphotos");
                Files.createDirectories(uploadDir);

                fileName = Paths.get(profilePic.getOriginalFilename()).getFileName().toString();
                Path uploadPath = uploadDir.resolve(fileName);
                profilePic.transferTo(uploadPath.toFile());
            }

            if (email != null && !email.isBlank() && repository.checkIsEmailTaken(email)) {
                return ResponseEntity.status(422).body(Map.of("info", "Email taken"));
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return ResponseEntity.status(400).body(Map.of("info", "Invalid email format"));
            }

            if (phone != null && !phone.isBlank()) {
                if (!phone.matches("^\\+90 \\d{3} \\d{3} \\d{4}$")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Invalid phone format"));
                }
            }

            String hashedPassword = HashService.hashPassword(password);

            if (repository.save(fullname, email, phone, hashedPassword, address, fileName, dateOfBirth)) {
                return ResponseEntity.ok().body(Map.of("info", "User inserted"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("info", "Bad Request"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("info", "Bad request " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody LoginRequest request) throws NoSuchAlgorithmException {

        if (request.getPassword() == null || request.getPassword().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.status(400).body(Map.of("info", "Missing required field or fields"));
        }
        try {
            String hashedPassword = HashService.hashPassword(request.getPassword());
            Integer id = repository.check(request.getEmail(), hashedPassword);
            if (id != null && id > 0) {

                String email = request.getEmail();
                String token = JWTService.create(email, id);

                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("token", token);

                return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, token).body(responseBody);
            }

            return ResponseEntity.status(404).body("User not found");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    private Map<String, Object> lowerCaseMap(Map<String, Object> map) {
        Map<String, Object> lowerCaseMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            lowerCaseMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        return lowerCaseMap;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> GetUserInfo(@PathVariable Integer id) throws NoSuchAlgorithmException {

        if (!id.equals(RequestContext.getUserId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("info", "Unauthorized"));
        }

        try {

            var products = repository.userById(id);

            if (products == null || products.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("info", "Not found user for id: " + id));
            }

            Integer userid = RequestContext.getUserId();

            if (Objects.equals(userid, id)) {
                return ResponseEntity.ok(lowerCaseMap(products));
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> EditUserInfo(@PathVariable Integer id,
                                          @RequestParam(value = "fullname", required = false) String name,
                                          @RequestParam(value = "email", required = false) String email,
                                          @RequestParam(value = "phone", required = false) String phone,
                                          @RequestParam(value = "password", required = false) String password,
                                          @RequestParam(value = "address", required = false) String address,
                                          @RequestParam(value = "date_of_birth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                          @RequestParam(value = "profile_picture_url", required = false) MultipartFile profilePic) throws NoSuchAlgorithmException, IOException {

        if (id != RequestContext.getUserId()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("info", "Unauthorized"));
        }

        String fileName = null;
        String hashedPassword = "";
        if (profilePic != null && !profilePic.isEmpty()) {

            String contentType = profilePic.getContentType();
            if (!"image/jpeg".equalsIgnoreCase(contentType)) {
                return ResponseEntity.status(400).body(Map.of("info", "This is not JPEG file"));
            }

            Path uploadDir = Paths.get(System.getProperty("user.dir"), "productphotos");
            Files.createDirectories(uploadDir);

            fileName = Paths.get(profilePic.getOriginalFilename()).getFileName().toString();
            Path uploadPath = uploadDir.resolve(fileName);
            profilePic.transferTo(uploadPath.toFile());
        }

        try {
            if (email != null && repository.checkIsEmailTaken(email)) {
                return ResponseEntity.status(422).body(Map.of("info", "Email taken"));
            }

            if (email != null && !email.isBlank()) {
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Invalid email format"));
                }
            }

            if ( password != null && !password.isBlank()) {
                if (password.length() < 6) {
                    return ResponseEntity.status(400).body(Map.of("info", "Password length should be more than 6 character"));
                }
            }

            if (phone != null && !phone.isBlank()) {
                if (!phone.matches("^\\+90 \\d{3} \\d{3} \\d{4}$")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Invalid phone format. Use +90 xxx xxx xxxx"));
                }
            }

            if (password != null && !password.isBlank()) {
                hashedPassword = HashService.hashPassword(password);
            }

            Integer result = repository.editUserInfo(id, name, email, phone, hashedPassword, address, dateOfBirth, fileName);

            if (result == -1 || result == null) {
                return ResponseEntity.status(404).body(Map.of("info", "No user found for id: " + result));
            }

            Integer userId = RequestContext.getUserId();
            Integer userId2 = repository.getIdByEmail(email);

            if (Objects.equals(userId, userId2)) {
                return ResponseEntity.ok(Map.of("info", "User infos edited id: " + userId2));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

}

