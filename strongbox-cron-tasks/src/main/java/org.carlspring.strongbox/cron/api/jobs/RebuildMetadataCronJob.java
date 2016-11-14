package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    private static final String BASEDIR_STORAGES = "/storages";

    private final Logger logger = LoggerFactory.getLogger(RebuildMetadataCronJob.class);

    @Autowired
    ArtifactMetadataService artifactMetadataService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("Executed RebuildMetadataCronJob.");

        try
        {
            String storageId = getConfiguration().getProperty("storageId");
            String repositoryId = getConfiguration().getProperty("repositoryId");
            String basePath = getConfiguration().getProperty("basePath");
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
        String storagesBasePath = ConfigurationResourceResolver.getVaultDirectory().concat(BASEDIR_STORAGES);
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
                    listDirectories.add(file.getAbsolutePath());
                }
            }
        }
        return listDirectories;
    }

}
