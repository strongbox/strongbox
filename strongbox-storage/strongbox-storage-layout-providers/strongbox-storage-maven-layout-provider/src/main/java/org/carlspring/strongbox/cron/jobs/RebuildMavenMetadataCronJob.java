package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Kate Novik
 */
public class RebuildMavenMetadataCronJob
        extends JavaCronJob
{

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private JobManager manager;


    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");
        String basePath = config.getProperty("basePath");

        if (storageId == null)
        {
            Map<String, Storage> storages = getStorages();
            for (String storage : storages.keySet())
            {
                rebuildRepositories(storage);
            }
        }
        else if (repositoryId == null)
        {
            rebuildRepositories(storageId);
        }
        else
        {
            artifactMetadataService.rebuildMetadata(storageId, repositoryId, basePath);
        }
    }

    /**
     * To rebuild artifact's metadata in repositories
     *
     * @param storageId path of storage
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void rebuildRepositories(String storageId)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        Map<String, Repository> repositories = getRepositories(storageId);

        for (String repository : repositories.keySet())
        {
            artifactMetadataService.rebuildMetadata(storageId, repository, null);
        }
    }

    private Map<String, Storage> getStorages()
    {
        return configurationManager.getConfiguration().getStorages();
    }

    private Map<String, Repository> getRepositories(String storageId)
    {
        return getStorages().get(storageId).getRepositories();
    }

}
