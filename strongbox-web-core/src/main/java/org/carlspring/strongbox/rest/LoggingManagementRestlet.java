package org.carlspring.strongbox.rest;

import org.carlspring.logging.rest.AbstractLoggingManagementRestlet;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;

/**
 * @author carlspring
 */
@Component
@Path("/logging")
public class LoggingManagementRestlet extends AbstractLoggingManagementRestlet
{

}
