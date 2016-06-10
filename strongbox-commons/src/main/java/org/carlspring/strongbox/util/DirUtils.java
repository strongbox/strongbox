package org.carlspring.strongbox.util;

import java.io.File;
import java.io.IOException;

/**
 * @author mtodorov
 */
public class DirUtils
{


    private DirUtils() 
    {
    }

    public static void removeEmptyAncestors(String path, String stopAtPath)
            throws IOException
    {
        File dir = new File(path);
        if (stopAtPath != null && dir.getName().equals(stopAtPath))
        {
            return;
        }

        if (dir.list().length == 0)
        {
            org.apache.commons.io.FileUtils.deleteDirectory(dir);
            removeEmptyAncestors(dir.getParentFile().getAbsolutePath(), stopAtPath);
        }
    }

}
