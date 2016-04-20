package org.carlspring.strongbox.util;

import java.io.File;

/**
 * @author mtodorov
 */
public class FileUtils
{

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

}
