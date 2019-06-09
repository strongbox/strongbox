package org.carlspring.strongbox.repository.group.metadata;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.group.BaseMavenGroupRepositoryComponentTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import javax.inject.Inject;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.carlspring.strongbox.storage.routing.RoutingRuleTypeEnum.DENY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Przemyslaw Fusik
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenMetadataGroupRepositoryComponentOnUploadTest
        extends BaseMavenGroupRepositoryComponentTest
{

    private static final String REPOSITORY_LEAF_AE = "leaf-repo-ae";

    private static final String REPOSITORY_LEAF_AL = "leaf-repo-al";

    private static final String REPOSITORY_LEAF_AZ = "leaf-repo-az";

    private static final String REPOSITORY_LEAF_AD = "leaf-repo-ad";

    private static final String REPOSITORY_LEAF_AG = "leaf-repo-ag";

    private static final String REPOSITORY_LEAF_AK = "leaf-repo-ak";

    private static final String REPOSITORY_GROUP_AA = "group-repo-aa";

    private static final String REPOSITORY_GROUP_AB = "group-repo-ab";

    private static final String REPOSITORY_GROUP_AC = "group-repo-ac";

    private static final String REPOSITORY_GROUP_AF = "group-repo-af";

    private static final String REPOSITORY_GROUP_AH = "group-repo-ah";

    @Inject
    private MavenMetadataGroupRepositoryComponent mavenGroupRepositoryComponent;

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void whenMetadataWasUploadedInRepositoryAllGroupRepositoriesContainingShouldHaveMetadataUpdatedIfPossible(
            @MavenRepository(repositoryId = REPOSITORY_LEAF_AE) Repository repositoryLeafAe,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_AL) Repository repositoryLeafAl,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_AZ) Repository repositoryLeafAz,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_AD) Repository repositoryLeafAd,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_AG) Repository repositoryLeafAg,
            @MavenRepository(repositoryId = REPOSITORY_LEAF_AK) Repository repositoryLeafAk,
            @TestRepository.Group({ REPOSITORY_LEAF_AE,
                                    REPOSITORY_LEAF_AZ })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_AC) Repository repositoryGroupAc,
            @TestRepository.Group({ REPOSITORY_GROUP_AC,
                                    REPOSITORY_LEAF_AD,
                                    REPOSITORY_LEAF_AL })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_AB) Repository repositoryGroupAb,
            @TestRepository.Group({ REPOSITORY_LEAF_AG,
                                    REPOSITORY_GROUP_AB })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_AA) Repository repositoryGroupAa,
            @TestRepository.Group({ REPOSITORY_GROUP_AC,
                                    REPOSITORY_LEAF_AD,
                                    REPOSITORY_LEAF_AL })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_AF) Repository repositoryGroupAf,
            @TestRepository.Group(repositories = { REPOSITORY_GROUP_AF,
                                                   REPOSITORY_LEAF_AK },
                    rules = { @TestRepository.Group.Rule(
                            pattern = ".*(com|org)/artifacts/to/update/releases/update-group.*",
                            repositories = REPOSITORY_LEAF_AD,
                            type = DENY)
                    })
            @MavenRepository(repositoryId = REPOSITORY_GROUP_AH) Repository repositoryGroupAh,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_AL, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                     "1.2.2" })
                    Path artifactLeafAl,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_AG, id = "com.artifacts.to.delete.releases:delete-group", versions = { "1.2.1",
                                                                                                                                     "1.2.2" })
                    Path artifactLeafAg,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_AD, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1",
                                                                                                                                     "1.2.2" })
                    Path artifactLeafAd,
            @MavenTestArtifact(repositoryId = REPOSITORY_LEAF_AK, id = "com.artifacts.to.update.releases:update-group", versions = { "1.2.1" })
                    Path artifactLeafAk)
            throws Exception
    {
        // BEFORE
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AL);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AG);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AD);
        generateMavenMetadata(STORAGE0, REPOSITORY_LEAF_AK);

        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AF, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AB, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AA, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AL, REPOSITORY_GROUP_AH, FilenameUtils.normalize(
                "com/artifacts/to/delete/releases/delete-group/maven-metadata.xml"));
        copyArtifactMetadata(REPOSITORY_LEAF_AK, REPOSITORY_GROUP_AH, FilenameUtils.normalize(
                "com/artifacts/to/update/releases/update-group/maven-metadata.xml"));

        Metadata metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafAd, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafAk, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupAh, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        RepositoryFiles.delete(repositoryPathResolver.resolve(repositoryGroupAf,
                                                              "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                               false);
        RepositoryFiles.delete(repositoryPathResolver.resolve(repositoryGroupAb,
                                                              "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                               false);
        RepositoryFiles.delete(repositoryPathResolver.resolve(repositoryGroupAa,
                                                              "com/artifacts/to/update/releases/update-group/maven-metadata.xml"),
                               false);

        assertThrows(FileNotFoundException.class, () -> mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupAf, "com/artifacts/to/update/releases/update-group")));

        assertThrows(FileNotFoundException.class, () -> mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupAb, "com/artifacts/to/update/releases/update-group")));

        assertThrows(FileNotFoundException.class, () -> mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupAa, "com/artifacts/to/update/releases/update-group")));

        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repositoryLeafAd,
                                                                       "com/artifacts/to/update/releases/update-group");

        // IMITATE THE EVENT
        mavenGroupRepositoryComponent.updateGroupsContaining(repositoryPath);

        // AFTER
        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafAd, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryLeafAk, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupAh, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupAb, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));

        metadata = mavenMetadataManager.readMetadata(
                repositoryPathResolver.resolve(repositoryGroupAa, "com/artifacts/to/update/releases/update-group"));
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.2.1"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.2.2"));
    }


}
