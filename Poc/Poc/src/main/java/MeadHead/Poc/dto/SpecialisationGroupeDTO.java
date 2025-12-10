package MeadHead.Poc.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationGroupeDTO {

    private long id;   
    private String nom; 
    private List<SpecialisationOptionDTO> specialisations;

}