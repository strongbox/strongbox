package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
public class MavenTestCaseWithArtifactGeneration
{
    @Inject
    protected MavenRepositoryFeatures features;
    
    @Inject
    protected HostedRepositoryProvider hostedRepositoryProvider;
    
    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected ConfigurationManager configurationManager;


    public void generateArtifact(String basedir, Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(artifact);
    }

    public void generateArtifact(String basedir, Artifact artifact, String packaging)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(artifact, packaging);
    }

    protected MavenArtifactGenerator createArtifactGenerator(String basedir)
    {
        return new MavenArtifactGenerator(basedir)
        {

            @Override
            protected OutputStream newOutputStream(File artifactFile)
                throws IOException
            {
                Path basePath = basedir;
                Path artifactPath = artifactFile.toPath();

                String path = FilenameUtils.separatorsToUnix(basePath.relativize(artifactPath).toString());

                Path repositoryBasePath = basePath;
                if (repositoryBasePath.endsWith(LayoutFileSystem.TEMP) ||
                    repositoryBasePath.endsWith(LayoutFileSystem.TEMP))
                {
                    repositoryBasePath = repositoryBasePath.getParent();
                }
                String storageId = repositoryBasePath.getParent().getFileName().toString();
                String repositoryId = repositoryBasePath.getFileName().toString();

                RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);

                if (basePath.endsWith(LayoutFileSystem.TEMP))
                {
                    repositoryPath = RepositoryFiles.temporary(repositoryPath);
                }
                else if (basePath.endsWith(LayoutFileSystem.TRASH))
                {
                    repositoryPath = RepositoryFiles.trash(repositoryPath);
                }
                
                return hostedRepositoryProvider.getOutputStream(repositoryPath);
            }
            
        };
    }

    public MavenArtifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                           String groupId,
                                                           String artifactId,
                                                           String baseSnapshotVersion,
                                                           String packaging,
                                                           String[] classifiers,
                                                           int numberOfBuilds)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        MavenArtifact artifact = null;

        for (int i = 0; i < numberOfBuilds; i++)
        {
            artifact = createTimestampedSnapshot(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 packaging,
                                                 classifiers,
                                                 i + 1,
                                                 null);
        }

        // Return the main artifact
        return artifact;
    }

    protected RepositoryPath resolve(String repositoryBasedir,
                                     Artifact artifact)
    {
        Path repositoryBasePath = Paths.get(repositoryBasedir);
        if (repositoryBasePath.endsWith(LayoutFileSystem.TEMP)
                || repositoryBasePath.endsWith(LayoutFileSystem.TRASH))
        {
            repositoryBasePath = repositoryBasePath.getParent();
        }
        
        String repositoryId = repositoryBasePath.getFileName().toString();
        String storageId = repositoryBasePath.getParent().getFileName().toString();
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId,
                                                                       repositoryId,
                                                                       MavenArtifactUtils.convertArtifactToPath(artifact));
        return repositoryPath;
    }

    public MavenArtifact createTimestampedSnapshot(String repositoryBasedir,
                                                   String groupId,
                                                   String artifactId,
                                                   String baseSnapshotVersion,
                                                   String packaging,
                                                   String[] classifiers,
                                                   int numberOfBuild,
                                                   String timestamp)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        String version;
        if (timestamp != null)
        {
            version = createSnapshotVersion(baseSnapshotVersion, numberOfBuild, timestamp);
        }
        else
        {
            version = createSnapshotVersion(baseSnapshotVersion, numberOfBuild);
        }

        MavenArtifact artifact = new MavenRepositoryArtifact(groupId, artifactId, version, packaging);
        RepositoryPath repositoryPath = resolve(repositoryBasedir, artifact);
        artifact.setPath(repositoryPath);

        generateArtifact(repositoryBasedir, artifact, packaging);

        if (classifiers != null)
        {
            for (String classifier : classifiers)
            {
                String gavtc = groupId + ":" + artifactId + ":" + version + ":jar:" + classifier;
                generateArtifact(repositoryBasedir, MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc));
            }
        }

        return artifact;
    }

    public String createSnapshotVersion(String baseSnapshotVersion,
                                        int buildNumber)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, 7);
        calendar.add(Calendar.MINUTE, 5);

        String timestamp = formatter.format(calendar.getTime());
        @SuppressWarnings("UnnecessaryLocalVariable")
        String version = baseSnapshotVersion + "-" + timestamp + "-" + buildNumber;

        return version;
    }

    public String createSnapshotVersion(String baseSnapshotVersion,
                                        int buildNumber,
                                        String timestamp)
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String version = baseSnapshotVersion + "-" + timestamp + "-" + buildNumber;

        return version;
    }

    public MavenRepositoryFeatures getFeatures()
    {
        return features;
    }

}
