package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepositoryDto;
import org.carlspring.strongbox.testing.artifact.MavenArtifactTestUtils;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
public class MavenTestCaseWithArtifactGeneration
        extends TestCaseWithRepositoryManagement
{
    private final Logger logger = LoggerFactory.getLogger(MavenTestCaseWithArtifactGeneration.class);

    @Inject
    protected MavenRepositoryFeatures features;
    
    @Inject
    protected HostedRepositoryProvider hostedRepositoryProvider;
    
    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected MavenRepositoryFactory mavenRepositoryFactory;

    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected ConfigurationManager configurationManager;


    public MavenArtifact generateArtifact(String basedir, String gavtc)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        return generateArtifact(Paths.get(basedir), gavtc);
    }

    public MavenArtifact generateArtifact(Path basedir, String gavtc)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        MavenArtifact artifact = MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc);
        artifact.setPath(resolve(basedir.toAbsolutePath().toString(), artifact));
        generateArtifact(basedir, artifact);

        return artifact;
    }

    public void generateArtifact(Path basedir, MavenArtifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        
        artifact.setPath(resolve(basedir.toAbsolutePath().toString(), artifact));

        MavenArtifactGenerator generator = createArtifactGenerator(basedir.toAbsolutePath().toString());
        generator.generate(artifact);
    }

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

    public void generateArtifact(String basedir, String ga, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(ga, versions);
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
            String version = createSnapshotVersion(baseSnapshotVersion, i + 1);

            artifact = new MavenRepositoryArtifact(groupId, artifactId, version, packaging);

            RepositoryPath repositoryPath = resolve(repositoryBasedir, artifact);
            artifact.setPath(repositoryPath);

            generateArtifact(repositoryBasedir, artifact, packaging);

            if (classifiers != null)
            {
                for (String classifier : classifiers)
                {
                    String gavtc = groupId + ":" + artifactId + ":" + version + ":jar:" + classifier;
                    generateArtifact(repositoryBasedir,MavenArtifactTestUtils.getArtifactFromGAVTC(gavtc));
                }
            }
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

    public void createTimestampedSnapshot(String repositoryBasedir,
                                          String groupId,
                                          String artifactId,
                                          String baseSnapshotVersion,
                                          String packaging,
                                          String[] classifiers,
                                          int numberOfBuild,
                                          String timestamp)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        String version = createSnapshotVersion(baseSnapshotVersion, numberOfBuild, timestamp);

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
    }

    public String createSnapshotVersion(String baseSnapshotVersion, int buildNumber)
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

    public void changeCreationDate(MavenArtifact artifact)
            throws IOException
    {
        Path directory = artifact.getPath().getParent();

        try (Stream<Path> pathStream = Files.walk(directory))
        {
            pathStream.filter(Files::isRegularFile).forEach(
                    filePath -> {
                        BasicFileAttributeView attributes = Files.getFileAttributeView(filePath,
                                                                                       BasicFileAttributeView.class);
                        FileTime time = FileTime.from(System.currentTimeMillis() + 60000L, TimeUnit.MILLISECONDS);
                        try
                        {
                            attributes.setTimes(time, time, time);
                        }
                        catch (IOException e)
                        {
                            logger.error(
                                    String.format("Failed to change creation date for [%s]", filePath),
                                    e);
                        }
                    });
        }
    }

    public File getRepositoryBasedir(String storageId, String repositoryId)
    {
        return Paths.get(propertiesBooter.getVaultDirectory(), "storages", storageId, repositoryId).toFile();
    }

    public MavenRepositoryFeatures getFeatures()
    {
        return features;
    }

    @Override
    public void createProxyRepository(String storageId,
                                      String repositoryId,
                                      String remoteRepositoryUrl)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException
    {
        RemoteRepositoryDto remoteRepository = new RemoteRepositoryDto();
        remoteRepository.setUrl(remoteRepositoryUrl);

        RepositoryDto repository = mavenRepositoryFactory.createRepository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        createRepository(storageId, repository);
    }

}
