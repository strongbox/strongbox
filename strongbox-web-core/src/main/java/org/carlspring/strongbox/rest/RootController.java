package org.carlspring.strongbox.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class RootController
{

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity greet()
    {
        System.out.println("\n\n*******************************\n\n");
        System.out.println("[RootController] Greet from server!!! ->>>> 200 OK");
        return new ResponseEntity<>("root success", HttpStatus.OK);
    }
}
