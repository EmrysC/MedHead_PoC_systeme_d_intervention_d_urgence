

package MeadHead.MedHead_PoC.poc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "test")
public class TestController {

    @GetMapping(path = "string")
    public String getString(){
        return "Test premier get";
    }

}
