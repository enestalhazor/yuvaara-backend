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
    public ResponseEntity<?> register(@NotBlank(message = "Full name is required") @RequestParam String fullname,
                                      @NotBlank(message = "Email is required") @RequestParam String email,
                                      @NotBlank(message = "Phone is required") @RequestParam String phone,
                                      @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") @RequestParam String password,
                                      @NotBlank(message = "Address is required") @RequestParam String address,
                                      @RequestParam(value = "dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                      @RequestParam(value = "profilePictureUrl", required = false) MultipartFile profilePicUrl) throws IOException {

        try {
            String fileName = null;
            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {

                String contentType = profilePicUrl.getContentType();
                if (!contentType.startsWith("image/")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Please upload a valid image file"));
                }

                Path uploadDir = Paths.get(System.getProperty("user.dir"), "userphotos");
                Files.createDirectories(uploadDir);

                fileName = Paths.get(profilePicUrl.getOriginalFilename()).getFileName().toString();
                Path uploadPath = uploadDir.resolve(fileName);
                profilePicUrl.transferTo(uploadPath.toFile());
            }

            if (email != null && !email.isBlank() && repository.checkIsEmailTaken(email)) {
                return ResponseEntity.status(422).body(Map.of("info", "This email is already in use"));
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return ResponseEntity.status(400).body(Map.of("info", "Invalid email format"));
            }

            if (phone != null && !phone.isBlank()) {
                if (!phone.matches("^\\+905\\d{9}$")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Invalid phone format. Use +905XXXXXXXXX"));
                }
            }

            String hashedPassword = HashService.hashPassword(password);

            if (repository.save(fullname, email, phone, hashedPassword, address, fileName, dateOfBirth)) {
                return ResponseEntity.ok().body(Map.of("info", "Registration successful"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("info", "Something went wrong, please try again"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("info", "Something went wrong, please try again"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) throws NoSuchAlgorithmException {

        if (request.getPassword() == null || request.getPassword().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.status(400).body(Map.of("info", "Email and password are required"));
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

            return ResponseEntity.status(404).body(Map.of("info", "Invalid email or password"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
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
    public ResponseEntity<?> getUserInfo(@PathVariable Integer id) throws NoSuchAlgorithmException {

        if (!id.equals(RequestContext.getUserId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("info", "Unauthorized"));
        }

        try {

            var products = repository.userById(id);

            if (products == null || products.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("info", "User not found"));
            }

            Integer userid = RequestContext.getUserId();

            if (Objects.equals(userid, id)) {
                return ResponseEntity.ok(lowerCaseMap(products));
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editUserInfo(@PathVariable Integer id,
                                          @RequestParam(value = "fullname", required = false) String name,
                                          @RequestParam(value = "email", required = false) String email,
                                          @RequestParam(value = "phone", required = false) String phone,
                                          @RequestParam(value = "password", required = false) String password,
                                          @RequestParam(value = "address", required = false) String address,
                                          @RequestParam(value = "date_of_birth", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
                                          @RequestParam(value = "profilePictureUrl", required = false) MultipartFile profilePic) throws NoSuchAlgorithmException, IOException {

        if (!Objects.equals(id, RequestContext.getUserId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("info", "Unauthorized"));
        }

        boolean nothingToUpdate = (name == null || name.isBlank()) &&
                (email == null || email.isBlank()) &&
                (phone == null || phone.isBlank()) &&
                (password == null || password.isBlank()) &&
                (address == null || address.isBlank()) &&
                dateOfBirth == null &&
                (profilePic == null || profilePic.isEmpty());

        if (nothingToUpdate) {
            return ResponseEntity.ok(Map.of("info", "Nothing to update"));
        }

        try {
            String fileName = null;
            String hashedPassword = "";
            if (profilePic != null && !profilePic.isEmpty()) {

                String contentType = profilePic.getContentType();
                if (!contentType.startsWith("image/")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Please upload a valid image file"));
                }

                Path uploadDir = Paths.get(System.getProperty("user.dir"), "userphotos");
                Files.createDirectories(uploadDir);

                fileName = Paths.get(profilePic.getOriginalFilename()).getFileName().toString();
                Path uploadPath = uploadDir.resolve(fileName);
                profilePic.transferTo(uploadPath.toFile());
            }

            if (email != null && !email.isBlank()) {
                Integer ownerOfEmail = repository.getIdByEmail(email);
                if (ownerOfEmail != null && !Objects.equals(id, ownerOfEmail)) {
                    return ResponseEntity.status(409).body(Map.of("info", "This email is already in use"));
                }
            }

            if (email != null && !email.isBlank()) {
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Invalid email format"));
                }
            }

            if (password != null && !password.isBlank()) {
                if (password.length() < 6) {
                    return ResponseEntity.status(400).body(Map.of("info", "Password must be at least 6 characters"));
                }
            }

            if (phone != null && !phone.isBlank()) {
                if (!phone.matches("^\\+90 5\\d{2} \\d{3} \\d{4}$")) {
                    return ResponseEntity.status(400).body(Map.of("info", "Invalid phone format. Use +90 5xx xxx xxxx"));
                }
            }

            if (password != null && !password.isBlank()) {
                hashedPassword = HashService.hashPassword(password);
            }

            Integer result = repository.editUserInfo(id, name, email, phone, hashedPassword, address, dateOfBirth, fileName);

            if (result == -1 || result == null) {
                return ResponseEntity.status(404).body(Map.of("info", "User not found"));
            }

            return ResponseEntity.ok(Map.of("info", "Profile updated successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("info", "Something went wrong, please try again"));
        }
    }
}