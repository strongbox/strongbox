package org.carlspring.strongbox.storage.visitors;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stodorov
 */
public class ArtifactPomVisitor
        extends SimpleFileVisitor<Path>
{

    private PathMatcher matcher;

    public List<Path> matchingPaths = new ArrayList<>();


    public ArtifactPomVisitor()
    {
        matcher = FileSystems.getDefault().getPathMatcher("glob:*.pom");
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr)
            throws IOException
    {
        Path name = file.getFileName();
        if (matcher.matches(name))
        {
            matchingPaths.add(file);
        }

        return FileVisitResult.CONTINUE;
    }

}
