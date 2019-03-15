package org.carlspring.strongbox.storage.metadata.nuget;

import java.io.InputStream;

/**
 * This class purpose is to only expose access to its namespace resources
 * to avoid duplicating them across sub-namespaces.
 * 
 * @author kalski
 */
public class NugetTestResourceUtil
{

    public static InputStream getAsStream(String name)
    {
        return NugetTestResourceUtil.class.getResourceAsStream(name);
    }
}
