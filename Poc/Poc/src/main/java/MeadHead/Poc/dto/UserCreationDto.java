
package MeadHead.Poc.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationDto {
    

    private String email;
    private String password;
    private String nom;
    private String prenom;
}