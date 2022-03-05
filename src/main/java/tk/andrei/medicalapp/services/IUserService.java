package tk.andrei.medicalapp.services;



import tk.andrei.medicalapp.entities.Role;
import tk.andrei.medicalapp.entities.User;
import tk.andrei.medicalapp.entities.dto.UserDTO;

import java.util.UUID;

public interface IUserService {
    User createUser(UserDTO user);
    User getUser(String userEmail);
    User getUser(UUID userid);
    Boolean deleteUser(String email);
    void saveRole(Role role);
    Boolean addRoleToUser(String userEmail, String roleName);
    Boolean removeRoleFromUser(String userEmail, String roleName);
}
