package org.carlspring.strongbox.io;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author carlspring
 */
public class ArtifactFilenameFilter
        implements FilenameFilter
{

    public static final String EXTENSION_POM = ".pom";


    public ArtifactFilenameFilter()
    {
    }

    @Override
    public boolean accept(File dir,
                          String name)
    {
        return !(new File(dir, name).isDirectory()) &&
               !EXTENSION_POM.endsWith(name) &&
               !MetadataHelper.MAVEN_METADATA_XML.equals(name) &&
               !ArtifactUtils.isChecksum(name);
    }

}


