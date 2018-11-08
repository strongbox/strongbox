package org.carlspring.strongbox.controllers;

import io.swagger.annotations.Api;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Steve Todorov
 */
@Controller
@RequestMapping(path = { "/", "/**" })
@Api(value = "/")
public class HomepageController
        extends BaseArtifactController
{

    @GetMapping(produces = { MediaType.TEXT_HTML_VALUE })
    public String homepage()
    {
        return "/index.html";
    }

}
