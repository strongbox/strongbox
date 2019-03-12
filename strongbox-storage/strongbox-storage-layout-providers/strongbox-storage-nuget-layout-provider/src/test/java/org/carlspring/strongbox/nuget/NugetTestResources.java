package org.carlspring.strongbox.nuget;

import java.io.InputStream;

/**
 * This class purpose is to only expose access to its namespace resources
 * to avoid duplicating them across sub-namespaces.
 * 
 * @author kalski
 */
public class NugetTestResources
{

    public static InputStream getAsStream(String name)
    {
        return NugetTestResources.class.getResourceAsStream(name);
    }
}
