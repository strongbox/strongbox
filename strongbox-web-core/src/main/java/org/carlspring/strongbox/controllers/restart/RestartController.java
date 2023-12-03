package org.carlspring.strongbox.controllers.restart;

import io.swagger.annotations.Api;
import org.carlspring.strongbox.app.StrongboxSpringBootApplication;
import org.carlspring.strongbox.controllers.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.carlspring.strongbox.controllers.restart.RestartController.MAPPING;


/**
 * @author: adavid9
 */
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = MAPPING)
public class RestartController
        extends BaseController
{

    public static final String MAPPING = "/api";

    private static final Logger logger = LoggerFactory.getLogger(RestartController.class);

    @PostMapping("/restart")
    public void restart()
    {
        StrongboxSpringBootApplication.restart();
        logger.info("Restarting strongbox application.");
    }
}
