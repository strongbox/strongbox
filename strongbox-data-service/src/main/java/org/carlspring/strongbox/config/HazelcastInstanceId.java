package org.carlspring.strongbox.config;

public class HazelcastInstanceId
{

    private final String instanceName;

    public HazelcastInstanceId(String instanceName)
    {
        super();
        this.instanceName = instanceName;
    }

    public String getInstanceName()
    {
        return instanceName;
    }

}
