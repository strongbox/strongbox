package org.carlspring.strongbox.repository;

import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Przemyslaw Fusik
 */
public class MavenGroupRepositoryComponent
{

    @Inject
    private ConfigurationManagementService configurationManagementService;

    public void doIt(String storageId, String repositoryId) {
        List<Repository> groupRepositories = configurationManagementService.getGroupRepositoriesContaining(repositoryId);
        if (CollectionUtils.isEmpty(groupRepositories)) {
            return;
        }
    }

}
