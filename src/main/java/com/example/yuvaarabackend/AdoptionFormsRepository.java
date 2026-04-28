package com.example.yuvaarabackend;

import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AdoptionFormsRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdoptionFormsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean add(String message, Integer user_id, Integer adoption_list_id) {

        String sql = "INSERT INTO adoption_forms (user_id, adoption_list_id, message) VALUES (?, ?, ?)";
        try {
            Integer count = jdbcTemplate.update(sql, user_id, adoption_list_id, message);
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Map<String, Object>> forms() {

        String sql = "SELECT * FROM products";
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean deleteFormById(Integer id) {
        String sql = "DELETE FROM products WHERE id=?";
        try {
            return jdbcTemplate.update(sql, id) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

