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
import java.util.*;

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
        userToRegister.setPassword(passwordEncoder.encode(userToRegister.getPassword()));
        userToRegister.setFirstName(user.getFirstName());
        userToRegister.setLastName(user.getLastName());
        return userRepository.save(userToRegister);
    }

    @Override
    public User getUser(String userEmail) {
        return userRepository.findUserByEmail(userEmail).orElse(null);
    }

    @Override
    public User getUser(UUID uuid) {
        return userRepository.findById(uuid).orElse(null);
    }

    @Override
    public Boolean deleteUser(String userEmail) {
        Optional<User> userToDeleteOptional = userRepository.findUserByEmail(userEmail);
        if (userToDeleteOptional.isPresent()) {
            userRepository.delete(userToDeleteOptional.get());
            return true;
        }
        return false;
    }

    @Override
    public void saveRole(Role role) {
        roleRepository.save(role);
    }

    @Override
    public Boolean addRoleToUser(String userEmail, String roleName) {
        Optional<User> userOptional = userRepository.findUserByEmail(userEmail);
        Optional<Role> roleOptional = Optional.of(roleRepository.findRoleByName(roleName));
        if (userOptional.isPresent()) {
            userOptional.get().getRoles().add(roleOptional.get());
            userRepository.save(userOptional.get());
            return true;
        }
        return false;
    }

    @Override
    public Boolean removeRoleFromUser(String userEmail, String roleName) {
        Optional<User> userOptional = userRepository.findUserByEmail(userEmail);
        Optional<Role> roleOptional = Optional.of(roleRepository.findRoleByName(roleName));
        Collection<Role> roles;
        if (userOptional.isPresent()) {
            roles = userOptional.get().getRoles();
            if (roles.contains(roleOptional.get())) {
                roles.remove(roleOptional.get());
                userOptional.get().setRoles(roles);
                userRepository.save(userOptional.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findUserByEmail(username);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (userOptional.isPresent()) {
            userOptional.get().getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
            return new org.springframework.security.core.userdetails.User(userOptional.get().getEmail(), userOptional.get().getPassword(), authorities);
        }
        return null;
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
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return this.getUser(username);
    }
}
