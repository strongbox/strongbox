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
import org.springframework.beans.factory.annotation.Autowired;
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
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { StorageApiConfig.class })
@Rollback(false)
public class ArtifactEntryServiceTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryServiceTest.class);

    @Inject
    ArtifactEntryService artifactEntryService;

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Before
    public void prepareTests()
    {

        // uncomment that if you want to debug db entries manually and set @Rollback(false)
        artifactEntryService.deleteAll();
    }

    @Test
    public void testThatArtifactEntryIsCreatable()
    {

        final String storageId = "storage0";
        final String repositoryId = "release";

        ArtifactEntry artifactEntry = createArtifactEntry(createMavenArtifactCoordinates(), storageId, repositoryId);
        logger.info("Saved entity " + artifactEntry);

        if (artifactEntryService.count() > 0)
        {
            ArtifactEntry savedEntry = databaseTx.detachAll(
                    artifactEntryService
                            .findAll()
                            .orElseThrow(() -> new NullPointerException("Unable to find any artifact entry"))
                            .get(0), true);

            logger.info("Detached entity " + savedEntry);

            assertEquals(storageId, savedEntry.getStorageId());
            assertEquals(repositoryId, savedEntry.getRepositoryId());
        }
        else
        {
            logger.warn("Unable to find saved entries in the db...");
        }
    }


    /*
        there will be multiple implementations of ArtifactEntry - MavenArtifactEntry, NugetArtifactEntry, etc...
        each of these will have it's own *ArtifactCooridnates -- MavenArtifactCoordinates, etc...

        meaning that they will have different cooridnates.
        as in "fields" for these coordinates.
        in Maven these will be GAVTC (groupId, artifactId, version, type, classifier)
        in Nuget these will be id, version, extension, etc...
        npm will have it's own coordinate fields and so on...

        the idea is to have a generic enough implementation that suits this.
     */
    @Test
    public void testSearchByCoordinates()
            throws Exception
    {

        // prepare ArtifactEntry instance in database
        final String storageId = "storage0";
        final String repositoryId = "release";
        createArtifactEntry(createMavenArtifactCoordinates(), storageId, repositoryId);

        // display current database entries
        displayAllEntries();

        // prepare search query key (coordinates)
        MavenArtifactCoordinates query = new MavenArtifactCoordinates();
        query.setGroupId("org.carlspring.strongbox");

        List<ArtifactEntry> result = artifactEntryService.findByCoordinates(query);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        result.forEach(artifactEntry ->
                       {

                           databaseTx.activateOnCurrentThread();
                           ArtifactEntry artifact = databaseTx.detachAll(artifactEntry, true);

                           logger.info("Found artifact " + artifact);
                       });
    }

    private void displayAllEntries()
    {
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

    private ArtifactEntry createArtifactEntry(ArtifactCoordinates coordinates,
                                              String storageId,
                                              String repositoryId)
    {

        ArtifactEntry artifactEntry = new ArtifactEntry();
        artifactEntry.setArtifactCoordinates(coordinates);
        artifactEntry.setStorageId("storage0");
        artifactEntry.setRepositoryId("release");

        databaseTx.activateOnCurrentThread();
        return databaseTx.detachAll(artifactEntryService.save(artifactEntry), true);
    }

    private ArtifactCoordinates createMavenArtifactCoordinates()
    {

        return new MavenArtifactCoordinates("org.carlspring.strongbox",
                                            "coordinates-test",
                                            "1.2.3",
                                            null,
                                            "jar");
    }
}
