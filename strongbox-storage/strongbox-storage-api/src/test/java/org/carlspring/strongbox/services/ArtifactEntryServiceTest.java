package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;

import javax.inject.Inject;
import java.util.List;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

/**
 * Functional test and usage example scenarios for {@link ArtifactEntryService}.
 *
 * @author Alex Oreshkevich
 * @see https://dev.carlspring.org/youtrack/issue/SB-711
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { StorageApiConfig.class })
@Transactional
@Rollback(false)
public class ArtifactEntryServiceTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryServiceTest.class);

    @Inject
    ArtifactEntryService artifactEntryService;

    @Inject
    OObjectDatabaseTx databaseTx;

    final String storageId = "storage0";
    final String repositoryId = "release";

    final String groupId = "org.carlspring.strongbox";
    final String artifactId = "coordinates-test";

    @Before
    public synchronized void init()
            throws Exception
    {

        logger.info("[prepareTests] Create artifacts....");

        createArtifacts(groupId, artifactId, storageId, repositoryId);
        displayAllEntries();
    }

    @Test
    public synchronized void testAll()
            throws Exception
    {
        logger.info("[testAll] Start testing....");

        if (artifactEntryService.count() > 0)
        {

            checkThatArtifactEntryIsCreatable();

            searchBySingleCoordinate(groupId, 2);
            searchByTwoCoordinate(groupId, artifactId, 1);

            artifactEntryService.deleteAll();
        }
        else
        {
            logger.warn("Artifact entries storage was not initialized properly. Unable to execute any tests.");
        }
    }

    private void checkThatArtifactEntryIsCreatable()
    {
        final String storageId = "storage3";
        final String repositoryId = "release432";

        ArtifactEntry artifactEntry = createArtifactEntry(createMavenArtifactCoordinates(), storageId, repositoryId);
        logger.info("Saved entity " + artifactEntry);

        if (artifactEntryService.count() > 0)
        {
            ArtifactEntry savedEntry = databaseTx.detachAll(
                    artifactEntryService
                            .findOne(artifactEntry.getObjectId())
                            .orElseThrow(() -> new NullPointerException("Unable to find any artifact entry")), true);

            logger.info("[checkThatArtifactEntryIsCreatable] Detached entity " + savedEntry);

            assertEquals(storageId, savedEntry.getStorageId());
            assertEquals(repositoryId, savedEntry.getRepositoryId());

            String savedObjectId = savedEntry.getObjectId();
            logger.info("[checkThatArtifactEntryIsCreatable] Delete entity by ID " + savedObjectId);
            artifactEntryService.delete(savedObjectId);
        }
        else
        {
            logger.warn("Unable to find saved entries in the db...");
        }
    }

    /**
     * Make sure that we are able to search artifacts by single coordinate.
     *
     * @param groupId
     * @throws Exception
     */
    private void searchBySingleCoordinate(String groupId,
                                          final int expectedResultCount)
            throws Exception
    {

        // prepare search query key (coordinates)
        MavenArtifactCoordinates query = new MavenArtifactCoordinates();
        query.setGroupId(groupId);

        List<ArtifactEntry> result = artifactEntryService.findByCoordinates(query);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertEquals(expectedResultCount, result.size());

        result.forEach(artifactEntry ->
                       {

                           databaseTx.activateOnCurrentThread();
                           ArtifactEntry artifact = databaseTx.detachAll(artifactEntry, true);

                           logger.info("Found artifact " + artifact);

                           assertEquals(groupId, artifact.getArtifactCoordinates()
                                                         .getCoordinate("groupId"));
                       });
    }

    /**
     * Make sure that we are able to search artifacts by two coordinates that need to be joined with logical AND operator.
     *
     * @param groupId
     * @param artifactId
     */
    private void searchByTwoCoordinate(String groupId,
                                       String artifactId,
                                       final int expectedResultCount)
            throws Exception
    {

        // prepare search query key (coordinates)
        MavenArtifactCoordinates query = new MavenArtifactCoordinates();
        query.setGroupId(groupId);
        query.setArtifactId(artifactId);

        List<ArtifactEntry> result = artifactEntryService.findByCoordinates(query);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertEquals(expectedResultCount, result.size());

        result.forEach(artifactEntry ->
                       {

                           databaseTx.activateOnCurrentThread();
                           ArtifactEntry artifact = databaseTx.detachAll(artifactEntry, true);

                           logger.info("Found artifact " + artifact);

                           assertEquals(groupId, artifact.getArtifactCoordinates()
                                                         .getCoordinate("groupId"));
                           assertEquals(artifactId, artifact.getArtifactCoordinates()
                                                            .getCoordinate("artifactId"));
                       });
    }

    private void displayAllEntries()
    {
        logger.info("[displayAllEntries] ->>>> ...... ");
        List<ArtifactEntry> result = artifactEntryService.findAll()
                                                         .orElse(null);
        if (result == null || result.isEmpty())
        {
            logger.warn("Artifact repository is empty");
        }

        result.forEach(artifactEntry ->
                       {
                           databaseTx.activateOnCurrentThread();
                           ArtifactEntry artifact = databaseTx.detachAll(
                                   artifactEntry, true);
                           logger.info("Found artifact " +
                                       artifact);
                       });
    }

    private void createArtifacts(String groupId,
                                 String artifactId,
                                 String storageId,
                                 String repositoryId)
    {
        // create 3 artifacts, one will have coordinates that matches our query, one - not

        ArtifactCoordinates coordinates1 = new MavenArtifactCoordinates(groupId,
                                                                        artifactId + "123",
                                                                        "1.2.3",
                                                                        null,
                                                                        "jar");

        ArtifactCoordinates coordinates2 = new MavenArtifactCoordinates(groupId,
                                                                        artifactId,
                                                                        "1.2.3",
                                                                        null,
                                                                        "jar");

        ArtifactCoordinates coordinates3 = new MavenArtifactCoordinates(groupId + "myId",
                                                                        artifactId + "321",
                                                                        "1.2.3",
                                                                        null,
                                                                        "jar");

        createArtifactEntry(coordinates1, storageId, repositoryId);
        createArtifactEntry(coordinates2, storageId, repositoryId);
        createArtifactEntry(coordinates3, storageId, repositoryId);
    }

    private ArtifactEntry createArtifactEntry(ArtifactCoordinates coordinates,
                                              String storageId,
                                              String repositoryId)
    {

        ArtifactEntry artifactEntry = new ArtifactEntry();
        artifactEntry.setArtifactCoordinates(coordinates);
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        databaseTx.activateOnCurrentThread();
        return databaseTx.detachAll(artifactEntryService.save(artifactEntry), true);
    }

    private ArtifactCoordinates createMavenArtifactCoordinates()
    {

        return new MavenArtifactCoordinates("org.carlspring.strongbox.another.package",
                                            "coordinates-test-super-test",
                                            "1.2.3",
                                            null,
                                            "jar");
    }
}
