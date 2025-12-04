package MeadHead.Poc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import MeadHead.Poc.entites.UniteSoins;
import MeadHead.Poc.repository.UniteSoinsRepository;


@Service
public class UniteSoinsService {


    private UniteSoinsRepository uniteSoinsRepository;

    public UniteSoinsService(@Autowired UniteSoinsRepository uniteSoinsRepository){
        this.uniteSoinsRepository = uniteSoinsRepository;  
    }

     public void creer(UniteSoins uniteSoins){
        this.uniteSoinsRepository.save(uniteSoins);
    }

}
