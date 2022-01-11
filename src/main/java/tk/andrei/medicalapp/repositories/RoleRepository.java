package tk.andrei.medicalapp.repositories;

import org.springframework.data.repository.CrudRepository;
import tk.andrei.medicalapp.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Integer> {
    Role findRoleByName(String name);
}
