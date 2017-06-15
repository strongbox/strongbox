package org.carlspring.strongbox.services;

import org.carlspring.strongbox.TestConfig;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.StorageCoreConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;

import javax.inject.Inject;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * Functional test and usage example scenarios for {@link ArtifactEntryService}.
 *
 * @author Alex Oreshkevich
 * @see https://dev.carlspring.org/youtrack/issue/SB-711
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class ArtifactEntryServiceTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryServiceTest.class);

    @Inject
    ArtifactEntryService artifactEntryService;

    final String storageId = "storage0";

    final String repositoryId = "release";

    final String groupId = "org.carlspring.strongbox";

    final String artifactId = "coordinates-test";

    /**
     * Make sure that we are able to search artifacts by single coordinate.
     *
     * @throws Exception
     */
    @Test
    public void searchBySingleCoordinate()
            throws Exception
    {
        artifactEntryService.deleteAll();
        createArtifacts(groupId, artifactId, storageId, repositoryId);
        displayAllEntries();

        logger.debug("There are a total of " + artifactEntryService.count() + " artifacts.");

        // prepare search query key (coordinates)
        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId(groupId);

        List<ArtifactEntry> artifactEntries = artifactEntryService.findByCoordinates(coordinates);

        assertNotNull(artifactEntries);
        assertFalse(artifactEntries.isEmpty());
        assertEquals(2, artifactEntries.size());

        artifactEntries.forEach(artifactEntry ->
                                {
                                    logger.info("Found artifact " + artifactEntry);

                                    assertEquals(groupId,
                                                 artifactEntry.getArtifactCoordinates().getCoordinate("groupId"));
                                });
    }

    /**
     * Make sure that we are able to search artifacts by two coordinates that need to be joined with logical AND operator.
     */
    @Test
    public void searchByTwoCoordinate()
            throws Exception
    {
        artifactEntryService.deleteAll();

        createArtifacts(groupId, artifactId, storageId, repositoryId);
        displayAllEntries();

        logger.debug("There are a total of " + artifactEntryService.count() + " artifacts.");

        // prepare search query key (coordinates)
        MavenArtifactCoordinates query = new MavenArtifactCoordinates();
        query.setGroupId(groupId);
        query.setArtifactId(artifactId);

        List<ArtifactEntry> result = artifactEntryService.findByCoordinates(query);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertEquals(1, result.size());

        result.forEach(artifactEntry ->
                       {
                           logger.debug("Found artifact " + artifactEntry);

                           assertEquals(groupId, artifactEntry.getArtifactCoordinates().getCoordinate("groupId"));
                           assertEquals(artifactId, artifactEntry.getArtifactCoordinates().getCoordinate("artifactId"));
                       });

        artifactEntryService.deleteAll();
    }

    public void displayAllEntries()
    {
        List<ArtifactEntry> result = artifactEntryService.findAll()
                                                         .orElse(null);
        if (result == null || result.isEmpty())
        {
            logger.warn("Artifact repository is empty");
        }

        result.forEach(artifactEntry -> logger.debug("Found artifact " + artifactEntry));
    }

    public void createArtifacts(String groupId,
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

    public ArtifactEntry createArtifactEntry(ArtifactCoordinates coordinates,
                                             String storageId,
                                             String repositoryId)
    {
        ArtifactEntry artifactEntry = new ArtifactEntry();
        artifactEntry.setArtifactCoordinates(coordinates);
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        return artifactEntryService.save(artifactEntry);
    }

    public ArtifactCoordinates createMavenArtifactCoordinates()
    {

        return new MavenArtifactCoordinates("org.carlspring.strongbox.another.package",
                                            "coordinates-test-super-test",
                                            "1.2.3",
                                            null,
                                            "jar");
    }

}
