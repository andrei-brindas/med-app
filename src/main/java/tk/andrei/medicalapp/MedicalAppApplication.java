package tk.andrei.medicalapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tk.andrei.medicalapp.entities.Role;
import tk.andrei.medicalapp.entities.dto.UserDTO;
import tk.andrei.medicalapp.services.UserServiceImpl;


@SpringBootApplication
public class MedicalAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicalAppApplication.class, args);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner run(UserServiceImpl userService) {
		return args -> {
			userService.saveRole(new Role(null, Role.RoleEnum.ROLE_USER.name()));
			userService.saveRole(new Role(null, Role.RoleEnum.ROLE_ADMIN.name()));
			userService.saveRole(new Role(null, Role.RoleEnum.ROLE_MANAGER.name()));

			userService.createUser(new UserDTO("user@user.com", "password", "User", "User"));
			userService.createUser(new UserDTO("manager@user.com", "password", "Manager", "Manager"));
			userService.createUser(new UserDTO("admin@user.com", "password", "Admin", "Admin"));
			userService.createUser(new UserDTO("john@user.com", "password", "John", "Doe"));


			userService.addRoleToUser("user@user.com", Role.RoleEnum.ROLE_USER.toString());
			userService.addRoleToUser("manager@user.com", Role.RoleEnum.ROLE_MANAGER.toString());
			userService.addRoleToUser("admin@user.com", Role.RoleEnum.ROLE_ADMIN.toString());
			userService.addRoleToUser("john@user.com", Role.RoleEnum.ROLE_MANAGER.toString());
		};
	}

}
