package tk.andrei.medicalapp.repositories;


import org.springframework.data.repository.CrudRepository;
import tk.andrei.medicalapp.entities.User;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, UUID> {
    User findUserByEmail(String email);
}
