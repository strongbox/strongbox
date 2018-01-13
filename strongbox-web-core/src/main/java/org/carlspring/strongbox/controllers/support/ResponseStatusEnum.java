package org.carlspring.strongbox.controllers.support;

/**
 * @author Pablo Tirado
 */
public enum ResponseStatusEnum
{
    OK("OK"),
    FAILED("FAILED");

    private final String value;

    ResponseStatusEnum(String value)
    {
        this.value = value;
    }

    public String value()
    {
        return this.value;
    }
}