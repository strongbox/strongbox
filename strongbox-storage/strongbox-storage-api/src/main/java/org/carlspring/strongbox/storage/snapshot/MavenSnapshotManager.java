package org.carlspring.strongbox.storage.snapshot;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
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
            throws IOException, ProviderImplementationException, NoSuchAlgorithmException
    {
//        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
//        if (layoutProvider.containsPath(repository, artifactPath))
//        {
//            logger.debug("Removal of timestamped Maven snapshot artifact " + artifactPath +
//                         " in '" + repository.getStorage()
//                                             .getId() + ":" + repository.getId() + "'.");
//            Versioning versioning = request.getVersioning();
//            Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);
//
//            if (!versioning.getVersions()
//                           .isEmpty())
//            {
//                for (String version : versioning.getVersions())
//                {
//
//                    Path versionBasePath = Paths.get(request.getArtifactBasePath()
//                                                            .toString(),
//                                                     ArtifactUtils.getSnapshotBaseVersion(version));
//
//                    removeTimestampedSnapshot(versionBasePath.toString(), repository, numberToKeep, keepPeriod);
//
//                    mavenMetadataManager.generateSnapshotVersioningMetadata(versionBasePath, artifact, version, true);
//
//                    logger.debug("Generated Maven checksum for " + versionBasePath + ".");
//                }
//            }
//
//        }
//        else
//        {
//            logger.error("Removal of timestamped Maven snapshot artifact: " + artifactPath + ".");
//        }

    }

//    private void removeTimestampedSnapshot (String basePath, Repository repository, int numberToKeep, int keepPeriod)
//            throws ProviderImplementationException, IOException, XmlPullParserException
//    {
//        String storageId = repository.getStorage().getId();
//        File file = new File(basePath);
//
//        LayoutProvider layoutProvider = getLayoutProvider(repository, layoutProviderRegistry);
//
//        Metadata metadata = mavenMetadataManager.readMetadata(Paths.get(basePath));
//        int numberToRemove;
//
//        if (metadata != null && metadata.getVersioning() != null)
//        {
//            Snapshot snapshot = metadata.getVersioning().getSnapshot();
//
//            if (numberToKeep != 0 && numberToKeep < snapshot.getBuildNumber()) {
//                numberToRemove = snapshot.getBuildNumber() - numberToKeep;
//            }
//            else if (numberToKeep == 0 && keepPeriod != 0)
//            {
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");
//                Calendar calendar = Calendar.getInstance();
//                Date buildDate = formatter.(calendar.getTime())
//            }
//            metadata.getVersioning()
//                    .getVersions()
//                    .remove(version);
//            mavenMetadataManager.storeMetadata(path, null, metadata, MetadataType.ARTIFACT_ROOT_LEVEL);
//        }
//
//        List<File> list = Arrays.asList(file.listFiles());
//
//        list.stream().filter(File::isFile)
//                     .filter(e -> ArtifactUtils.isArtifact(e.getPath()))
//                     .forEach(e ->
//                                        {
//                                            if (allowDeletingTimestampedSnapshot(e, numberToKeep, keepPeriod))
//                                            {
//                                                try
//                                                {
//
//                                                    layoutProvider.delete(storageId, repository.getId(), e.getPath(), true);
//
//                                                    String artifactName = e.getName();
//                                                    artifactName = artifactName.substring(0, artifactName.lastIndexOf("."));
//                                                    String pomPath = Paths.get(e.getParent(), artifactName).toString().concat(".pom");
//
//                                                    layoutProvider.delete(storageId, repository.getId(), pomPath, true);
//                                                }
//                                                catch (IOException e1)
//                                                {
//                                                    logger.error(e1.getMessage(), e1);
//                                                }
//
//                                            }
//                                        });
//
//    }
//
//    private boolean allowDeletingTimestampedSnapshot (File artifactFile, int numberToKeep, int keepPeriod)
//    {
//        Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactFile.getPath());
//
//        Metadata metadata = mavenMetadataManager.readMetadata()
//        return true;
//    }

}
