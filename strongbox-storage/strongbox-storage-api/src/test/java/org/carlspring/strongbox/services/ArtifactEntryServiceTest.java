package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;

import javax.inject.Inject;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Functional test and usage example scenarios for {@link ArtifactEntryService}.
 *
 * @author Alex Oreshkevich
 * @see https://dev.carlspring.org/youtrack/issue/SB-711
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { StorageApiConfig.class })
public class ArtifactEntryServiceTest
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactEntryServiceTest.class);

    @Inject
    ArtifactEntryService artifactEntryService;

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Test
    public void testThatArtifactEntryIsCreatable()
    {

        artifactEntryService.deleteAll();

        ArtifactCoordinates artifactCoordinates = new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                                               "coordinates-test",
                                                                               "1.2.3",
                                                                               null,
                                                                               "jar");

        ArtifactEntry artifactEntry = new ArtifactEntry();
        //  artifactEntry.setArtifactCoordinates(artifactCoordinates);
        artifactEntry.setRepositoryId("release");
        artifactEntry.setStorageId("storage0");

        artifactEntry = databaseTx.detachAll(artifactEntryService.save(artifactEntry), true);

        logger.info("Saved entity " + artifactEntry);

        ArtifactEntry savedEntry = databaseTx.detachAll(artifactEntryService.findAll()
                                                                            .orElseThrow(
                                                                                    () -> new NullPointerException("Unable to find any artifact entry"))
                                                                            .get(0), true);

        logger.info("Detached entity " + savedEntry);

        assertTrue(artifactEntryService.count() > 0);
        assertEquals("storage0", savedEntry.getStorageId());
        assertEquals("release", savedEntry.getRepositoryId());
    }
}
