package tk.andrei.medicalapp.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tk.andrei.medicalapp.entities.Role;
import tk.andrei.medicalapp.entities.User;
import tk.andrei.medicalapp.entities.dto.ManageRoleDTO;
import tk.andrei.medicalapp.entities.dto.UserDTO;
import tk.andrei.medicalapp.filter.CustomAuthorizationFilter;
import tk.andrei.medicalapp.services.JwtService;
import tk.andrei.medicalapp.services.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserServiceImpl userService;
    private final JwtService tokenService;

    @ApiOperation(value = "Register user", notes = "With this request you can register new user")
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserDTO user) {
        User existingUser = userService.getUser(user.getEmail());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }
        User savedUser = userService.createUser(user);
        userService.addRoleToUser(savedUser.getEmail(), Role.RoleEnum.ROLE_USER.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @ApiOperation(value = "Get user by email", notes = "With this request you can retrieve an user by email", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("/find")
    @ResponseBody
    public ResponseEntity<User> getUserByEmail(@RequestParam("email") String email) {
        User existingUser = userService.getUser(email);
        if (existingUser.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        existingUser.setPassword(null);
        return ResponseEntity.status(HttpStatus.OK).body(existingUser);
    }

    @ApiOperation(value = "Get user current user", notes = "With this request you can retrieve your own information", authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("/currentUser")
    @ResponseBody
    public ResponseEntity<User> getUserInformation() {
        User currentUser = userService.getCurrentUser();
        System.out.println(currentUser);
        currentUser.setPassword(null);
        return ResponseEntity.status(HttpStatus.OK).body(currentUser);
    }

    @ApiOperation(value = "Delete user", notes = "With this request you can delete an user", authorizations = {@Authorization(value = "Bearer")})
    @DeleteMapping("/delete")
    public ResponseEntity<String> removeUser(@RequestBody String userEmail) {
        Boolean success = userService.deleteUser(userEmail);
        if (success) {
            return ResponseEntity.status(HttpStatus.OK).body("User removed successfully");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Removing user failed.");
    }

    @ApiOperation(value = "Add role to user", notes = "With this request you can add a role to an user", authorizations = {@Authorization(value = "Bearer")})
    @PostMapping("/role/add")
    public ResponseEntity<String> addRoleToUser(@RequestBody ManageRoleDTO addRoleDTO) {
        Boolean success = userService.addRoleToUser(addRoleDTO.getUserEmail(), addRoleDTO.getRoleName());
        if (success) {
            return ResponseEntity.status(HttpStatus.OK).body("Role added successfully");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Adding role to user failed.");
    }

    @ApiOperation(value = "Delete user role", notes = "With this request you can delete a role from an user", authorizations = {@Authorization(value = "Bearer")})
    @DeleteMapping("/role/delete")
    public ResponseEntity<String> removeRoleFromUser(@RequestBody ManageRoleDTO manageRoleDTO) {
        Boolean success = userService.removeRoleFromUser(manageRoleDTO.getUserEmail(), manageRoleDTO.getRoleName());
        if (success) {
            return ResponseEntity.status(HttpStatus.OK).body("Role removed successfully");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Removing role from user failed.");
    }

    @ApiOperation(value = "Refresh Token", notes = "With this request you get the refresh token",authorizations = {@Authorization(value = "Bearer")})
    @GetMapping("/refresh-token")
    public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refresh_token = authorizationHeader.substring("Bearer ".length());
                DecodedJWT decodedJWT = tokenService.decodeToken(refresh_token);
                String username = tokenService.getUsername(decodedJWT);
                UserDetails user = userService.loadUserByUsername(username);
                if (user == null) {
                    throw new Exception("Wrong credentials");
                }
                String access_token = tokenService.createAccessToken(
                        user.getUsername(),
                        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())
                );

                new ObjectMapper().writeValue(response.getOutputStream(), tokenService.setTokens(access_token, refresh_token));

            } catch (Exception e) {
                e.printStackTrace();
                CustomAuthorizationFilter.authorizationFailedErrorResponse(response, e);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

}
