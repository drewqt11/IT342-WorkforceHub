package cit.edu.workforce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://aws-0-ap-southeast-1.pooler.supabase.com:5432/postgres?user=postgres.rystiuryfkxbcqjtdplv&password=[YOUR-PASSWORD]");
        dataSource.setUsername("postgres.rystiuryfkxbcqjtdplv");
        dataSource.setPassword("SupaBase_1234");
        return dataSource;
    }
}
