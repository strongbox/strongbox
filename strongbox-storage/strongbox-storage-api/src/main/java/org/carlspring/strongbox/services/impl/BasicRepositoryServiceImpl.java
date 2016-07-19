package org.carlspring.strongbox.services.impl;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.services.BasicRepositoryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component("basicRepositoryService")
public class BasicRepositoryServiceImpl
        implements BasicRepositoryService
{

    @Autowired
    private ConfigurationManager configurationManager;


    @Override
    public boolean containsArtifact(Repository repository, Artifact artifact)
    {
        if (!repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

            final File repositoryBasedir = new File(repository.getStorage().getBasedir(), repository.getId());
            final File artifactFile = new File(repositoryBasedir, artifactPath).getAbsoluteFile();

            return artifactFile.exists();
        }
        else if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            for (String storageAndRepositoryId : repository.getGroupRepositories())
            {
                String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");
                String storageId = storageAndRepositoryIdTokens.length == 2 ?
                                   storageAndRepositoryIdTokens[0] :
                                   repository.getStorage().getId();
                String repositoryId = storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];

                Repository r = getConfiguration().getStorage(storageId).getRepository(repositoryId);

                if (containsArtifact(r, artifact))
                {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public boolean containsPath(Repository repository, String path)
    {
        if (!repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            final File repositoryBasedir = new File(repository.getStorage().getBasedir(), repository.getId());
            final File artifactFile = new File(repositoryBasedir, path).getAbsoluteFile();

            return artifactFile.exists();
        }
        else if (repository.getType().equals(RepositoryTypeEnum.GROUP.getType()))
        {
            for (String storageAndRepositoryId : repository.getGroupRepositories())
            {
                String[] storageAndRepositoryIdTokens = storageAndRepositoryId.split(":");
                String storageId = storageAndRepositoryIdTokens.length == 2 ?
                                   storageAndRepositoryIdTokens[0] :
                                   repository.getStorage().getId();
                String repositoryId = storageAndRepositoryIdTokens[storageAndRepositoryIdTokens.length < 2 ? 0 : 1];

                Repository r = getConfiguration().getStorage(storageId).getRepository(repositoryId);

                if (containsPath(r, path))
                {
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    @Override
    public String getPathToArtifact(Repository repository, Artifact artifact)
    {
        final String artifactPath = ArtifactUtils.convertArtifactToPath(artifact);

        final File repositoryBasedir = new File(repository.getStorage().getBasedir(), repository.getId());
        final File artifactFile = new File(repositoryBasedir, artifactPath);

        return artifactFile.getAbsolutePath();
    }

    public ConfigurationManager getConfigurationManager()
    {
        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    @Override
    public Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
