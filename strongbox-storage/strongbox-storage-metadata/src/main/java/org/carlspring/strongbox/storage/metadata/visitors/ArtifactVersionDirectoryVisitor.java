package org.carlspring.strongbox.storage.metadata.visitors;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stodorov
 */
public class ArtifactVersionDirectoryVisitor
        extends SimpleFileVisitor<Path>
{

    private static final String CHECKSUM_PATTERN = "glob:*.{md5,sha1}";
    private static final String METADATA_PATTERN = "glob:maven-metadata.*";

    public List<Path> matchingPaths = new ArrayList<>();

    private PathMatcher checksumFileMatcher;
    private PathMatcher metadataFileMatcher;

    public ArtifactVersionDirectoryVisitor()
    {
        checksumFileMatcher = FileSystems.getDefault().getPathMatcher(CHECKSUM_PATTERN);
        metadataFileMatcher = FileSystems.getDefault().getPathMatcher(METADATA_PATTERN);
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr)
            throws IOException
    {
        Path name = file.getFileName();
        if (!checksumFileMatcher.matches(name) && !metadataFileMatcher.matches(name))
        {
            matchingPaths.add(file);
        }

        return FileVisitResult.CONTINUE;
    }

    public List<Path> getMatchingPaths(){
        return matchingPaths;
    }

}
