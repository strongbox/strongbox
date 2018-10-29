package org.carlspring.strongbox.storage.routing;

/**
 * @author mtodorov
 */
public enum RoutingRuleTypeEnum
{

    ACCEPT("accept"),

    DENY("deny");


    private String type;


    RoutingRuleTypeEnum(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }

}
