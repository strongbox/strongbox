package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinatesEntity;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.RawArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.services.ArtifactEntryService;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.CollectionUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria.Builder.anArtifactEntrySearchCriteria;

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
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
                        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class ArtifactEntryServiceTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryServiceTest.class);

    private final String STORAGE_ID = "storage0";

    private final String REPOSITORY_ID = "aest";

    private final String GROUP_ID = "org.carlspring.strongbox.aest";

    private final String ARTIFACT_ID = "coordinates-test";

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private  ArtifactCoordinatesService artifactCoordinatesService;

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

        List<ArtifactEntity> artifactEntries = findAll(groupId);
        List<ArtifactCoordinatesEntity> artifactCoordinates = artifactEntries.stream()
                                                                               .map(e -> (ArtifactCoordinatesEntity) e.getArtifactCoordinates())
                                                                               .collect(Collectors.toList());
        artifactEntryService.delete(artifactEntries);
        artifactCoordinatesService.delete(artifactCoordinates);

        displayAllEntries(groupId);
    }

    private List<ArtifactEntity> findAll(final String groupId)
    {
        HashMap<String, String> coordinates = new HashMap<>();
        coordinates.put("path", String.format("%s", groupId));
        return artifactEntryService.findArtifactList(null, null, coordinates, false);
    }

    protected int count(final String groupId)
    {
        return findAll(groupId).size();
    }

    @Test
    public void saveEntityShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        ArtifactEntity artifactEntry = new ArtifactEntity();
        artifactEntry.setStorageId(STORAGE_ID);
        artifactEntry.setRepositoryId(REPOSITORY_ID);
        artifactEntry.setArtifactCoordinates(createArtifactCoordinates(groupId,
                                                                       ARTIFACT_ID + "1234",
                                                                       "1.2.3",
                                                                       "jar"));

        assertThat(artifactEntry.getCreated()).isNull();

        artifactEntry = save(artifactEntry);

        assertThat(artifactEntry.getCreated()).isNotNull();

        Date creationDate = artifactEntry.getCreated();
        //Updating artifact entry in order to ensure that creationDate is not updated
        artifactEntry.setDownloadCount(1);
        artifactEntry = save(artifactEntry);

        assertThat(artifactEntry.getCreated()).isEqualTo(creationDate);
    }

    @Test
    public void cascadeUpdateShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        ArtifactCoordinates jarCoordinates = createArtifactCoordinates(groupId, ARTIFACT_ID + "123", "1.2.3", "jar");
        ArtifactCoordinates pomCoordinates = createArtifactCoordinates(groupId, ARTIFACT_ID + "123", "1.2.3", "pom");

        Optional<ArtifactEntity> artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                                                 REPOSITORY_ID,
                                                                                                                 jarCoordinates.buildPath()));

        assertThat(artifactEntryOptional).isPresent();

        ArtifactEntity artifactEntry = artifactEntryOptional.get();
        assertThat(artifactEntry.getArtifactCoordinates()).isNotNull();
        assertThat(artifactEntry.getArtifactCoordinates().buildPath()).isEqualTo(jarCoordinates.buildPath());

        //Simple field update
        artifactEntry.setRepositoryId(REPOSITORY_ID + "abc");
        artifactEntry = save(artifactEntry);

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID,
                                                                                         jarCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isNotPresent();

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID + "abc",
                                                                                         jarCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isPresent();

        //Cascade field update
        RawArtifactCoordinates nullArtifactCoordinates = (RawArtifactCoordinates)artifactEntry.getArtifactCoordinates();
        nullArtifactCoordinates.setId(pomCoordinates.buildPath());
        save(artifactEntry);

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID + "abc",
                                                                                         jarCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isNotPresent();

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID + "abc",
                                                                                         pomCoordinates.buildPath()));
        assertThat(artifactEntryOptional).isPresent();
    }

    private ArtifactEntity save(ArtifactEntity artifactEntry)
    {
        return artifactEntryService.save(artifactEntry);
    }

    @Test
    public void searchBySizeShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<ArtifactEntity> entries = artifactEntryService.findMatching(anArtifactEntrySearchCriteria()
                                                                                .withMinSizeInBytes(500L)
                                                                                .build(),
                                                                        PagingCriteria.ALL)
                                                          .stream()
                                                          .filter(e -> e.getArtifactCoordinates().getId().startsWith(
                                                                  groupId))
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

        List<ArtifactEntity> entries = artifactEntryService.findMatching(anArtifactEntrySearchCriteria()
                                                                                .withLastAccessedTimeInDays(5)
                                                                                .build(),
                                                                        PagingCriteria.ALL)
                                                          .stream()
                                                          .filter(e -> e.getArtifactCoordinates().getId().startsWith(
                                                                  groupId))
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

        List<ArtifactEntity> artifactEntries = findAll(groupId);
        int removed = artifactEntryService.delete(artifactEntries);
        assertThat(removed).isEqualTo(all);

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

        List<ArtifactEntity> artifactEntries = findAll(groupId);
        artifactEntries.remove(0);
        int removed = artifactEntryService.delete(artifactEntries);
        assertThat(removed).isEqualTo(all - 1);

        int left = count(groupId);
        assertThat(left).isEqualTo(1);
    }

    @Test
    public void searchByLastUsedAndBySizeShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<ArtifactEntity> entries = artifactEntryService.findMatching(anArtifactEntrySearchCriteria()
                                                                                .withMinSizeInBytes(500L)
                                                                                .withLastAccessedTimeInDays(5)
                                                                                .build(),
                                                                        PagingCriteria.ALL)
                                                          .stream()
                                                          .filter(e -> e.getArtifactCoordinates().getId().startsWith(
                                                                  groupId))
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

        // prepare search query key (coordinates)
        RawArtifactCoordinates coordinates = new RawArtifactCoordinates(groupId + "/");

        List<ArtifactEntity> artifactEntries = artifactEntryService.findArtifactList(STORAGE_ID,
                                                                                    REPOSITORY_ID,
                                                                                    coordinates.getCoordinates(),
                                                                                    false);

        assertThat(artifactEntries).isNotNull();
        assertThat(artifactEntries).isNotEmpty();
        assertThat(artifactEntries).hasSize(2);

        artifactEntries.forEach(artifactEntry ->
                                {
                                    logger.debug("Found artifact {}", artifactEntry);
                                    assertThat(((RawArtifactCoordinates)artifactEntry.getArtifactCoordinates())
                                                       .getPath().startsWith(groupId + "/")).isTrue();
                                });
    }

    /**
     * Make sure that we are able to search artifacts by two coordinates that need to be joined with logical AND operator.
     */
    @Test
    public void searchByTwoCoordinate(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        logger.debug("There are a total of {} artifacts.", count(groupId));

        // prepare search query key (coordinates)
        RawArtifactCoordinates c1 = new RawArtifactCoordinates(groupId + "/" + ARTIFACT_ID + "/");

        List<ArtifactEntity> result = artifactEntryService.findArtifactList(STORAGE_ID,
                                                                           REPOSITORY_ID,
                                                                           c1.getCoordinates(),
                                                                           false);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);

        result.forEach(artifactEntry ->
                       {
                           logger.debug("Found artifact {}", artifactEntry);
                           assertThat(((RawArtifactCoordinates)artifactEntry.getArtifactCoordinates())
                                              .getPath().startsWith(groupId + "/" + ARTIFACT_ID)).isTrue();
                       });

        Long c = artifactEntryService.countArtifacts(STORAGE_ID, REPOSITORY_ID, c1.getCoordinates(), false);
        assertThat(c).isEqualTo(Long.valueOf(1));
    }

    @Test
    public void saveEntityCreationDateShouldBeGeneratedAutomaticallyAndRemainUnchanged(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        final ArtifactEntity artifactEntry = createArtifactEntry(groupId);

        assertThat(artifactEntry.getCreated()).isNull();
        assertThat(artifactEntry.getLastUpdated()).isNull();

        final ArtifactEntity firstTimeSavedArtifactEntry = save(artifactEntry);
        final String artifactEntryId = firstTimeSavedArtifactEntry.getObjectId();
        final Date creationDate = firstTimeSavedArtifactEntry.getCreated();

        final ArtifactEntity firstTimeReadFromDatabase = artifactEntryService.findOne(artifactEntryId)
                                                                            .orElse(null);

        assertThat(firstTimeReadFromDatabase).isNotNull();
        assertThat(firstTimeReadFromDatabase.getCreated()).isEqualTo(creationDate);

        artifactEntry.setDownloadCount(1);
        save(firstTimeReadFromDatabase);

        final ArtifactEntity secondTimeReadFromDatabase = artifactEntryService.findOne(artifactEntryId)
                                                                             .orElse(null);

        assertThat(secondTimeReadFromDatabase).isNotNull();
        assertThat(secondTimeReadFromDatabase.getCreated()).isEqualTo(creationDate);
    }

    @Test
    public void saveEntityCreatedLastUsedLastUpdatedPropertiesShouldRetainTime(TestInfo testInfo)
            throws ParseException
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        final ArtifactEntity artifactEntry = createArtifactEntry(groupId);
        final ArtifactEntity firstTimeSavedArtifactEntry = save(artifactEntry);
        final String artifactEntryId = firstTimeSavedArtifactEntry.getObjectId();

        final ArtifactEntity firstTimeReadFromDatabase = artifactEntryService.findOne(artifactEntryId)
                                                                            .orElse(null);
        assertThat(firstTimeReadFromDatabase).isNotNull();

        final Date sampleDate = createSampleDate();

        firstTimeReadFromDatabase.setCreated(sampleDate);
        firstTimeReadFromDatabase.setLastUpdated(sampleDate);
        firstTimeReadFromDatabase.setLastUsed(sampleDate);

        save(firstTimeReadFromDatabase);
        final ArtifactEntity secondTimeReadFromDatabase = artifactEntryService.findOne(artifactEntryId)
                                                                             .orElse(null);

        assertThat(secondTimeReadFromDatabase).isNotNull();

        assertThat(secondTimeReadFromDatabase.getCreated()).isEqualTo(sampleDate);
        assertThat(secondTimeReadFromDatabase.getLastUpdated()).isEqualTo(sampleDate);
        assertThat(secondTimeReadFromDatabase.getLastUsed()).isEqualTo(sampleDate);
    }

    private Date createSampleDate()
            throws ParseException
    {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-31 13:15:50");
    }

    private ArtifactEntity createArtifactEntry(String groupId)
    {
        final ArtifactEntity artifactEntry = new ArtifactEntity();

        artifactEntry.setStorageId(STORAGE_ID);
        artifactEntry.setRepositoryId(REPOSITORY_ID);
        artifactEntry.setArtifactCoordinates(createArtifactCoordinates(groupId, ARTIFACT_ID + "1234", "1.2.3", "jar"));

        return artifactEntry;
    }

    private void displayAllEntries(final String groupId)
    {
        List<ArtifactEntity> result = findAll(groupId);
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
        // create 3 artifacts, one will have coordinates that matches our query, one - not
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
        ArtifactEntity artifactEntry = new ArtifactEntity();
        artifactEntry.setArtifactCoordinates(coordinates);
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        save(artifactEntry);
    }

    private void updateArtifactAttributes(final String groupId)
    {
        List<ArtifactEntity> artifactEntries = findAll(groupId);
        for (int i = 0; i < artifactEntries.size(); i++)
        {
            final ArtifactEntity artifactEntry = artifactEntries.get(i);
            if (i == 0)
            {
                artifactEntry.setLastUsed(new Date());
                artifactEntry.setLastUpdated(new Date());
                artifactEntry.setSizeInBytes(1L);
            }
            else
            {
                artifactEntry.setLastUsed(DateUtils.addDays(new Date(), -10));
                artifactEntry.setLastUpdated(DateUtils.addDays(new Date(), -10));
                artifactEntry.setSizeInBytes(100000L);
            }

            save(artifactEntry);
        }
    }

}
