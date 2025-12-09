package MeadHead.Poc.entites;

import MeadHead.Poc.enums.TypeDeRole;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;   
import lombok.Getter;   
import lombok.NoArgsConstructor;    
import lombok.Setter;   
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;  
import jakarta.persistence.GenerationType;  
import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "role_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeDeRole roleType;

}
