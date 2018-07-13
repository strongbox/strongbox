package org.carlspring.strongbox.controllers;

import java.io.IOException;

import io.swagger.annotations.Api;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Steve Todorov
 */
@Controller
@RequestMapping("/")
@Api(value = "/")
public class HomepageController
        extends BaseArtifactController
{

    @GetMapping(produces = { MediaType.TEXT_HTML_VALUE })
    public String homepage(@RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws IOException
    {
        return "index.html";
    }

}
