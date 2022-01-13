package tk.andrei.medicalapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tk.andrei.medicalapp.entities.Role;
import tk.andrei.medicalapp.entities.User;
import tk.andrei.medicalapp.entities.dto.UserDTO;
import tk.andrei.medicalapp.repositories.RoleRepository;
import tk.andrei.medicalapp.repositories.UserRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service 
@RequiredArgsConstructor 
@Transactional
public class UserServiceImpl implements IUserService, UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserDTO user) {

        User userToRegister = new User();
        userToRegister.setEmail(user.getEmail());
        userToRegister.setPassword(user.getPassword());
        // encrypt the password
        userToRegister.setPassword(passwordEncoder.encode(userToRegister.getPassword()));
        userToRegister.setFirstName(user.getFirstName());
        userToRegister.setLastName(user.getLastName());
        return userRepository.save(userToRegister);
    }

    @Override
    public User getUser(String userEmail) {
        return userRepository.findUserByEmail(userEmail);
    }

    @Override
    public User getUser(UUID uuid) {
        Optional<User> user = userRepository.findById(uuid);
        return user.orElse(null);
    }

    @Override
    public Boolean deleteUser(String userEmail) {
        User userToDelete = userRepository.findUserByEmail(userEmail);

        if(userToDelete == null){
            throw new UsernameNotFoundException("User not found in the database");
        }
        userRepository.delete(userToDelete);
        return true;
    }

    @Override
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Boolean addRoleToUser(String userEmail, String roleName) {
        User user = userRepository.findUserByEmail(userEmail);
        Role role = roleRepository.findRoleByName(roleName);

        if (user != null && role != null) {
            user.getRoles().add(role);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public Boolean removeRoleFromUser(String userEmail, String roleName) {
        User user = userRepository.findUserByEmail(userEmail);
        Role role = roleRepository.findRoleByName(roleName);
        if (user != null && role != null) {
            Collection<Role> roles = user.getRoles();
            if (roles.contains(role)) {
                roles.remove(role);
                user.setRoles(roles);
                userRepository.save(user);
            }
            return true;
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found in the database");
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

    public User getCurrentUser() {
        // get the email used as username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal == null) {
            // we have no authenticated user
            return null;
        }

        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }

        return this.getUser(username);
    }
}
