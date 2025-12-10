
package MeadHead.Poc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationDTO {
    

    private String email;
    private String password;
    private String nom;
    private String prenom;
}