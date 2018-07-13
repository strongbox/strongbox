package org.carlspring.strongbox.testing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.RepositoryStreamSupport.RepositoryOutputStream;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author mtodorov
 */
public class MavenTestCaseWithArtifactGeneration
        extends TestCaseWithRepositoryManagement
{

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected MavenRepositoryFeatures features;
    
    @Inject
    protected HostedRepositoryProvider hostedRepositoryProvider;
    
    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected MavenRepositoryFactory mavenRepositoryFactory;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;
    
    protected MutableStorage getStorage(String storageId)
    {
        final Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        final MutableStorage result = new MutableStorage();
        result.setBasedir(storage.getBasedir());
        result.setId(storage.getId());

        return result;
    }

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
        MavenArtifact artifact = new MavenRepositoryArtifact(ArtifactUtils.getArtifactFromGAVTC(gavtc));
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
        //artifact.setPath(Paths.get(basedir).resolve(MavenArtifactUtils.convertArtifactToPath(artifact)));

        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(artifact);
    }

    public void generateArtifact(String basedir, Artifact artifact, String packaging)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        //artifact.setPath(Paths.get(basedir).resolve(MavenArtifactUtils.convertArtifactToPath(artifact)));

        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(artifact, packaging);
    }

    public void generateArtifact(String basedir, String gavtc, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(gavtc, versions);
    }

    protected MavenArtifactGenerator createArtifactGenerator(String basedir)
    {
        return new MavenArtifactGenerator(basedir) {

            @Override
            protected OutputStream newOutputStream(File artifactFile)
                throws IOException
            {
                Path basePath = Paths.get(basedir);
                Path artifactPath = artifactFile.toPath();

                String path = FilenameUtils.separatorsToUnix(basePath.relativize(artifactPath).toString());

                Path repositoryBasePath = basePath;
                if (repositoryBasePath.endsWith(RepositoryFileSystem.TEMP) || repositoryBasePath.endsWith(RepositoryFileSystem.TEMP))
                {
                    repositoryBasePath = repositoryBasePath.getParent();
                }
                String storageId = repositoryBasePath.getParent().getFileName().toString();
                String repositoryId = repositoryBasePath.getFileName().toString();

                RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, path);

                if (basePath.endsWith(RepositoryFileSystem.TEMP))
                {
                    repositoryPath = RepositoryFiles.temporary(repositoryPath);
                }
                else if (basePath.endsWith(RepositoryFileSystem.TRASH))
                {
                    repositoryPath = RepositoryFiles.trash(repositoryPath);
                }
                
                RepositoryPath finalRepositoryPath = repositoryPath;
                OutputStream outputStream = hostedRepositoryProvider.getOutputStream(repositoryPath);
                outputStream = new FilterOutputStream(outputStream) {

                    @Override
                    public void close()
                        throws IOException
                    {
                        super.close();
                        artifactEventListenerRegistry.dispatchArtifactStoredEvent(finalRepositoryPath);
                    }
                    
                };
                
                return outputStream;
            }
            
        };
    }

    public void generateArtifact(String basedir, String gavtc, String packaging, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(gavtc, packaging, versions);
    }

    public void generatePluginArtifact(String basedir, String gavtc, String... versions)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        MavenArtifactGenerator generator = createArtifactGenerator(basedir);
        generator.generate(gavtc, "maven-plugin", versions);
    }

    public InputStream generateArtifactInputStream(String basedir, String repositoryId, String gavtc, boolean useTempDir)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        File baseDir = new File(basedir + "/" + repositoryId + (useTempDir ? "/.temp" : ""));
        if (!baseDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdirs();
        }

        Artifact artifact = generateArtifact(baseDir.getCanonicalPath(), gavtc);

        return new FileInputStream(new File(baseDir, ArtifactUtils.convertArtifactToPath(artifact)));
    }

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                           String groupId,
                                                           String artifactId,
                                                           String baseSnapshotVersion)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 "jar",
                                                 null,
                                                 1);
    }

    public Artifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                           String groupId,
                                                           String artifactId,
                                                           String baseSnapshotVersion,
                                                           int numberOfBuilds)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 "jar",
                                                 null,
                                                 numberOfBuilds);
    }

    public MavenArtifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                           String groupId,
                                                           String artifactId,
                                                           String baseSnapshotVersion,
                                                           String[] classifiers)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 "jar",
                                                 classifiers,
                                                 1);
    }

    public MavenArtifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                           String groupId,
                                                           String artifactId,
                                                           String baseSnapshotVersion,
                                                           String packaging,
                                                           String[] classifiers)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return createTimestampedSnapshotArtifact(repositoryBasedir,
                                                 groupId,
                                                 artifactId,
                                                 baseSnapshotVersion,
                                                 packaging,
                                                 classifiers,
                                                 1);
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

            artifact = new MavenRepositoryArtifact(groupId, artifactId, version);
            
            RepositoryPath repositoryPath = resolve(repositoryBasedir, artifact);
            artifact.setPath(repositoryPath);

            generateArtifact(repositoryBasedir, artifact, packaging);

            if (classifiers != null)
            {
                for (String classifier : classifiers)
                {
                    String gavtc = groupId + ":" + artifactId + ":" + version + ":jar:" + classifier;
                    generateArtifact(repositoryBasedir,ArtifactUtils.getArtifactFromGAVTC(gavtc));
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
        if (repositoryBasePath.endsWith(RepositoryFileSystem.TEMP)
                || repositoryBasePath.endsWith(RepositoryFileSystem.TRASH))
        {
            repositoryBasePath = repositoryBasePath.getParent();
        }
        
        String repositoryId = repositoryBasePath.getFileName().toString();
        String storageId = repositoryBasePath.getParent().getFileName().toString();
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(storageId, repositoryId, ArtifactUtils.convertArtifactToPath(artifact));
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
        MavenArtifact artifact;

        String version = createSnapshotVersion(baseSnapshotVersion, numberOfBuild, timestamp);

        artifact = new MavenRepositoryArtifact(groupId, artifactId, version);
        RepositoryPath repositoryPath = resolve(repositoryBasedir, artifact);
        artifact.setPath(repositoryPath);

        generateArtifact(repositoryBasedir, artifact, packaging);

        if (classifiers != null)
        {
            for (String classifier : classifiers)
            {
                String gavtc = groupId + ":" + artifactId + ":" + version + ":jar:" + classifier;
                generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(gavtc));
            }
        }

        // Return the main artifact
        return artifact;
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

    /**
     * Generate a couple of testing artifacts for a specific snapshot (i.e. javadoc, sources, etc)
     *
     * @param repositoryBasedir String
     * @param gavt String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    public Artifact createSnapshot(String repositoryBasedir, String gavt)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = ArtifactUtils.getArtifactFromGAVTC(gavt);
        snapshot.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(repositoryBasedir, snapshot);

        return snapshot;
    }

    /**
     * Generate a couple of testing artifacts for a specific snapshot (i.e. javadoc, sources, etc)
     *
     * @param repositoryBasedir String
     * @param gavt String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    public Artifact createSnapshot(String repositoryBasedir, String gavt, String[] classifiers)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Artifact snapshot = ArtifactUtils.getArtifactFromGAVTC(gavt);
        snapshot.setFile(new File(repositoryBasedir + "/" + ArtifactUtils.convertArtifactToPath(snapshot)));

        generateArtifact(repositoryBasedir, snapshot);

        for (String classifier : classifiers)
        {
            generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(gavt + ":" + classifier));

        }

        return snapshot;
    }

    public void changeCreationDate(MavenArtifact artifact)
            throws IOException
    {
        File directory = artifact.getPath().getParent().toFile();

        //noinspection ConstantConditions
        for (final File fileEntry : directory.listFiles())
        {
            if (fileEntry.isFile())
            {
                BasicFileAttributeView attributes = Files.getFileAttributeView(fileEntry.toPath(), BasicFileAttributeView.class);
                FileTime time = FileTime.from(System.currentTimeMillis() + 60000L, TimeUnit.MILLISECONDS);
                attributes.setTimes(time, time, time);
            }
        }
    }

    public File getStorageBasedir(String storageId)
    {
        return new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/" + storageId);
    }

    public File getRepositoryBasedir(String storageId, String repositoryId)
    {
        return new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/" + storageId + "/" + repositoryId);
    }

    public MavenRepositoryFeatures getFeatures()
    {
        return features;
    }

    public RepositoryManagementStrategy getManagementStrategy(String storageId,
                                                              String repositoryId)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        return layoutProvider.getRepositoryManagementStrategy();
    }

    @Override
    public void createProxyRepository(String storageId,
                                      String repositoryId,
                                      String remoteRepositoryUrl)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException
    {
        MutableRemoteRepository remoteRepository = new MutableRemoteRepository();
        remoteRepository.setUrl(remoteRepositoryUrl);

        MutableRepository repository = mavenRepositoryFactory.createRepository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        createRepository(storageId, repository);
    }

}
