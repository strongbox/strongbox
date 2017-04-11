package org.carlspring.strongbox.io;

import org.carlspring.maven.commons.util.ArtifactUtils;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author carlspring
 */
public class ArtifactFilenameFilter
        implements FilenameFilter
{

    public ArtifactFilenameFilter()
    {
    }

    @Override
    public boolean accept(File dir, String name)
    {
        return !(new File(dir, name).isDirectory()) &&
               !name.endsWith(".pom") &&
               !name.equals("maven-metadata.xml") &&
               !ArtifactUtils.isChecksum(name);
    }

}


