package org.carlspring.strongbox.providers.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

import java.nio.file.Path;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactResolutionServiceHelper;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

/**
 * @author carlspring
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class MavenProxyRepositoryProviderTestIT
{
    private static final String STORAGE_ID = "storage-common-proxies";

    private static final String REPOSITORY_ID = "spring-libs-release-it";

    // using jcenter as repositories at https://repo.spring.io now expect you to be logged in to access artifacts
    private static final String REMOTE_URL = "https://jcenter.bintray.com/";

    private static final String CENTRAL_REPOSITORY_ID = "central-release-it";

    private static final String CENTRAL_URL = "https://repo1.maven.org/maven2/";

    @Inject
    private ArtifactRepository artifactEntityRepository;

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Inject
    private ArtifactResolutionServiceHelper artifactResolutionServiceHelper;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void whenDownloadingArtifactMetadaFileShouldAlsoBeResolved(@MavenRepository(storageId = STORAGE_ID,
                                                                                       repositoryId = REPOSITORY_ID + "-whenDownloadingArtifactMetadaFileShouldAlsoBeResolved",
                                                                                       setup = MavenIndexedRepositorySetup.class)
                                                                      @Remote(url = REMOTE_URL)
                                                                      Repository proxyRepository)
            throws Exception
    {

        String storageId = proxyRepository.getStorage().getId();
        String repositoryId = proxyRepository.getId();

        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/properties-injector/1.1/properties-injector-1.1.jar");

        assertThat(RepositoryFiles.artifactExists(
                repositoryPathResolver.resolve(
                        proxyRepository,"org/carlspring/properties-injector/maven-metadata.xml")))
                .isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist(@MavenRepository(storageId = STORAGE_ID,
                                                                                            repositoryId = CENTRAL_REPOSITORY_ID + "-whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist",
                                                                                            setup = MavenIndexedRepositorySetup.class)
                                                                           @Remote(url = CENTRAL_URL)
                                                                           Repository proxyRepository1,
                                                                           @MavenRepository(storageId = STORAGE_ID,
                                                                                            repositoryId = REPOSITORY_ID + "-whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist",
                                                                                            setup = MavenIndexedRepositorySetup.class)
                                                                           @Remote(url = REMOTE_URL)
                                                                           Repository proxyRepository2)
            throws Exception
    {
        String storageId = proxyRepository1.getStorage().getId();

        // 1. download the artifact and artifactId-level maven metadata-file from 1st repository
        String repositoryId = proxyRepository1.getId();

        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "javax/interceptor/javax.interceptor-api/1.2.2/javax.interceptor-api-1.2.2.jar");

        // 2. resolve downloaded artifact base path
        final Path mavenCentralArtifactBaseBath = repositoryPathResolver.resolve(proxyRepository1,
                                                                                 "javax/interceptor/javax.interceptor-api");

        // 3. copy the content to 2nd repository
        repositoryId = proxyRepository2.getId();

        final Path secondRepoArtifactBaseBath = repositoryPathResolver.resolve(proxyRepository2,
                                                                               "javax/interceptor/javax.interceptor-api");
        FileUtils.copyDirectory(mavenCentralArtifactBaseBath.toFile(),
                                secondRepoArtifactBaseBath.toFile());

        // 4. confirm maven-metadata.xml lies in the 2nd repository
        assertThat(RepositoryFiles.artifactExists(
                repositoryPathResolver.resolve(
                        proxyRepository2,"javax/interceptor/javax.interceptor-api/maven-metadata.xml")))
                .isTrue();

        // 5. confirm some pre-merge state
        Path artifactBasePath = repositoryPathResolver.resolve(proxyRepository2,
                                                               "javax/interceptor/javax.interceptor-api/");
        Metadata metadata = mavenMetadataManager.readMetadata(artifactBasePath);
        assertThat(metadata.getVersioning().getVersions()).hasSize(9);
        assertThat(metadata.getVersioning().getVersions().get(8)).isEqualTo("1.2.2");

        // 6. download the artifact from remote 2nd repository - it contains different maven-metadata.xml file
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "javax/interceptor/javax.interceptor-api/1.2.2/javax.interceptor-api-1.2.2.jar");

        // 7. confirm the state of maven-metadata.xml file has changed
        metadata = mavenMetadataManager.readMetadata(artifactBasePath);
        assertThat(metadata.getVersioning().getVersions()).hasSize(10);
        assertThat(metadata.getVersioning().getVersions().get(9)).isEqualTo("3.1");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void whenDownloadingArtifactDatabaseShouldBeAffectedByArtifactEntry(@MavenRepository(storageId = STORAGE_ID,
                                                                                                repositoryId = REPOSITORY_ID + "-whenDownloadingArtifactDatabaseShouldBeAffectedByArtifactEntry",
                                                                                                setup = MavenIndexedRepositorySetup.class)
                                                                               @Remote(url = REMOTE_URL)
                                                                               Repository proxyRepository)
            throws Exception
    {
        String storageId = proxyRepository.getStorage().getId();
        String repositoryId = proxyRepository.getId();
        String path = "org/carlspring/properties-injector/1.6/properties-injector-1.6.jar";

        Artifact artifact = artifactEntityRepository.findOneArtifact(storageId,
                                                                     repositoryId,
                                                                     path);
        Optional<Artifact> artifactEntry = Optional.ofNullable(artifact);
        assertThat(artifactEntry).isEqualTo(Optional.empty());

        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            path);

        artifactEntry = Optional.ofNullable(artifactEntityRepository.findOneArtifact(storageId,
                                                                                     repositoryId,
                                                                                     path));
        assertThat(artifactEntry).isNotEqualTo(Optional.empty());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testMavenCentral(@MavenRepository(storageId = STORAGE_ID,
                                                  repositoryId = REPOSITORY_ID + "-testMavenCentral",
                                                  setup = MavenIndexedRepositorySetup.class)
                                 @Remote(url = REMOTE_URL)
                                 Repository proxyRepository)
            throws Exception
    {
        String storageId = proxyRepository.getStorage().getId();
        String repositoryId = proxyRepository.getId();

        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml");
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml.md5");
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml.sha1");

        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom");
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.md5");
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.sha1");

        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar");
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.md5");
        artifactResolutionServiceHelper.assertStreamNotNull(storageId,
                                                            repositoryId,
                                                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.sha1");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testStrongboxAtCarlspringDotOrg()
            throws Exception
    {
        artifactResolutionServiceHelper.assertStreamNotNull("public",
                                                            "maven-group",
                                                            "org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml");
    }
}
