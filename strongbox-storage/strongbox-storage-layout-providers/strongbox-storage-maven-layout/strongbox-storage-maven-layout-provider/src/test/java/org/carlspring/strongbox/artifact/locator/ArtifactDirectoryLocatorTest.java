package org.carlspring.strongbox.artifact.locator;

import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationReportOperation;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
public class ArtifactDirectoryLocatorTest
{

    private static final String REPOSITORY_RELEASES = "adlt-releases";

//    private static PrintStream tempSysOut;
//
//    private ByteArrayOutputStream os;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

//    @BeforeEach
//    public void setUp()
//    {
//        os = new ByteArrayOutputStream();
//    }
//
//    @AfterEach
//    public void tearDown()
//    {
//        os = null;
//        System.setOut(tempSysOut);
//    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testLocateDirectories(@MavenRepository(repositoryId = REPOSITORY_RELEASES) Repository repository,
                                      @MavenArtifactsLocationUtils List<Path> artifactPaths1,
                                      @MavenArtifactsCarlspringStrongboxFoo List<Path> artifactPaths2,
                                      @MavenArtifactsCarlspringMavenLocatorTesting List<Path> artifactPaths3,
                                      @MavenArtifactsCarlspringStrongboxLocator List<Path> artifactPaths4,
                                      @MavenArtifactsCarlspringStrongboxFooLocator List<Path> artifactPaths5,
                                      @MavenArtifactsCarlspringStrongboxLocatorUtils List<Path> artifactPaths6)
            throws IOException
    {
//        System.setOut(new PrintStream(os));
//        tempSysOut = System.out;

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new ArtifactLocationReportOperation());
        locator.locateArtifactDirectories();

//        os.flush();
//
//        String output = new String(os.toByteArray());
//
//        assertThat(output.contains(normalize("org/apache/maven/location-utils"))).isTrue();
//        assertThat(output.contains(normalize("org/carlspring/maven/locator-testing"))).isTrue();
//        assertThat(output.contains(normalize("org/carlspring/strongbox/locator/foo-locator"))).isTrue();
//        assertThat(output.contains(normalize("org/carlspring/strongbox/locator/utils"))).isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testLocateDirectoriesWithBasePath(@MavenRepository(repositoryId = REPOSITORY_RELEASES) Repository repository,
                                                  @MavenArtifactsLocationUtils List<Path> artifactPaths1,
                                                  @MavenArtifactsCarlspringStrongboxFoo List<Path> artifactPaths2,
                                                  @MavenArtifactsCarlspringMavenLocatorTesting List<Path> artifactPaths3,
                                                  @MavenArtifactsCarlspringStrongboxLocator List<Path> artifactPaths4,
                                                  @MavenArtifactsCarlspringStrongboxFooLocator List<Path> artifactPaths5,
                                                  @MavenArtifactsCarlspringStrongboxLocatorUtils List<Path> artifactPaths6)
            throws IOException
    {
//        System.setOut(new PrintStream(os));
//        tempSysOut = System.out;

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).toAbsolutePath();

        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new ArtifactLocationReportOperation(repositoryPath.resolve("org/carlspring").relativize()));
        locator.locateArtifactDirectories();

//        os.flush();
//
//        String output = new String(os.toByteArray());
//
//        assertThat(output.contains(normalize("org/apache/maven/location-utils"))).isFalse();
//        assertThat(output.contains(normalize("org/carlspring/maven/locator-testing"))).isTrue();
//        assertThat(output.contains(normalize("org/carlspring/strongbox/locator/foo-locator"))).isTrue();
//        assertThat(output.contains(normalize("org/carlspring/strongbox/locator/utils"))).isTrue();
    }

    private String normalize(String path)
    {
        return Paths.get(path).normalize().toString();
    }

    @Target({ ElementType.PARAMETER,
              ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                       id = "org.apache.maven:location-utils",
                       versions = { "1.0.1",
                                    "1.0.2",
                                    "1.1",
                                    "1.2",
                                    "1.2.1" })
    private @interface MavenArtifactsLocationUtils
    {

    }

    @Target({ ElementType.PARAMETER,
              ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                       id = "com.carlspring.strongbox:foo",
                       versions = { "5.1",
                                    "5.2",
                                    "5.3" })
    private @interface MavenArtifactsCarlspringStrongboxFoo
    {

    }

    @Target({ ElementType.PARAMETER,
              ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                       id = "org.carlspring.maven:locator-testing",
                       versions = { "2.1",
                                    "2.2",
                                    "2.3",
                                    "2.4",
                                    "2.5",
                                    "3.0" })
    private @interface MavenArtifactsCarlspringMavenLocatorTesting
    {

    }

    @Target({ ElementType.PARAMETER,
              ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                       id = "org.carlspring.strongbox:locator",
                       versions = { "5.2.1",
                                    "5.2.2" })
    private @interface MavenArtifactsCarlspringStrongboxLocator
    {

    }

    @Target({ ElementType.PARAMETER,
              ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                       id = "org.carlspring.strongbox.locator:foo-locator",
                       versions = { "1.0",
                                    "1.1",
                                    "1.2" })
    private @interface MavenArtifactsCarlspringStrongboxFooLocator
    {

    }

    @Target({ ElementType.PARAMETER,
              ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                       id = "org.carlspring.strongbox.locator:utils",
                       versions = { "2.1",
                                    "2.2",
                                    "2.3" })
    private @interface MavenArtifactsCarlspringStrongboxLocatorUtils
    {

    }
}
