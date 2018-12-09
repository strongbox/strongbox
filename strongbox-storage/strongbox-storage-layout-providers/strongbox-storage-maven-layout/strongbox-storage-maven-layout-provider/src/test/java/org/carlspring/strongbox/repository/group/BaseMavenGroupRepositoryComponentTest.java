package org.carlspring.strongbox.repository.group;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.AfterEach;

/**
 * @author Przemyslaw Fusik
 */
public abstract class BaseMavenGroupRepositoryComponentTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected Set<MutableRepository> getRepositories()
    {
        return Collections.emptySet();
    }

    @Override
    public void createRepository(final String storageId,
                                 final MutableRepository repository)
            throws RepositoryManagementStrategyException, JAXBException, IOException
    {
        MutableMavenRepositoryConfiguration configuration = new MutableMavenRepositoryConfiguration();
        configuration.setIndexingEnabled(true);

        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setAllowsForceDeletion(true);
        repository.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
        repository.setRepositoryConfiguration(configuration);

        super.createRepository(storageId, repository);
    }

    protected void createLeaf(String storageId,
                              String repositoryId)
            throws Exception
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(new Random().nextInt(2) % 2 == 0 ?
                           RepositoryTypeEnum.HOSTED.getType() :
                           RepositoryTypeEnum.PROXY.getType());

        createRepository(storageId, repository);
    }

    protected MutableRepository createGroup(String repositoryId,
                                            String storageId,
                                            String... leafs)
            throws Exception
    {
        MutableRepository repository = new MutableRepository(repositoryId);
        repository.setLayout(Maven2LayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.GROUP.getType());
        repository.setGroupRepositories(Sets.newLinkedHashSet(Arrays.asList(leafs)));

        createRepository(storageId, repository);

        return repository;
    }

    @AfterEach
    public void removeRepositories()
            throws Exception
    {
        removeRepositories(getRepositories());
    }

}
