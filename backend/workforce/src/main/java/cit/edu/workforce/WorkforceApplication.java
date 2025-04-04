package cit.edu.workforce;

import cit.edu.workforce.Service.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkforceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkforceApplication.class, args);
	}

	@Bean
	public CommandLineRunner initRoles(RoleService roleService) {
		return args -> {
			// Initialize default roles
			roleService.initializeDefaultRoles();
		};
	}
}
