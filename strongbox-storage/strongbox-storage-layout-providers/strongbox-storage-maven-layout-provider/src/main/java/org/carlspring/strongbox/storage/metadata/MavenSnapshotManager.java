package org.carlspring.strongbox.storage.metadata;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.providers.layout.LayoutProviderRegistry.getLayoutProvider;

/**
 * @author Kate Novik.
 */
@Component
public class MavenSnapshotManager
{

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd.HHmmss";

    private static final Logger logger = LoggerFactory.getLogger(MavenSnapshotManager.class);

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected StorageProviderRegistry storageProviderRegistry;

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    public MavenSnapshotManager()
    {
    }

    public void deleteTimestampedSnapshotArtifacts(RepositoryPath basePath,
                                                   Versioning versioning,
                                                   int numberToKeep,
                                                   int keepPeriod)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ParseException,
                   XmlPullParserException
    {
        Repository repository = basePath.getRepository();
        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        if (!layoutProvider.containsPath(basePath))
        {
            logger.error("Removal of timestamped Maven snapshot artifact: " + basePath + ".");
            
            return;
        }
        
        logger.debug("Removal of timestamped Maven snapshot artifact " + basePath +
                     " in '" + repository.getStorage()
                                         .getId() + ":" + repository.getId() + "'.");

        Pair<String, String> artifactGroup = MavenArtifactUtils.getArtifactGroupId(basePath);
        String artifactGroupId = artifactGroup.getValue0();
        String artifactId = artifactGroup.getValue1();

        if (versioning.getVersions()
                       .isEmpty())
        {
            return;
        }
        
        for (String version : versioning.getVersions())
        {

            RepositoryPath versionDirectoryPath = (RepositoryPath) basePath.resolve(ArtifactUtils.getSnapshotBaseVersion(version));
            if (!removeTimestampedSnapshot(versionDirectoryPath, numberToKeep, keepPeriod))
            {
                continue;
            }

            logger.debug("Generate snapshot versioning metadata for " + versionDirectoryPath + ".");

            mavenMetadataManager.generateSnapshotVersioningMetadata(artifactGroupId, artifactId, versionDirectoryPath,
                                                                    version,
                                                                    true);
        }

    }

    private boolean removeTimestampedSnapshot(RepositoryPath basePath,
                                              int numberToKeep,
                                              int keepPeriod)
            throws ProviderImplementationException,
                   IOException,
                   XmlPullParserException,
                   ParseException
    {
        Metadata metadata = mavenMetadataManager.readMetadata(basePath);

        if (metadata == null || metadata.getVersioning() == null)
        {
            return false;
        }
        
        /**
         * map of snapshots for removing
         * k - number of the build, v - version of the snapshot
         */
        Map<Integer, String> mapToRemove = getRemovableTimestampedSnapshots(metadata, numberToKeep, keepPeriod);

        if (mapToRemove.isEmpty())
        {
            return false;
        }
        List<String> removingSnapshots = new ArrayList<>();

        new ArrayList<>(mapToRemove.values()).forEach(e -> removingSnapshots.add(metadata.getArtifactId()
                                                                                         .concat("-")
                                                                                         .concat(e)
                                                                                         .concat(".jar")));

        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(basePath))
        {
            for (Path path : directoryStream)
            {
                if (!Files.isRegularFile(path))
                {
                    continue;
                }
                
                RepositoryPath repositoryPath = (RepositoryPath) path;
                final String filename = path.getFileName().toString();
                
                if (!removingSnapshots.contains(filename) || !RepositoryFiles.isArtifact(repositoryPath) || RepositoryFiles.isMetadata(repositoryPath))
                {
                    continue;
                }
                
                try
                {
                    RepositoryFiles.delete(repositoryPath, true);

                    RepositoryPath pomRepositoryPath = repositoryPath.resolveSibling(filename.replace(".jar", ".pom"));
                    
                    RepositoryFiles.delete(pomRepositoryPath,true);
                }
                catch (IOException ex)
                {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        return true;
    }

    /**
     * To get map of removable timestamped snapshots
     *
     * @param metadata     type Metadata
     * @param numberToKeep type int
     * @param keepPeriod   type int
     * @return type Map<Integer, String>
     * @throws IOException
     * @throws XmlPullParserException
     */
    private Map<Integer, String> getRemovableTimestampedSnapshots(Metadata metadata,
                                                                  int numberToKeep,
                                                                  int keepPeriod)
            throws IOException,
                   XmlPullParserException
    {
        /**
         * map of the snapshots in metadata file
         * k - number of the build, v - version of the snapshot
         */
        Map<Integer, String> snapshots = new HashMap<>();
        /**
         * map of snapshots for removing
         * k - number of the build, v - version of the snapshot
         */
        Map<Integer, String> mapToRemove = new HashMap<>();

        metadata.getVersioning()
                .getSnapshotVersions()
                .forEach(e ->
                         {

                             if ("jar".equals(e.getExtension()))
                             {
                                 String version = e.getVersion();
                                 snapshots.put(Integer.parseInt(ArtifactUtils.getSnapshotBuildNumber(version)),
                                               version);
                             }
                         });

        if (numberToKeep != 0 && snapshots.size() > numberToKeep)
        {
            snapshots.forEach((k, v) ->
                               {
                                   if (mapToRemove.size() < snapshots.size() - numberToKeep)
                                   {
                                       mapToRemove.put(k, v);
                                   }
                               });
        }
        else if (numberToKeep == 0 && keepPeriod != 0)
        {
            snapshots.forEach((k, v) ->
                               {
                                   try
                                   {
                                       if (keepPeriod < getDifferenceDays(ArtifactUtils.getSnapshotTimestamp(v)))
                                       {
                                           mapToRemove.put(k, v);
                                       }
                                   }
                                   catch (ParseException e)
                                   {
                                       logger.error(e.getMessage(), e);
                                   }
                               });
        }

        return mapToRemove;
    }

    /**
     * To get day's number of keeping timestamp snapshot
     * @param buildTimestamp type String
     * @return days type int
     * @throws ParseException
     */
    private int getDifferenceDays(String buildTimestamp)
            throws ParseException
    {
        DateFormat formatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
        Calendar calendar = Calendar.getInstance();

        String currentDate = formatter.format(calendar.getTime());

        Date d2 = formatter.parse(currentDate);
        Date d1 = formatter.parse(buildTimestamp);

        long diff = d2.getTime() - d1.getTime();

        return (int) diff / (24 * 60 * 60 * 1000);
    }

}
