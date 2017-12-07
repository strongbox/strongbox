package org.carlspring.strongbox.storage.metadata;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.providers.datastore.StorageProviderRegistry;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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

    public void deleteTimestampedSnapshotArtifacts(Repository repository,
                                                   String artifactPath,
                                                   VersionCollectionRequest request,
                                                   int numberToKeep,
                                                   int keepPeriod)
            throws IOException,
                   ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ParseException,
                   XmlPullParserException
    {
        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
        if (layoutProvider.containsPath(repository, artifactPath))
        {
            logger.debug("Removal of timestamped Maven snapshot artifact " + artifactPath +
                         " in '" + repository.getStorage()
                                             .getId() + ":" + repository.getId() + "'.");

            Versioning versioning = request.getVersioning();
            Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

            if (!versioning.getVersions()
                           .isEmpty())
            {
                for (String version : versioning.getVersions())
                {

                    Path versionBasePath = Paths.get(request.getArtifactBasePath()
                                                            .toString(),
                                                     ArtifactUtils.getSnapshotBaseVersion(version));

                    if (removeTimestampedSnapshot(versionBasePath.toString(), repository, numberToKeep, keepPeriod))
                    {

                        logger.debug("Generate snapshot versioning metadata for " + versionBasePath + ".");

                        mavenMetadataManager.generateSnapshotVersioningMetadata(versionBasePath,
                                                                                artifact,
                                                                                version,
                                                                                true);
                    }
                }
            }

        }
        else
        {
            logger.error("Removal of timestamped Maven snapshot artifact: " + artifactPath + ".");
        }
    }

    private boolean removeTimestampedSnapshot(String basePath,
                                              Repository repository,
                                              int numberToKeep,
                                              int keepPeriod)
            throws ProviderImplementationException,
                   IOException,
                   XmlPullParserException,
                   ParseException
    {
        String storageId = repository.getStorage()
                                     .getId();
        File file = new File(basePath);

        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);

        Metadata metadata = mavenMetadataManager.readMetadata(Paths.get(basePath));

        if (metadata != null && metadata.getVersioning() != null)
        {
            /**
             * map of snapshots for removing
             * k - number of the build, v - version of the snapshot
             */
            Map<Integer, String> mapToRemove = getRemovableTimestampedSnapshots(metadata, numberToKeep, keepPeriod);

            if (!mapToRemove.isEmpty())
            {
                List<String> removingSnapshots = new ArrayList<>();

                new ArrayList<>(mapToRemove.values()).forEach(e -> removingSnapshots.add(metadata.getArtifactId()
                                                                                                 .concat("-")
                                                                                                 .concat(e)
                                                                                                 .concat(".jar")));

                List<File> list = Arrays.asList(file.listFiles());

                list.stream()
                    .filter(File::isFile)
                    .filter(e -> ArtifactUtils.isArtifact(e.getPath()) && removingSnapshots.contains(e.getName()))
                    .forEach(e ->
                             {
                                 try
                                 {

                                     layoutProvider.delete(storageId, repository.getId(), e.getPath(), true);

                                     String artifactName = e.getName();
                                     artifactName = artifactName.substring(0, artifactName.lastIndexOf('.'));
                                     String pomPath = Paths.get(e.getParent(), artifactName)
                                                           .toString()
                                                           .concat(".pom");

                                     layoutProvider.delete(storageId, repository.getId(), pomPath, true);
                                 }
                                 catch (IOException ex)
                                 {
                                     logger.error(ex.getMessage(), ex);
                                 }
                                 catch (SearchException ex)
                                 {
                                     logger.error(ex.getMessage(), ex);
                                 }

                             });
                return true;
            }
        }

        return false;
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
