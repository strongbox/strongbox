package org.carlspring.strongbox.util;

import java.io.File;

/**
 * @author mtodorov
 */
public class FileUtils
{

    private FileUtils() 
    {
    }

    public static String normalizePath(String path)
    {
        if (path.contains("\\") && !File.separator.equals("\\"))
        {
            path = path.replaceAll("\\\\", "/");
        }
        if (path.contains("/") && !File.separator.equals("/"))
        {
            path = path.replaceAll("/", "\\\\");
        }

        return path;
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
