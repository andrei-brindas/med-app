package tk.andrei.medicalapp.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    public enum RoleEnum {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_MANAGER
    }

    @Id
    @GeneratedValue
    private Integer id;
    @Column(unique = true)
    private String name;
}
