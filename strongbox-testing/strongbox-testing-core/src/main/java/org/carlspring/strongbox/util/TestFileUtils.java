package org.carlspring.strongbox.util;

import java.io.File;

/**
 * @author mtodorov
 */
public class TestFileUtils
{

    private TestFileUtils()
    {
    }

    public static void deleteIfExists(File file)
    {
        if (file.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

}
