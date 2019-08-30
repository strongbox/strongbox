package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.artifact.coordinates.AbstractArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.NullArtifactCoordinates;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.services.ArtifactEntryService;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.CollectionUtils;
import static org.carlspring.strongbox.services.support.ArtifactEntrySearchCriteria.Builder.anArtifactEntrySearchCriteria;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

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
@Execution(CONCURRENT)
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

        List<ArtifactEntry> artifactEntries = findAll(groupId);
        List<AbstractArtifactCoordinates> artifactCoordinates = artifactEntries.stream()
                                                                               .map(e -> (AbstractArtifactCoordinates) e.getArtifactCoordinates())
                                                                               .collect(Collectors.toList());
        artifactEntryService.delete(artifactEntries);
        artifactCoordinatesService.delete(artifactCoordinates);
        
        displayAllEntries(groupId);
    }

    private List<ArtifactEntry> findAll(final String groupId)
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

        ArtifactEntry artifactEntry = new ArtifactEntry();
        artifactEntry.setStorageId(STORAGE_ID);
        artifactEntry.setRepositoryId(REPOSITORY_ID);
        artifactEntry.setArtifactCoordinates(createArtifactCoordinates(groupId,
                                                                       ARTIFACT_ID + "1234",
                                                                       "1.2.3",
                                                                       "jar"));

        assertThat(artifactEntry.getCreated(), CoreMatchers.nullValue());

        artifactEntry = save(artifactEntry);

        assertThat(artifactEntry.getCreated(), CoreMatchers.notNullValue());

        Date creationDate = artifactEntry.getCreated();
        //Updating artifact entry in order to ensure that creationDate is not updated
        artifactEntry.setDownloadCount(1);
        artifactEntry = save(artifactEntry);

        assertEquals(artifactEntry.getCreated(), creationDate);
    }

    @Test
    public void cascadeUpdateShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        ArtifactCoordinates jarCoordinates = createArtifactCoordinates(groupId, ARTIFACT_ID + "123", "1.2.3", "jar");
        ArtifactCoordinates pomCoordinates = createArtifactCoordinates(groupId, ARTIFACT_ID + "123", "1.2.3", "pom");

        Optional<ArtifactEntry> artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                                                 REPOSITORY_ID,
                                                                                                                 jarCoordinates.toPath()));

        assertTrue(artifactEntryOptional.isPresent());

        ArtifactEntry artifactEntry = artifactEntryOptional.get();
        assertThat(artifactEntry.getArtifactCoordinates(), CoreMatchers.notNullValue());
        assertEquals(jarCoordinates.toPath(), artifactEntry.getArtifactCoordinates().toPath());

        //Simple field update
        artifactEntry.setRepositoryId(REPOSITORY_ID + "abc");
        artifactEntry = save(artifactEntry);

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID,
                                                                                         jarCoordinates.toPath()));
        assertFalse(artifactEntryOptional.isPresent());

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID + "abc",
                                                                                         jarCoordinates.toPath()));
        assertTrue(artifactEntryOptional.isPresent());

        //Cascade field update
        NullArtifactCoordinates nullArtifactCoordinates = (NullArtifactCoordinates)artifactEntry.getArtifactCoordinates();
        nullArtifactCoordinates.setId(pomCoordinates.toPath());
        save(artifactEntry);

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID + "abc",
                                                                                         jarCoordinates.toPath()));
        assertFalse(artifactEntryOptional.isPresent());

        artifactEntryOptional = Optional.ofNullable(artifactEntryService.findOneArtifact(STORAGE_ID,
                                                                                         REPOSITORY_ID + "abc",
                                                                                         pomCoordinates.toPath()));
        assertTrue(artifactEntryOptional.isPresent());
    }

    private ArtifactEntry save(ArtifactEntry artifactEntry)
    {
        return artifactEntryService.save(artifactEntry);
    }
    
    @Test
    public void searchBySizeShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<ArtifactEntry> entries = artifactEntryService.findMatching(anArtifactEntrySearchCriteria()
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

        assertThat(entries.size(), CoreMatchers.equalTo(all - 1));
    }

    @Test
    public void searchByLastUsedShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<ArtifactEntry> entries = artifactEntryService.findMatching(anArtifactEntrySearchCriteria()
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

        assertThat(entries.size(), CoreMatchers.equalTo(all - 1));
    }

    @Test
    public void deleteAllShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        assertThat(all, CoreMatchers.equalTo(3));

        List<ArtifactEntry> artifactEntries = findAll(groupId);
        int removed = artifactEntryService.delete(artifactEntries);
        assertThat(removed, CoreMatchers.equalTo(all));

        int left = count(groupId);
        assertThat(left, CoreMatchers.equalTo(0));
        assertTrue(findAll(groupId).isEmpty());
    }

    @Test
    public void deleteButNotAllShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        assertThat(all, CoreMatchers.equalTo(3));

        List<ArtifactEntry> artifactEntries = findAll(groupId);
        artifactEntries.remove(0);
        int removed = artifactEntryService.delete(artifactEntries);
        assertThat(removed, CoreMatchers.equalTo(all - 1));

        int left = count(groupId);
        assertThat(left, CoreMatchers.equalTo(1));
    }

    @Test
    public void searchByLastUsedAndBySizeShouldWork(TestInfo testInfo)
    {
        final String groupId = getGroupId(GROUP_ID, testInfo);

        int all = count(groupId);
        updateArtifactAttributes(groupId);

        List<ArtifactEntry> entries = artifactEntryService.findMatching(anArtifactEntrySearchCriteria()
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

        assertThat(entries.size(), CoreMatchers.equalTo(all - 1));
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
        NullArtifactCoordinates coordinates = new NullArtifactCoordinates(groupId + "/");

        List<ArtifactEntry> artifactEntries = artifactEntryService.findArtifactList(STORAGE_ID,
                                                                                    REPOSITORY_ID,
                                                                                    coordinates.getCoordinates(),
                                                                                    false);

        assertNotNull(artifactEntries);
        assertFalse(artifactEntries.isEmpty());
        assertEquals(2, artifactEntries.size());

        artifactEntries.forEach(artifactEntry ->
                                {
                                    logger.debug("Found artifact {}", artifactEntry);
                                    assertTrue(((NullArtifactCoordinates)artifactEntry.getArtifactCoordinates()).getPath().startsWith(
                                            groupId + "/"));
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
        NullArtifactCoordinates c1 = new NullArtifactCoordinates(groupId + "/" + ARTIFACT_ID + "/");

        List<ArtifactEntry> result = artifactEntryService.findArtifactList(STORAGE_ID,
                                                                           REPOSITORY_ID,
                                                                           c1.getCoordinates(),
                                                                           false);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertEquals(1, result.size());

        result.forEach(artifactEntry ->
                       {
                           logger.debug("Found artifact {}", artifactEntry);
                           assertTrue(((NullArtifactCoordinates)artifactEntry.getArtifactCoordinates()).getPath().startsWith(
                                   groupId + "/" + ARTIFACT_ID));
                       });

        Long c = artifactEntryService.countArtifacts(STORAGE_ID, REPOSITORY_ID, c1.getCoordinates(), false);
        assertEquals(Long.valueOf(1), c);
    }

    private void displayAllEntries(final String groupId)
    {
        List<ArtifactEntry> result = findAll(groupId);
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

        return new NullArtifactCoordinates(String.format("%s/%s/%s/%s", groupId, artifactId, version, extension));
    }

    private void createArtifactEntry(ArtifactCoordinates coordinates,
                                     String storageId,
                                     String repositoryId)
    {
        ArtifactEntry artifactEntry = new ArtifactEntry();
        artifactEntry.setArtifactCoordinates(coordinates);
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        save(artifactEntry);
    }

    private void updateArtifactAttributes(final String groupId)
    {
        List<ArtifactEntry> artifactEntries = findAll(groupId);
        for (int i = 0; i < artifactEntries.size(); i++)
        {
            final ArtifactEntry artifactEntry = artifactEntries.get(i);
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
