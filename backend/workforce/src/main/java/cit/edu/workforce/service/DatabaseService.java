package cit.edu.workforce.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getData() {
        String sql = "SELECT * FROM user_account";
        return jdbcTemplate.queryForList(sql);
    }
}
