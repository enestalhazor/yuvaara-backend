package com.example.yuvaarabackend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AdoptionFormsRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdoptionFormsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean addForm(String message, Integer userId, Integer adoptionListId) {

        String sql = "INSERT INTO adoption_forms (user_id, adoption_list_id, message) VALUES (?, ?, ?)";
        try {
            Integer count = jdbcTemplate.update(sql, userId, adoptionListId, message);
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> getForms(Integer userId) {

        String sql = "SELECT * FROM adoption_forms WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForList(sql, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean deleteFormById(Integer id, Integer userId) {
        try{
            String sql = "DELETE FROM adoption_forms WHERE id = ? AND user_id = ?";
            return jdbcTemplate.update(sql, id, userId) > 0;
        } catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}

