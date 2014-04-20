package org.carlspring.strongbox.client;

/**
 * @author mtodorov
 */
public class ResponseException extends Exception
{

    private int statusCode;


    public ResponseException(String message, int statusCode)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

}
