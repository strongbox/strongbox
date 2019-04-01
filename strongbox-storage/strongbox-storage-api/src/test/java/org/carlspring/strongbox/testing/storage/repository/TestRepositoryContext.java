package org.carlspring.strongbox.testing.storage.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PreDestroy;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.UndeclaredThrowableException;

/**
 * This class manages the resources used within {@link Repository}.
 * 
 * @author sbespalov
 *
 */
public class TestRepositoryContext implements AutoCloseable, Comparable<TestRepositoryContext>
{

    private static final Logger logger = LoggerFactory.getLogger(TestRepositoryContext.class);
    
    private final TestRepository testRepository;

    private final ConfigurationManagementService configurationManagementService;

    private final RepositoryPathResolver repositoryPathResolver;

    private final RepositoryManagementService repositoryManagementService;
    
    private final StorageManagementService storageManagementService;

    private boolean opened;

    public TestRepositoryContext(TestRepository testRepository,
                                 ConfigurationManagementService configurationManagementService,
                                 RepositoryPathResolver repositoryPathResolver,
                                 RepositoryManagementService repositoryManagementService,
                                 StorageManagementService storageManagementService)
        throws IOException,
        RepositoryManagementStrategyException
    {
        this.testRepository = testRepository;
        this.configurationManagementService = configurationManagementService;
        this.repositoryPathResolver = repositoryPathResolver;
        this.repositoryManagementService = repositoryManagementService;
        this.storageManagementService = storageManagementService;
        
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
        logger.info(String.format("Create [%s] with id [%s] ", TestRepository.class.getSimpleName(), id(testRepository)));
        Storage storage = Optional.ofNullable(configurationManagementService.getConfiguration()
                                                                            .getStorage(testRepository.storage()))
                                  .orElseGet(this::createStorage);

        if (configurationManagementService.getConfiguration()
                                          .getRepository(testRepository.storage(),
                                                         testRepository.repository()) != null)
        {
            throw new IOException(String.format("Repository [%s] already exists.", id(testRepository)));
        }

        MutableRepository repository = new MutableRepository(testRepository.repository());
        repository.setLayout(testRepository.layout());

        Arrays.stream(testRepository.setup()).forEach(s -> setupRepository(s, repository));
        
        configurationManagementService.saveRepository(testRepository.storage(), (MutableRepository) repository);
        repositoryManagementService.createRepository(storage.getId(), repository.getId());
        final RepositoryPath repositoryPath = repositoryPathResolver.resolve(new ImmutableRepository(repository, storage));
        if (!Files.exists(repositoryPath))
        {
            throw new IOException(String.format("Failed to create repository [%s].", repositoryPath));
        }

        opened = true;
        logger.info(String.format("Created [%s] with id [%s] ", TestRepository.class.getSimpleName(), id(testRepository)));
    }

    private void setupRepository(Class<? extends RepositorySetup> s, MutableRepository repository)
    {
        RepositorySetup repositorySetup;
        try
        {
            repositorySetup = s.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new UndeclaredThrowableException(e);
        }
        repositorySetup.setup(repository);
    }

    private Storage createStorage()
    {
        MutableStorage newStorage = new MutableStorage(testRepository.storage());
        configurationManagementService.addStorageIfNotExists(newStorage);
        try
        {
            storageManagementService.saveStorage(newStorage);
        }
        catch (IOException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        return configurationManagementService.getConfiguration()
                                             .getStorage(testRepository.storage());
    }

    @PreDestroy
    public void close()
        throws IOException
    {
        logger.info(String.format("Close [%s] with id [%s] ", TestRepository.class.getSimpleName(), id(testRepository)));
        if (testRepository.cleanup())
        {
            repositoryManagementService.removeRepository(testRepository.storage(), testRepository.repository());
        }

        configurationManagementService.removeRepository(testRepository.storage(), testRepository.repository());
        
        opened = false;
        logger.info(String.format("Closed [%s] with id [%s] ", TestRepository.class.getSimpleName(), id(testRepository)));
    }

    @Override
    public int compareTo(TestRepositoryContext o)
    {
        return id(getTestRepository()).compareTo(id(o.getTestRepository()));
    }

    public static String id(TestRepository tr)
    {
        return id(tr.storage(), tr.repository());
    }

    public static String id(String storageId, String repositoryId)
    {
        return String.format("%s/%s", storageId, repositoryId);
    }
    
}
