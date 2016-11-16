package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.cron.config.ApplicationContextProvider;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.impl.ArtifactMetadataServiceImpl;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kate Novik
 */
public class RebuildMetadataCronJob extends JavaCronJob {

    private static final String BASEDIR_STORAGES = "storages";

    private static final String storagesBasePath = getStoragesBasePath();

    private final Logger logger = LoggerFactory.getLogger(RebuildMetadataCronJob.class);

    private ArtifactMetadataServiceImpl artifactMetadataService = getArtifactMetadataService();

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed RebuildMetadataCronJob.");

        try
        {
            CronTaskConfiguration config = (CronTaskConfiguration) jobExecutionContext.getMergedJobDataMap().get("config");
            String storageId = config.getProperty("storageId");
            String repositoryId = config.getProperty("repositoryId");
            String basePath = config.getProperty("basePath");
            if (storageId == null) {
                List<String> storagesId = getStoragesId();
                for (String storage : storagesId) {
                    List<String> repositoriesId = getRepositoriesId(storage);
                    rebuildRepositories(repositoriesId, storage, basePath);
                }
            } else if (repositoryId == null) {
                List<String> repositoriesId = getRepositoriesId(storageId);
                rebuildRepositories(repositoriesId, storageId, basePath);
            } else {
                artifactMetadataService.rebuildMetadata(storageId, repositoryId, basePath);
            }
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            logger.error("IOException: ", e);
        }

    }

    /**
     * To rebuild artifact's metadata in repositories
     * @param repositories list of repositories
     * @param storageId path of storage
     * @param basePath basePath of artifact
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void rebuildRepositories(List<String> repositories, String storageId, String basePath)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException {
        for (String repository : repositories) {
            artifactMetadataService.rebuildMetadata(storageId, repository, basePath);
        }
    }

    /**
     * To get list of repositoryId in storage
     * @param storageId
     * @return list of repositoryId
     */
    private List<String> getRepositoriesId (String storageId) {
        File file = Paths.get(storageId).toFile();
        return getListDirectories(file);
    }

    /**
     * To get list of storageId
     * @return list of storageId
     */
    private List<String> getStoragesId () {
        File file = Paths.get(storagesBasePath).toFile();
        return getListDirectories(file);
    }

    /**
     * To get list of directories
     * @param baseFile Object File
     * @return list of directories
     */
    private List<String> getListDirectories (File baseFile) {
        List<String> listDirectories = new ArrayList<>();
        File[] listFiles = baseFile.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    String directoryId = file.getAbsolutePath().substring(0, storagesBasePath.length());
                    listDirectories.add(directoryId);
                }
            }
        }
        return listDirectories;
    }

    /**
     * To get base path of storages
     * @return String base path
     */
    private static String getStoragesBasePath () {
        return Paths.get(ConfigurationResourceResolver.getVaultDirectory(), BASEDIR_STORAGES).toString();
    }

    private ArtifactMetadataServiceImpl getArtifactMetadataService () {
        return ApplicationContextProvider.getApplicationContext().getBean(ArtifactMetadataServiceImpl.class);
    }

}
