package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public interface AuthenticationEventListener
{

    void handle(AuthenticationEvent event);

}
