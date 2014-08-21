package org.carlspring.strongbox.storage.services.impl;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * @author stodorov
 */
public class ArtifactPomVisitor
        extends SimpleFileVisitor<Path>
{

    private PathMatcher matcher;
    public ArrayList<Path> foundPaths = new ArrayList<>();

    public ArtifactPomVisitor(){
        matcher = FileSystems.getDefault().getPathMatcher("glob:*.pom");

    }

    // Print information about
    // each type of file.
    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attr)
            throws IOException
    {

        Path name = file.getFileName();
        if (matcher.matches(name)) {
            foundPaths.add(file);
        }



/*        if (attr.isSymbolicLink())
        {
            System.out.format("Symbolic link: %s ", file);
        }
        else if (attr.isRegularFile())
        {
            System.out.format("Regular file: %s ", file);
        }
        else
        {
            System.out.format("Other: %s ", file);
        }
        System.out.println("(" + attr.size() + "bytes)");*/
        return FileVisitResult.CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                              IOException exc)
    {
        //System.out.format("Directory: %s%n", dir);
        return FileVisitResult.CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc)
    {
        System.err.println(exc);
        return FileVisitResult.CONTINUE;
    }
}
