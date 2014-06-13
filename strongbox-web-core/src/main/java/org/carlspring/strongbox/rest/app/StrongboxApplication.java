package org.carlspring.strongbox.rest.app;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

public class StrongboxApplication extends ResourceConfig {

    public StrongboxApplication() {
        register(new LoggingFilter());
    }

}
