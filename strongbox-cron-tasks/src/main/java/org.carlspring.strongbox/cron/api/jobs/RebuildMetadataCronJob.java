package org.carlspring.strongbox.cron.api.jobs;

import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Kate Novik
 */
public class RebuildMetadataCronJob extends JavaCronJob {

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
            artifactMetadataService.rebuildMetadata(storageId, repositoryId, basePath);
        }
        catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            logger.error("IOException: ", e);
        }

    }
}
