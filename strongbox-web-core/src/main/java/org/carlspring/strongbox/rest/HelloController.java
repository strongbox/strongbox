package org.carlspring.strongbox.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/yury")
public class HelloController {

    @RequestMapping(value = "/storages/greet", method = RequestMethod.GET)
    public String greet() {

        System.out.println("++++++++++++++++");
        return "greet";
    }

}