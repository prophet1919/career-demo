package com.wzu.demo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DbCheckController {

    private final JdbcTemplate jdbc;

    public DbCheckController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/db/ping")
    public Result<Map<String, Object>> ping() {
        String version = jdbc.queryForObject("SELECT VERSION()", String.class);
        return Result.ok(Map.of("mysql", version, "connected", true));
    }

    @GetMapping("/api/db/users")
    public Result<List<Map<String, Object>>> users() {
        return Result.ok(jdbc.queryForList("SELECT id, username, role, create_time FROM `user`"));
    }
}