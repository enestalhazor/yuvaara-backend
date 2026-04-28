package com.example.yuvaarabackend;

import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class AdoptionListsRepository {
    private final JdbcTemplate jdbcTemplate;

    public AdoptionListsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean add(String name, String species, String breed, Integer age, String gender, String color, String status, String photo_url, String location
    ) {

        String sql = "INSERT INTO adoption_lists (name, species, breed, age, gender, color, status, photo_url, location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Integer rows = jdbcTemplate.update(sql, name, species, breed, age, gender, color, status, photo_url, location);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Map<String, Object>> filterLists(List<String> species, String age, String gender) {
        StringBuilder sql = new StringBuilder("SELECT * FROM adoption_lists WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (species != null && !species.isEmpty()) {
            sql.append(" AND species = ANY(?)");
            params.add(species.toArray(new String[0]));
        }

        if (age != null && !age.isEmpty()) {
            switch (age) {
                case "Puppy"  -> sql.append(" AND age BETWEEN 0 AND 1");
                case "Young"  -> sql.append(" AND age BETWEEN 1 AND 3");
                case "Adult"  -> sql.append(" AND age BETWEEN 3 AND 8");
                case "Senior" -> sql.append(" AND age > 8");
            }
        }

        if (gender != null && !gender.isEmpty()) {
            sql.append(" AND gender = ?");
            params.add(gender);
        }

        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
}

