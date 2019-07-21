package org.carlspring.strongbox.repository.group;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
public abstract class BaseMavenGroupRepositoryComponentTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected Set<RepositoryDto> getRepositories()
    {
        return Collections.emptySet();
    }

    @Override
    public void createRepository(final String storageId,
                                 final RepositoryDto repository)
            throws RepositoryManagementStrategyException, JAXBException, IOException
    {
        MavenRepositoryConfigurationDto configuration = new MavenRepositoryConfigurationDto();
        configuration.setIndexingEnabled(true);

        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setAllowsForceDeletion(true);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository.setRepositoryConfiguration(configuration);

        super.createRepository(storageId, repository);
    }

    protected RepositoryDto createGroup(String repositoryId,
                                            String storageId,
                                            String... leafs)
            throws Exception
    {
        RepositoryDto repository = new RepositoryDto(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.GROUP.getType());
        repository.setGroupRepositories(Sets.newLinkedHashSet(Arrays.asList(leafs)));

        createRepository(storageId, repository);

        return repository;
    }

    protected void copyArtifactMetadata(String sourceRepositoryId,
                                        String destinationRepositoryId,
                                        String path)
            throws IOException
    {
        final Storage storage = getConfiguration().getStorage(STORAGE0);

        Repository repository = storage.getRepository(sourceRepositoryId);
        final Path sourcePath = repositoryPathResolver.resolve(repository, path);

        repository = storage.getRepository(destinationRepositoryId);
        final Path destinationPath = repositoryPathResolver.resolve(repository, path);
        FileUtils.copyFile(sourcePath.toFile(), destinationPath.toFile());
    }

}
