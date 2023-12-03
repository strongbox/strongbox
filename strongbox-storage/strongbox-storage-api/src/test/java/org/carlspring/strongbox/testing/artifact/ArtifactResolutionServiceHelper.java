package org.carlspring.strongbox.testing.artifact;

import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactResolutionService;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Pablo Tirado
 */
public class ArtifactResolutionServiceHelper
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactResolutionServiceHelper.class);

    @Inject
    protected ArtifactResolutionService artifactResolutionService;

    public void assertStreamNotNull(final String storageId,
                                    final String repositoryId,
                                    final String path)
            throws Exception
    {
        RepositoryPath repositoryPath = artifactResolutionService.resolvePath(storageId,
                                                                              repositoryId,
                                                                              path);

        try (final InputStream is = artifactResolutionService.getInputStream(repositoryPath))
        {
            assertThat(is).as("Failed to resolve " + path + "!").isNotNull();

            if (RepositoryFiles.isMetadata(repositoryPath))
            {
                logger.debug(Arrays.toString(IOUtils.toByteArray(is)));
            }
            else
            {
                while (is.read(new byte[1024]) != -1) ;
            }
        }
    }
}
