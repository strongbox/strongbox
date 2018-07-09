package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.providers.io.RepositoryPath;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;

/**
 * @author Przemyslaw Fusik
 */
public enum JarArchiveListingFunction
        implements ArchiveListingFunction
{
    INSTANCE;

    @Override
    public Set<String> listFilenames(final RepositoryPath path)
            throws IOException
    {
        try (InputStream is = Files.newInputStream(path);
             BufferedInputStream bis = new BufferedInputStream(is);
             ArchiveInputStream ais = new JarArchiveInputStream(bis))
        {
            return getEntriesNames(ais);
        }
    }

    @Override
    public boolean supports(final RepositoryPath path)
    {
        final String filename = path.getFileName().toString();
        return filename.endsWith(".jar") || filename.endsWith(".war") || filename.endsWith(".ear");
    }
}
