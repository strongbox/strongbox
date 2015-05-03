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

    private static final String search = "glob:*.{md5,sha1}";

    public List<Path> matchingPaths = new ArrayList<>();

    private PathMatcher matcher;


    public ArtifactVersionDirectoryVisitor()
    {
        matcher = FileSystems.getDefault().getPathMatcher(search);
    }

    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr)
            throws IOException
    {
        Path name = file.getFileName();
        if (!matcher.matches(name))
        {
            matchingPaths.add(file);
        }

        return FileVisitResult.CONTINUE;
    }

    public List<Path> getMatchingPaths(){
        return matchingPaths;
    }

}
