package org.carlspring.strongbox.locator.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.carlspring.strongbox.artifact.locator.handlers.AbstractArtifactLocationHandler;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.metadata.MavenSnapshotManager;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.carlspring.strongbox.storage.metadata.VersionCollector;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class RemoveTimestampedSnapshotOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(
            RemoveTimestampedSnapshotOperation.class);

    private RepositoryPath previousPath;

    private int numberToKeep;

    private int keepPeriod;

    private MavenSnapshotManager mavenSnapshotManager;


    public RemoveTimestampedSnapshotOperation()
    {
    }

    public RemoveTimestampedSnapshotOperation(MavenSnapshotManager mavenSnapshotManager)
    {
        this.mavenSnapshotManager = mavenSnapshotManager;
    }

    public void execute(RepositoryPath path) throws IOException
    {
//        File f = path.toAbsolutePath()
//                     .toFile();
//
//        String[] list = f.list(new PomFilenameFilter());
        
        List<Path> filePathList = Files.walk(path)
                .filter(p -> !p.getFileName().startsWith(".pom"))
                .sorted()
                .collect(Collectors.toList());
        
//        List<String> filePaths = list != null ? Arrays.asList(list) : new ArrayList<>();

        RepositoryPath parentPath = path.getParent()
                                .toAbsolutePath();

        if (filePathList.isEmpty())
        {
            return;
        }
        
        // Don't enter visited paths (i.e. version directories such as 1.2, 1.3, 1.4...)
        if (!getVisitedRootPaths().isEmpty() && getVisitedRootPaths().containsKey(parentPath))
        {
            List<RepositoryPath> visitedVersionPaths = getVisitedRootPaths().get(parentPath);

            if (visitedVersionPaths.contains(path))
            {
                return;
            }
        }

        if (logger.isDebugEnabled())
        {
            // We're using System.out.println() here for clarity and due to the length of the lines
            System.out.println(parentPath);
        }

        // The current directory is out of the tree
        if (previousPath != null && !parentPath.startsWith(previousPath))
        {
            getVisitedRootPaths().remove(previousPath);
            previousPath = parentPath;
        }

        if (previousPath == null)
        {
            previousPath = parentPath;
        }

        List<RepositoryPath> versionDirectories = getVersionDirectories(parentPath);
        if (versionDirectories != null)
        {
            getVisitedRootPaths().put(parentPath, versionDirectories);

            VersionCollector versionCollector = new VersionCollector();
            VersionCollectionRequest request = versionCollector.collectVersions(path.getParent()
                                                                                    .toAbsolutePath());

            if (logger.isDebugEnabled())
            {
                for (RepositoryPath directory : versionDirectories)
                {
                    // We're using System.out.println() here for clarity and due to the length of the lines
                    System.out.println(" " + directory.toAbsolutePath());
                }
            }
            
            RepositoryPath artifactPath = path.getRepositoryRelative();

//            String artifactPath = parentPath.substring(getRepository().getBasedir()
//                                                                      .length() + 1, parentPath.length());

            try
            {
                mavenSnapshotManager.deleteTimestampedSnapshotArtifacts(getRepository(), artifactPath.toString(), request,
                                                                        numberToKeep, keepPeriod);
            }
            catch (IOException |
                   ProviderImplementationException |
                   NoSuchAlgorithmException |
                   ParseException |
                   XmlPullParserException e)
            {
                logger.error("Failed to delete timestamped snapshot artifacts for " + artifactPath, e);
            }
        }
    }

    public MavenSnapshotManager getMavenSnapshotManager()
    {
        return mavenSnapshotManager;
    }

    public void setMavenSnapshotManager(MavenSnapshotManager mavenSnapshotManager)
    {
        this.mavenSnapshotManager = mavenSnapshotManager;
    }

    public int getNumberToKeep()
    {
        return numberToKeep;
    }

    public void setNumberToKeep(int numberToKeep)
    {
        this.numberToKeep = numberToKeep;
    }

    public int getKeepPeriod()
    {
        return keepPeriod;
    }

    public void setKeepPeriod(int keepPeriod)
    {
        this.keepPeriod = keepPeriod;
    }

}
