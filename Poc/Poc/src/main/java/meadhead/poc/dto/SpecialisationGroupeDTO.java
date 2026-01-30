package meadhead.poc.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationGroupeDTO {

    private long id;
    private String nom;
    private List<SpecialisationOptionDTO> specialisations;

}
