package org.carlspring.strongbox.controllers.raw;

import org.carlspring.strongbox.controllers.BaseArtifactController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author carlspring
 */
@RestController
@RequestMapping(path = RawArtifactController.ROOT_CONTEXT, headers = "user-agent=Raw/*")
public class RawArtifactController
        extends BaseArtifactController
{

    private static final Logger logger = LoggerFactory.getLogger(RawArtifactController.class);

    // must be the same as @RequestMapping value on the class definition
    public final static String ROOT_CONTEXT = "/storages";


}
