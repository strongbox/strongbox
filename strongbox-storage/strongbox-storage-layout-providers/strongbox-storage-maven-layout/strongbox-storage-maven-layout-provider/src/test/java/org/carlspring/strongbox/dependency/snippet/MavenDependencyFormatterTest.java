package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.annotation.*;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
public class MavenDependencyFormatterTest
{

    private static final String REPOSITORY_RELEASES = "mdft-releases";

    private static final String GROUP_ID = "org.carlspring.strongbox";

    private static final String ARTIFACT_ID = "maven-snippet";

    private static final String VERSION_1 = "1.0";

    private static final String VERSION_2 = "2.0";

    private static final String CLASSIFIER_SOURCES = "sources";

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    @Inject
    private SnippetGenerator snippetGenerator;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testMavenDependencyGeneration(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                               setup = MavenIndexedRepositorySetup.class)
                                              Repository repository,
                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                 id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                 versions = { VERSION_1 })
                                              Path artifactPath)
            throws ProviderImplementationException, IOException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            Maven2LayoutProvider.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates(
                (RepositoryPath) artifactPath.normalize());

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("<dependency>\n" +
                           "    <groupId>org.carlspring.strongbox</groupId>\n" +
                           "    <artifactId>maven-snippet</artifactId>\n" +
                           "    <version>1.0</version>\n" +
                           "    <type>jar</type>\n" +
                           "    <scope>compile</scope>\n" +
                           "</dependency>\n");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testMavenDependencyGenerationWithClassifier(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                                             setup = MavenIndexedRepositorySetup.class)
                                                            Repository repository,
                                                            @MavenArtifactWithClassifiers(repositoryId = REPOSITORY_RELEASES,
                                                                                          id = GROUP_ID + ":" + ARTIFACT_ID,
                                                                                          versions = { VERSION_2 })
                                                            Path artifactPath)
            throws ProviderImplementationException, IOException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            Maven2LayoutProvider.ALIAS);
        assertThat(formatter).as("Failed to look up dependency synonym formatter!").isNotNull();

        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates(
                (RepositoryPath) artifactPath.normalize());
        coordinates.setClassifier(CLASSIFIER_SOURCES);

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertThat(snippet)
                .as("Failed to generate dependency!")
                .isEqualTo("<dependency>\n" +
                           "    <groupId>org.carlspring.strongbox</groupId>\n" +
                           "    <artifactId>maven-snippet</artifactId>\n" +
                           "    <version>2.0</version>\n" +
                           "    <type>jar</type>\n" +
                           "    <classifier>sources</classifier>\n" +
                           "    <scope>compile</scope>\n" +
                           "</dependency>\n");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRegisteredSynonyms(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                        setup = MavenIndexedRepositorySetup.class)
                                       Repository repository,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                          id = GROUP_ID + ":" + ARTIFACT_ID,
                                                          versions = { VERSION_1 })
                                       Path artifactPath)
            throws IOException
    {
        MavenArtifactCoordinates coordinates = (MavenArtifactCoordinates) RepositoryFiles.readCoordinates(
                (RepositoryPath) artifactPath.normalize());

        List<CodeSnippet> codeSnippets = snippetGenerator.generateSnippets(Maven2LayoutProvider.ALIAS,
                                                                           coordinates);

        assertThat(codeSnippets).as("Failed to look up dependency synonym formatter!").isNotNull();
        assertThat(codeSnippets.isEmpty()).as("No synonyms found!").isFalse();
        assertThat(codeSnippets).as("Incorrect number of dependency synonyms!").hasSize(7);


        String[] synonyms = new String[]{ "Maven 2", "Bazel", "Buildr", "Gradle", "Ivy", "Leiningen", "SBT", };

        int i = 0;
        for (CodeSnippet snippet : codeSnippets)
        {
            System.out.println(snippet.getName());

            assertThat(snippet.getName()).as("Failed to re-order correctly!").isEqualTo(synonyms[i]);

            i++;
        }
    }

    @Target({ ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @MavenTestArtifact(classifiers = { CLASSIFIER_SOURCES })
    private @interface MavenArtifactWithClassifiers
    {

        @AliasFor(annotation = MavenTestArtifact.class)
        String id() default "";

        @AliasFor(annotation = MavenTestArtifact.class)
        String[] versions() default {};

        @AliasFor(annotation = MavenTestArtifact.class)
        String repositoryId() default "";

    }

}
