package org.carlspring.strongbox.cron.jobs;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.config.NpmLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.testing.NpmRepositoryTestCase;
import org.carlspring.strongbox.xml.configuration.repository.remote.MutableNpmRemoteRepositoryConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = NpmLayoutProviderCronTasksTestConfig.class)
@ActiveProfiles(profiles = "test")
@RunWith(SpringJUnit4ClassRunner.class)
public class FetchChangesFeedCronJobTestIT
        extends NpmRepositoryTestCase
{

    private static final String STORAGE = "test-npm-storage";

    private static final String REPOSITORY = "fcfcjt-releases";

    @Rule
    public TestRule watcher = new TestWatcher()
    {
        @Override
        protected void starting(final Description description)
        {
            // expectedJobName = description.getMethodName();
        }
    };

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    protected StorageManagementService storageManagementService;

    @Inject
    private JobManager jobManager;

    @BeforeClass
    public static void cleanUp()
        throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @After
    public void removeRepositories()
        throws IOException,
        JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Before
    public void initialize()
        throws Exception
    {

        String jobName = FetchRemoteChangesFeedCronJob.calculateJobName(STORAGE, REPOSITORY);
        jobManager.registerExecutionListener(jobName, (jobName1,
                                                       statusExecuted) -> {
            if (!jobName1.equals(jobName) || !statusExecuted)
            {
                return;
            }
        });

        createStorage(STORAGE);

        MutableRepository repository = createRepositoryMock(STORAGE, REPOSITORY, NpmLayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());
        
        MutableRemoteRepository remoteRepository = new MutableRemoteRepository();
        repository.setRemoteRepository(remoteRepository);

        remoteRepository.setUrl("https://registry.npmjs.org");

        MutableNpmRemoteRepositoryConfiguration remoteRepositoryConfiguration = new MutableNpmRemoteRepositoryConfiguration();
        remoteRepository.setCustomConfiguration(remoteRepositoryConfiguration);

        remoteRepositoryConfiguration.setReplicateUrl("https://replicate.npmjs.com");

        createRepository(STORAGE, repository);

        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setName("testRegenerateNugetPackageChecksum");
        configuration.addProperty("jobClass", FetchRemoteChangesFeedCronJob.class.getName());
        configuration.addProperty("cronExpression", "0 0 * ? * * *"); // Execute
                                                                      // every
                                                                      // hour
        configuration.addProperty("storageId", STORAGE);
        configuration.addProperty("repositoryId", REPOSITORY);
        configuration.setImmediateExecution(true);
        configuration.setOneTimeExecution(true);

        cronTaskConfigurationService.saveConfiguration(configuration);

        CronTaskConfigurationDto cronTaskConfiguration = cronTaskConfigurationService.getTaskConfigurationDto("testRegenerateNugetPackageChecksum");
        assertNotNull(cronTaskConfiguration);

    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE, REPOSITORY, NpmLayoutProvider.ALIAS));
        return repositories;
    }

    @Test
    public void testRegenerateNugetPackageChecksum()
        throws Exception
    {
        Thread.sleep(3600000);
    }

}
