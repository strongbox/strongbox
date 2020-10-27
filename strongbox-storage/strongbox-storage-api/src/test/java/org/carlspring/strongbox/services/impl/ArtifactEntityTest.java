package org.carlspring.strongbox.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.repositories.ArtifactCoordinatesRepository;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.util.LocalDateTimeInstance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.CollectionUtils;

/**
 * Functional test and usage example scenarios for {@link ArtifactEntryService}.
 *
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @see https://dev.carlspring.org/youtrack/issue/SB-711
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = StorageApiTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Transactional
public class ArtifactEntityTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntityTest.class);

    private final String STORAGE_ID = "storage0";

    private final String REPOSITORY_ID = "aest";

    private final String GROUP_ID = "org.carlspring.strongbox.aest";

    private final String ARTIFACT_ID = "coordinates-test";

    @Inject
    private ArtifactRepository artifactEntityRepository;

    @Inject
    private ArtifactCoordinatesRepository artifactCoordinatesRepository;


    @BeforeEach
    public void setup(TestInfo testInfo)
    {

        final String groupId = getGroupId(GROUP_ID, testInfo);

        createArtifacts(groupId,
                        ARTIFACT_ID,
                        STORAGE_ID,
                        REPOSITORY_ID);

        displayAllEntries(groupId);
    }

    private String getGroupId(String groupId,
                              TestInfo testInfo)
    {
        Assumptions.assumeTrue(testInfo.getTestMethod().isPresent());
        String methodName = testInfo.getTestMethod().get().getName();
        return groupId + "." + methodName;
    }

    @AfterEach
    public void cleanup(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        List<Artifact> artifactEntries = findAll(groupId);
        List<ArtifactCoordinates> artifactCoordinates = artifactEntries.stream()
                                                                       .map(e -> (ArtifactCoordinates) e.getArtifactCoordinates())
                                                                       .collect(Collectors.toList());
        artifactEntityRepository.deleteAll(artifactEntries);
        artifactCoordinatesRepository.deleteAll(artifactCoordinates);

        displayAllEntries(groupId);
    }

    private List<Artifact> findAll(final String groupId)
    {
        return artifactEntityRepository.findByPathLike(STORAGE_ID, REPOSITORY_ID, groupId);
    }

    protected int count(final String groupId)
    {
        return findAll(groupId).size();
    }

    @Test
    public void saveEntityShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        ArtifactCoordinates artifactCoordinates = createArtifactCoordinates(groupId,
                                                                            ARTIFACT_ID + "1234",
                                                                            "1.2.3",
                                                                            "jar");

        Artifact artifactEntry = new ArtifactEntity(STORAGE_ID, REPOSITORY_ID, artifactCoordinates);
        artifactEntry.setStorageId(STORAGE_ID);
        artifactEntry.setRepositoryId(REPOSITORY_ID);
        artifactEntry.setArtifactCoordinates(artifactCoordinates);

        assertThat(artifactEntry.getCreated()).isNull();

        artifactEntry = save(artifactEntry);

        assertThat(artifactEntry.getCreated()).isNotNull();

        LocalDateTime creationDate = artifactEntry.getCreated();
        // Updating artifact entry in order to ensure that creationDate is not
        // updated
        artifactEntry.setDownloadCount(1);
        artifactEntry = save(artifactEntry);

        assertThat(artifactEntry.getCreated()).isEqualTo(creationDate);
    }

    @Disabled
    @Test
    public void cascadeUpdateShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        ArtifactCoordinates jarCoordinates = createArtifactCoordinates(groupId, ARTIFACT_ID + "123", "1.2.3", "jar");
        ArtifactCoordinates pomCoordinates = createArtifactCoordinates(groupId, ARTIFACT_ID + "123", "1.2.3", "pom");

        Optional<Artifact> artifactEntryOptional = Optional.ofNullable(artifactEntityRepository.findOneArtifact(STORAGE_ID,
                                                                                                                REPOSITORY_ID,
                                                                                                                jarCoordinates.buildPath()));

        assertThat(artifactEntryOptional).isPresent();

        Artifact artifactEntry = artifactEntryOptional.get();
        assertThat(artifactEntry.getArtifactCoordinates()).isNotNull();
        assertThat(artifactEntry.getArtifactCoordinates().buildPath()).isEqualTo(jarCoordinates.buildPath());

        // Simple field update
        artifactEntry.setRepositoryId(REPOSITORY_ID + "abc");
        assertThatThrownBy(() -> save(artifactEntry)).isInstanceOf(IllegalStateException.class)
                                                     .hasMessage("Can't change the uuid, [storage0-aestabc-org.carlspring.strongbox.aest.cascadeUpdateShouldWork/coordinates-test123/1.2.3/jar]->[storage0-aest-org.carlspring.strongbox.aest.cascadeUpdateShouldWork/coordinates-test123/1.2.3/jar].");

        artifactEntryOptional = Optional.ofNullable(artifactEntityRepository.findOneArtifact(STORAGE_ID,
                                                                                             REPOSITORY_ID,
                                                                                             jarCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isNotPresent();

        artifactEntryOptional = Optional.ofNullable(artifactEntityRepository.findOneArtifact(STORAGE_ID,
                                                                                             REPOSITORY_ID + "abc",
                                                                                             jarCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isPresent();

        // Cascade field update
        RawArtifactCoordinates nullArtifactCoordinates = (RawArtifactCoordinates) artifactEntry.getArtifactCoordinates();
        nullArtifactCoordinates.setId(pomCoordinates.buildPath());
        assertThatThrownBy(() -> save(artifactEntry)).isInstanceOf(IllegalStateException.class)
                                                     .hasMessage("Can't change the uuid, [org.carlspring.strongbox.aest.cascadeUpdateShouldWork/coordinates-test123/1.2.3/jar]->[org.carlspring.strongbox.aest.cascadeUpdateShouldWork/coordinates-test123/1.2.3/pom].");

        artifactEntryOptional = Optional.ofNullable(artifactEntityRepository.findOneArtifact(STORAGE_ID,
                                                                                             REPOSITORY_ID + "abc",
                                                                                             jarCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isPresent();

        artifactEntryOptional = Optional.ofNullable(artifactEntityRepository.findOneArtifact(STORAGE_ID,
                                                                                             REPOSITORY_ID + "abc",
                                                                                             pomCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isNotPresent();
    }

    private Artifact save(Artifact artifactEntry)
    {
        return artifactEntityRepository.save(artifactEntry);
    }

    @Test
    public void searchBySizeShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<Artifact> entries = artifactEntityRepository.findMatching((Integer)null,
                                                                       500L,
                                                                       PageRequest.of(0, Integer.MAX_VALUE))
                                                         .stream()
                                                         .filter(e -> e.getArtifactCoordinates()
                                                                       .getId()
                                                                       .startsWith(groupId))
                                                         .collect(Collectors.toList());

        entries.forEach(entry -> logger.debug("Found artifact after search: [{}] - {}",
                                              entry.getArtifactCoordinates().getId(),
                                              entry));

        assertThat(entries).hasSize(all - 1);
    }

    @Test
    public void searchByLastUsedShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<Artifact> entries = artifactEntityRepository.findMatching(5, null,
                                                                       PageRequest.of(0, Integer.MAX_VALUE, Sort.by("'uuid'")))
                                                         .stream()
                                                         .filter(e -> e.getArtifactCoordinates()
                                                                       .getId()
                                                                       .startsWith(groupId))
                                                         .collect(Collectors.toList());

        entries.forEach(entry -> logger.debug("Found artifact after search: [{}] - {}",
                                              entry.getArtifactCoordinates().getId(),
                                              entry));

        assertThat(entries).hasSize(all - 1);
    }

    @Test
    public void deleteAllShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        assertThat(all).isEqualTo(3);

        List<Artifact> artifactEntries = findAll(groupId);
        artifactEntityRepository.deleteAll(artifactEntries);

        int left = count(groupId);
        assertThat(left).isZero();
        assertThat(findAll(groupId)).isEmpty();
    }

    @Test
    public void deleteButNotAllShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        assertThat(all).isEqualTo(3);

        List<Artifact> artifactEntries = findAll(groupId);
        artifactEntries.remove(0);
        artifactEntityRepository.deleteAll(artifactEntries);

        int left = count(groupId);
        assertThat(left).isEqualTo(1);
    }

    @Test
    public void searchByLastUsedAndBySizeShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<Artifact> entries = artifactEntityRepository.findMatching(5, 500L,
                                                                       PageRequest.of(0, Integer.MAX_VALUE))
                                                         .stream()
                                                         .filter(e -> e.getArtifactCoordinates()
                                                                       .getId()
                                                                       .startsWith(groupId))
                                                         .collect(Collectors.toList());

        entries.forEach(entry -> logger.debug("Found artifact after search: [{}] - {}",
                                              entry.getArtifactCoordinates().getId(),
                                              entry));

        assertThat(entries).hasSize(all - 1);
    }

    /**
     * Make sure that we are able to search artifacts by single coordinate.
     *
     */
    @Test
    public void searchBySingleCoordinate(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        logger.debug("There are a total of {} artifacts.", count(groupId));
        List<Artifact> artifactEntries = artifactEntityRepository.findByPathLike(STORAGE_ID,
                                                                                 REPOSITORY_ID,
                                                                                 groupId + "/");

        assertThat(artifactEntries).isNotNull();
        assertThat(artifactEntries).isNotEmpty();
        assertThat(artifactEntries).hasSize(2);

        artifactEntries.forEach(artifactEntry -> {
            logger.debug("Found artifact {}", artifactEntry);
            assertThat(((RawArtifactCoordinates) artifactEntry.getArtifactCoordinates())
                                                                                        .getPath()
                                                                                        .startsWith(groupId + "/")).isTrue();
        });
    }

    @Test
    public void saveEntityCreationDateShouldBeGeneratedAutomaticallyAndRemainUnchanged(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        final Artifact artifactEntry = createArtifactEntry(groupId);

        assertThat(artifactEntry.getCreated()).isNull();
        assertThat(artifactEntry.getLastUpdated()).isNull();

        final Artifact firstTimeSavedArtifactEntry = save(artifactEntry);
        final String artifactEntryId = firstTimeSavedArtifactEntry.getUuid();
        final LocalDateTime creationDate = firstTimeSavedArtifactEntry.getCreated();

        final Artifact firstTimeReadFromDatabase = artifactEntityRepository.findById(artifactEntryId)
                                                                           .orElse(null);

        assertThat(firstTimeReadFromDatabase).isNotNull();
        assertThat(firstTimeReadFromDatabase.getCreated()).isEqualTo(creationDate);

        artifactEntry.setDownloadCount(1);
        save(firstTimeReadFromDatabase);

        final Artifact secondTimeReadFromDatabase = artifactEntityRepository.findById(artifactEntryId)
                                                                            .orElse(null);

        assertThat(secondTimeReadFromDatabase).isNotNull();
        assertThat(secondTimeReadFromDatabase.getCreated()).isEqualTo(creationDate);
    }

    @Test
    public void saveEntityCreatedLastUsedLastUpdatedPropertiesShouldRetainTime(TestInfo testInfo)
        throws ParseException
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        final Artifact artifactEntry = createArtifactEntry(groupId);
        final Artifact firstTimeSavedArtifactEntry = save(artifactEntry);
        final String artifactEntryId = firstTimeSavedArtifactEntry.getUuid();

        final Artifact firstTimeReadFromDatabase = artifactEntityRepository.findById(artifactEntryId)
                                                                           .orElse(null);
        assertThat(firstTimeReadFromDatabase).isNotNull();

        final LocalDateTime sampleDate = createSampleDate();

        firstTimeReadFromDatabase.setCreated(sampleDate);
        firstTimeReadFromDatabase.setLastUpdated(sampleDate);
        firstTimeReadFromDatabase.setLastUsed(sampleDate);

        save(firstTimeReadFromDatabase);
        final Artifact secondTimeReadFromDatabase = artifactEntityRepository.findById(artifactEntryId)
                                                                            .orElse(null);

        assertThat(secondTimeReadFromDatabase).isNotNull();

        assertThat(secondTimeReadFromDatabase.getCreated()).isEqualTo(sampleDate);
        assertThat(secondTimeReadFromDatabase.getLastUpdated()).isEqualTo(sampleDate);
        assertThat(secondTimeReadFromDatabase.getLastUsed()).isEqualTo(sampleDate);
    }

    private LocalDateTime createSampleDate()
    {
        return LocalDateTime.parse("2019-10-31 13:15:50",
                                   new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss").toFormatter());
    }

    private ArtifactEntity createArtifactEntry(String groupId)
    {
        ArtifactCoordinates artifactCoordinates = createArtifactCoordinates(groupId, ARTIFACT_ID + "1234", "1.2.3", "jar");

        final ArtifactEntity artifactEntry = new ArtifactEntity(STORAGE_ID, REPOSITORY_ID, artifactCoordinates);
        artifactEntry.setStorageId(STORAGE_ID);
        artifactEntry.setRepositoryId(REPOSITORY_ID);
        artifactEntry.setArtifactCoordinates(artifactCoordinates);

        return artifactEntry;
    }

    private void displayAllEntries(final String groupId)
    {
        List<Artifact> result = findAll(groupId);
        if (CollectionUtils.isEmpty(result))
        {
            logger.debug("Artifact repository is empty");
        }
        else
        {
            result.forEach(artifactEntry -> logger.debug("Found artifact [{}] - {}",
                                                         artifactEntry.getArtifactCoordinates().getId(),
                                                         artifactEntry));
        }
    }

    private void createArtifacts(String groupId,
                                 String artifactId,
                                 String storageId,
                                 String repositoryId)
    {
        // create 3 artifacts, one will have coordinates that matches our query,
        // one - not
        ArtifactCoordinates coordinates1 = createArtifactCoordinates(groupId, artifactId + "123", "1.2.3", "jar");
        ArtifactCoordinates coordinates2 = createArtifactCoordinates(groupId, artifactId, "1.2.3", "jar");
        ArtifactCoordinates coordinates3 = createArtifactCoordinates(groupId + "myId", artifactId + "321", "1.2.3",
                                                                     "jar");

        createArtifactEntry(coordinates1, storageId, repositoryId);
        createArtifactEntry(coordinates2, storageId, repositoryId);
        createArtifactEntry(coordinates3, storageId, repositoryId);
    }

    private ArtifactCoordinates createArtifactCoordinates(final String groupId,
                                                          final String artifactId,
                                                          final String version,
                                                          final String extension)
    {

        return new RawArtifactCoordinates(String.format("%s/%s/%s/%s", groupId, artifactId, version, extension));
    }

    private void createArtifactEntry(ArtifactCoordinates coordinates,
                                     String storageId,
                                     String repositoryId)
    {
        ArtifactEntity artifactEntry = new ArtifactEntity(storageId, repositoryId, coordinates);
        artifactEntry.setArtifactCoordinates(coordinates);
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        save(artifactEntry);
    }

    private void updateArtifactAttributes(final String groupId)
    {
        List<Artifact> artifactEntries = findAll(groupId);
        for (int i = 0; i < artifactEntries.size(); i++)
        {
            LocalDateTime now = LocalDateTimeInstance.now();

            final Artifact artifactEntry = artifactEntries.get(i);
            if (i == 0)
            {
                artifactEntry.setLastUsed(now);
                artifactEntry.setLastUpdated(now);
                artifactEntry.setSizeInBytes(1L);
            }
            else
            {
                artifactEntry.setLastUsed(now.minusDays(10));
                artifactEntry.setLastUpdated(now.minusDays(10));
                artifactEntry.setSizeInBytes(100000L);
            }

            save(artifactEntry);
        }
    }

}
