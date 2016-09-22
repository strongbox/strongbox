package org.carlspring.strongbox.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Component
public class RootController
        extends BaseArtifactController {

    private static final Logger logger = LogManager.getLogger(RootController.class.getName());


    @PreAuthorize("authenticated")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity greet() {
        System.out.println("inside greet");
        return new ResponseEntity<>("root success", HttpStatus.OK);
    }
}
