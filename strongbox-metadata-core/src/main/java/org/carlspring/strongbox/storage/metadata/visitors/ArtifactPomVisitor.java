package org.carlspring.strongbox.storage.metadata.visitors;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * @author stodorov
 */
public class ArtifactPomVisitor
        extends SimpleFileVisitor<Path>
{

    private static final String SEARCH_PATTERN = "glob:*.pom";

    private List<Path> matchingPaths = new ArrayList<>();

    private PathMatcher matcher;


    public ArtifactPomVisitor()
    {
        matcher = FileSystems.getDefault().getPathMatcher(SEARCH_PATTERN);
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

    public List<Path> getMatchingPaths(){
        return matchingPaths;
    }

}
