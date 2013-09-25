package org.carlspring.strongbox.event;

/**
 * @author mtodorov
 */
public class AuthenticationEvent
{

    public static final int EVENT_LOGIN_SUCCESSFUL = 1;

    public static final int EVENT_LOGIN_FAILED = 2;

    private int type;


    public AuthenticationEvent()
    {
    }

    public AuthenticationEvent(int type)
    {
        this.type = type;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

}
