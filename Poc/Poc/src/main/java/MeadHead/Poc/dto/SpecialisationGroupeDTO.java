package MeadHead.Poc.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationGroupeDTO {

    private long id;   
    private String nom; 
    private List<SpecialisationOptionDTO> specialisations;

}