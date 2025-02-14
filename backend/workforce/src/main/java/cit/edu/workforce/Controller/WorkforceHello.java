package cit.edu.workforce.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkforceHello {
   
    @GetMapping("/")

    public String print() {
        return "Hello Workforce";
    }

}

