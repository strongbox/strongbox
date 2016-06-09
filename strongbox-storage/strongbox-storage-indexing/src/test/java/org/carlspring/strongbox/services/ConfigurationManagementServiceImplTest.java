package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ConfigurationManagementServiceImplTest
        extends TestCaseWithArtifactGenerationWithIndexing
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementServiceImplTest.class);

    @Autowired
    private ConfigurationManagementService configurationManagementService;

    @Test
    public void testGetGroupRepositories() throws Exception
    {
        List<Repository> groupRepositories = configurationManagementService.getGroupRepositories();

        assertFalse(groupRepositories.isEmpty());

        System.out.println("Group repositories:");

        for (Repository repository : groupRepositories)
        {
            System.out.println(" - " + repository.getId());
        }
    }

    @Test
    public void testGetGroupRepositoriesContainingRepository() throws Exception
    {
        List<Repository> groupRepositoriesContainingReleases = configurationManagementService.getGroupRepositoriesContaining("releases");

        assertFalse(groupRepositoriesContainingReleases.isEmpty());

        System.out.println("Group repositories containing \"releases\" repository:");

        for (Repository repository : groupRepositoriesContainingReleases)
        {
            System.out.println(" - " + repository.getId());
        }
    }

    @Test
    public void testRemoveRepositoryFromAssociatedGroups() throws Exception
    {
        Storage storage = configurationManagementService.getStorage("storage0");

        Repository repository = new Repository("test-repository-releases");
        repository.setType(RepositoryTypeEnum.HOSTED.getType());
        repository.setStorage(storage);

        Repository groupRepository1 = new Repository("test-group-repository-releases-1");
        groupRepository1.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository1.getGroupRepositories().add(repository.getId());
        groupRepository1.setStorage(storage);

        Repository groupRepository2 = new Repository("test-group-repository-releases-2");
        groupRepository2.setType(RepositoryTypeEnum.GROUP.getType());
        groupRepository2.getGroupRepositories().add(repository.getId());
        groupRepository2.setStorage(storage);

        configurationManagementService.addOrUpdateRepository("storage0", repository);
        configurationManagementService.addOrUpdateRepository("storage0", groupRepository1);
        configurationManagementService.addOrUpdateRepository("storage0", groupRepository2);

        assertEquals("Failed to add repository to group!",
                     2,
                     configurationManagementService.getGroupRepositoriesContaining("test-repository-releases").size());

        configurationManagementService.removeRepositoryFromAssociatedGroups("test-repository-releases");

        assertEquals("Failed to remove repository from all associated groups!",
                     0,
                     configurationManagementService.getGroupRepositoriesContaining("test-repository-releases").size());

        configurationManagementService.removeRepository("storage0", groupRepository1.getId());
        configurationManagementService.removeRepository("storage0", groupRepository2.getId());
    }

}
