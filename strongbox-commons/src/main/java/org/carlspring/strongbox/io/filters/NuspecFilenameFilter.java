package org.carlspring.strongbox.io.filters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Kate Novik.
 */
public class NuspecFilenameFilter
        implements FilenameFilter
{

    public NuspecFilenameFilter()
    {
    }

    @Override
    public boolean accept(File dir,
                          String name)
    {
        return name.endsWith(".nuspec");
    }

}

