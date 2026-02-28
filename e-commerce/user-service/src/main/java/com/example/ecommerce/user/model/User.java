package com.example.ecommerce.user.model;

import com.example.ecommerce.common.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractBaseEntity {
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String firstName;
    private String lastName;
    private String phone;
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    
    public enum Role {
        USER, ADMIN
    }
}
