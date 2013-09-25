package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public interface AuthenticationEventListener
{

    public void handle(AuthenticationEvent event);

}
