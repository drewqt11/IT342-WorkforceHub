package cit.edu.workforce.controller;

import cit.edu.workforce.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/test-connection")
    public String testConnection() {
        List<Map<String, Object>> data = databaseService.getData();
        data.forEach(row -> System.out.println(row));
        return "Database Connection Successful!";
    }
}