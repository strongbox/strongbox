package org.carlspring.strongbox.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({ "org.carlspring.strongbox.users.security" })
public class UsersSecurityConfig
{

}
