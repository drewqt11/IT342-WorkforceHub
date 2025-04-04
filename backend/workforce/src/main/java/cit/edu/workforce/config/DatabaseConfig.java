package cit.edu.workforce.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres");
        dataSource.setUsername("postgres.rystiuryfkxbcqjtdplv");
        dataSource.setPassword("");
        return dataSource;
    }
}
