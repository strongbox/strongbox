package org.carlspring.strongbox.testing.storage.repository;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.StorageDto;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepositoryDto;
import org.carlspring.strongbox.storage.routing.MutableRoutingRule;
import org.carlspring.strongbox.storage.routing.MutableRoutingRuleRepository;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.carlspring.strongbox.util.ThrowingSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the resources used within {@link Repository}.
 * 
 * @author sbespalov
 */
public class TestRepositoryContext implements AutoCloseable, Comparable<TestRepositoryContext>
{

    private static final Logger logger = LoggerFactory.getLogger(TestRepositoryContext.class);

    private final TestRepository testRepository;

    private final Remote remoteRepository;
    
    private final Group groupRepository;
    
    private final RepositoryAttributes repositoryAttributes;

    private final ConfigurationManagementService configurationManagementService;

    private final RepositoryPathResolver repositoryPathResolver;

    private final RepositoryManagementService repositoryManagementService;

    private final StorageManagementService storageManagementService;

    private boolean opened;


    public TestRepositoryContext(TestRepository testRepository,
                                 Remote remoteRepository,
                                 Group groupRepository,
                                 RepositoryAttributes repositoryAttributes,
                                 ConfigurationManagementService configurationManagementService,
                                 RepositoryPathResolver repositoryPathResolver,
                                 RepositoryManagementService repositoryManagementService,
                                 StorageManagementService storageManagementService)
            throws IOException,
                   RepositoryManagementStrategyException
    {
        this.testRepository = testRepository;
        this.remoteRepository = remoteRepository;
        this.groupRepository = groupRepository;
        this.repositoryAttributes = repositoryAttributes;
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
                                                              .getRepository(testRepository.storageId(),
                                                                             testRepository.repositoryId());

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
        logger.info(String.format("Create [%s] with id [%s] ",
                                  TestRepository.class.getSimpleName(),
                                  id(testRepository)));

        if (groupRepository != null && remoteRepository != null)
        {
            throw new IllegalStateException(
                    String.format("The repository [%s] shoudn't be configured as [%s] and [%s] at the same time.",
                                  id(testRepository), Group.class.getSimpleName(),
                                  Remote.class.getSimpleName()));
        }
        
        Storage storage = Optional.ofNullable(configurationManagementService.getConfiguration()
                                                                            .getStorage(testRepository.storageId()))
                                  .orElseGet(ThrowingSupplier.unchecked(this::createStorage));

        if (configurationManagementService.getConfiguration()
                                          .getRepository(testRepository.storageId(),
                                                         testRepository.repositoryId()) != null)
        {
            throw new IOException(String.format("Repository [%s] already exists.", id(testRepository)));
        }

        RepositoryDto repository = new RepositoryDto(testRepository.repositoryId());
        repository.setLayout(testRepository.layout());
        repository.setPolicy(testRepository.policy().toString());

        Optional.ofNullable(remoteRepository).ifPresent(r -> {
            repository.setType(RepositoryTypeEnum.PROXY.getType());

            RemoteRepositoryDto remoteRepositoryConfiguration = new RemoteRepositoryDto();
            remoteRepositoryConfiguration.setUrl(r.url());
            repository.setRemoteRepository(remoteRepositoryConfiguration);
        });

        if (groupRepository != null)
        {
            repository.setType(RepositoryTypeEnum.GROUP.getType());
            repository.getGroupRepositories().addAll(Arrays.asList(groupRepository.repositories()));

            for (TestRepository.Group.Rule rule : groupRepository.rules())
            {
                MutableRoutingRule routingRule = MutableRoutingRule.create(testRepository.storageId(),
                                                                           testRepository.repositoryId(),
                                                                           routingRepositories(rule.repositories()),
                                                                           rule.pattern(),
                                                                           rule.type());
                configurationManagementService.addRoutingRule(routingRule);
            }
        }

        Optional.ofNullable(repositoryAttributes).ifPresent(a -> {
            repository.setAllowsDelete(repositoryAttributes.allowsDelete());
            repository.setAllowsDeployment(repositoryAttributes.allowsDeployment());
            repository.setAllowsDirectoryBrowsing(repositoryAttributes.allowsDirectoryBrowsing());
            repository.setAllowsForceDeletion(repositoryAttributes.allowsForceDeletion());
            repository.setAllowsRedeployment(repositoryAttributes.allowsRedeployment());
            repository.setChecksumHeadersEnabled(repositoryAttributes.checksumHeadersEnabled());
            repository.setStatus(repositoryAttributes.status().getStatus());
            repository.setTrashEnabled(repositoryAttributes.trashEnabled());
        });
        
        Arrays.stream(testRepository.setup()).forEach(s -> setupRepository(s, repository));

        configurationManagementService.saveRepository(testRepository.storageId(), repository);
        repositoryManagementService.createRepository(storage.getId(), repository.getId());

        final RepositoryPath repositoryPath = repositoryPathResolver.resolve(new RepositoryData(repository,
                                                                                                     storage));
        if (!Files.exists(repositoryPath))
        {
            throw new IOException(String.format("Failed to create repository [%s].", repositoryPath));
        }

        opened = true;

        logger.info(String.format("Created [%s] with id [%s] ",
                                  TestRepository.class.getSimpleName(),
                                  id(testRepository)));
    }

    private List<MutableRoutingRuleRepository> routingRepositories(String[] repositories)
    {
        return Arrays.stream(repositories).map(MutableRoutingRuleRepository::new).collect(Collectors.toList());
    }

    private void setupRepository(Class<? extends RepositorySetup> s,
                                 RepositoryDto repository)
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
            throws IOException
    {
        StorageDto newStorage = new StorageDto(testRepository.storageId());
        configurationManagementService.addStorageIfNotExists(newStorage);

        storageManagementService.saveStorage(newStorage);

        return configurationManagementService.getConfiguration().getStorage(testRepository.storageId());
    }

    @PreDestroy
    public void close()
        throws IOException
    {
        logger.info(String.format("Close [%s] with id [%s] ",
                                  TestRepository.class.getSimpleName(),
                                  id(testRepository)));

        try
        {
            closeInternal();
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to close [%s] with id [%s] ",
                                       TestRepository.class.getSimpleName(),
                                       id(testRepository)), e);

            throw e;
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to close [%s] with id [%s] ",
                                       TestRepository.class.getSimpleName(),
                                       id(testRepository)), e);

            throw new IOException(e);
        }

        logger.info(String.format("Closed [%s] with id [%s] ",
                                  TestRepository.class.getSimpleName(),
                                  id(testRepository)));
    }

    private void closeInternal()
        throws IOException
    {
        if (testRepository.cleanup())
        {
            repositoryManagementService.removeRepository(testRepository.storageId(), testRepository.repositoryId());
        }

        configurationManagementService.removeRepository(testRepository.storageId(), testRepository.repositoryId());

        opened = false;
    }

    @Override
    public int compareTo(TestRepositoryContext o)
    {
        return id(getTestRepository()).compareTo(id(o.getTestRepository()));
    }

    public static String id(TestRepository tr)
    {
        return id(tr.storageId(), tr.repositoryId());
    }

    public static String id(String storageId,
                            String repositoryId)
    {
        return String.format("%s/%s", storageId, repositoryId);
    }

}
