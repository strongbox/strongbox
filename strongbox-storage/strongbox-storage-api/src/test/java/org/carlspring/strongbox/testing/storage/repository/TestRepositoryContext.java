package org.carlspring.strongbox.testing.storage.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import javax.annotation.PreDestroy;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.core.Ordered;

/**
 * This class manages the resources used within {@link Repository}.
 * 
 * @author sbespalov
 *
 */
public class TestRepositoryContext implements AutoCloseable, Ordered, Comparable<TestRepositoryContext>
{

    private final TestRepository testRepository;

    private final ConfigurationManagementService configurationManagementService;

    private final RepositoryPathResolver repositoryPathResolver;

    private final RepositoryManagementService repositoryManagementService;

    private boolean opened;

    private int order;

    public TestRepositoryContext(TestRepository testRepository,
                                 ConfigurationManagementService configurationManagementService,
                                 RepositoryPathResolver repositoryPathResolver,
                                 RepositoryManagementService repositoryManagementService)
        throws IOException,
        RepositoryManagementStrategyException
    {
        this.testRepository = testRepository;
        this.configurationManagementService = configurationManagementService;
        this.repositoryPathResolver = repositoryPathResolver;
        this.repositoryManagementService = repositoryManagementService;

        open();
    }

    public TestRepository getTestRepository()
    {
        return testRepository;
    }

    public Repository getRepository()
    {
        if (!opened)
        {
            throw new IllegalStateException(String.format("Repository [%s] not found.", id(testRepository)));
        }

        Repository repository = configurationManagementService.getConfiguration()
                                                              .getRepository(testRepository.storage(),
                                                                             testRepository.repository());
        Objects.requireNonNull(repository, String.format("Repository [%s] not found.", id(testRepository)));
        return repository;
    }

    public boolean isOpened()
    {
        return opened;
    }

    protected void open()
        throws IOException,
        RepositoryManagementStrategyException
    {

        Storage storage = configurationManagementService.getConfiguration().getStorage(testRepository.storage());
        Objects.requireNonNull(storage, String.format("Storage [%s] not found.", testRepository.storage()));

        if (configurationManagementService.getConfiguration()
                                          .getRepository(testRepository.storage(),
                                                         testRepository.repository()) != null)
        {
            throw new IOException(String.format("Repository [%s] already exists.", id(testRepository)));
        }
        MutableRepository repository = new MutableRepository(testRepository.repository());
        repository.setLayout(testRepository.layout());
        configurationManagementService.saveRepository(testRepository.storage(), (MutableRepository) repository);

        final RepositoryPath repositoryPath = repositoryPathResolver.resolve(new ImmutableRepository(repository));
        if (Files.exists(repositoryPath))
        {
            throw new IOException(String.format("Repository [%s] already exists.", repositoryPath));
        }

        repositoryManagementService.createRepository(storage.getId(), repository.getId());

        opened = true;
    }

    @PreDestroy
    public void close()
        throws IOException
    {
        if (testRepository.cleanup())
        {
            repositoryManagementService.removeRepository(testRepository.storage(), testRepository.repository());
        }

        configurationManagementService.removeRepository(testRepository.storage(), testRepository.repository());

        opened = false;
    }

    @Override
    public int compareTo(TestRepositoryContext o)
    {
        return id(getTestRepository()).compareTo(id(o.getTestRepository()));
    }

    @Override
    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    public static String id(TestRepository tr)
    {
        return String.format("%s/%s", tr.storage(), tr.repository());
    }

}
