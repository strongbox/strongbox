package org.carlspring.strongbox.io.filters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Kate Novik.
 */
public class MetadataFilenameFilter
        implements FilenameFilter
{

    public MetadataFilenameFilter()
    {
    }

    @Override
    public boolean accept(File dir,
                          String name)
    {
        return name.endsWith(".pom") || name.endsWith(".nuspec");
    }
}
