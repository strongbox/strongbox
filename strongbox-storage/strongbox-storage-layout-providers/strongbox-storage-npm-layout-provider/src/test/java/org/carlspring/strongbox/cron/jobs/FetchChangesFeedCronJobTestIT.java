package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.NpmLayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.data.criteria.Expression.ExpOperator;
import org.carlspring.strongbox.data.criteria.OQueryTemplate;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.data.criteria.Selector;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.MutableRemoteRepository;
import org.carlspring.strongbox.testing.NpmRepositoryTestCase;
import org.carlspring.strongbox.yaml.configuration.repository.remote.NpmRemoteRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.configuration.repository.remote.NpmRemoteRepositoryConfiguration;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = { NpmLayoutProviderCronTasksTestConfig.class })
@ActiveProfiles(profiles = { "test", "FetchChangesFeedCronJobTestConfig" })
@SpringBootTest
@Execution(CONCURRENT)
public class FetchChangesFeedCronJobTestIT
        extends NpmRepositoryTestCase implements ApplicationListener<CronTaskEvent>, ApplicationContextAware
{

    private static final long EVENT_TIMEOUT_SECONDS = 3600L;

    private static final String EMPTY_FEED = "{\"results\": [],\"last_seq\": 322}";

    private static final String STORAGE = "test-npm-storage";

    private static final String REPOSITORY = "fcfcjt-releases";

    private UUID expectedJobKey;

    private String expectedJobName;

    protected AtomicInteger receivedExpectedEvent = new AtomicInteger(0);

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @BeforeAll
    public static void cleanUp()
        throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        expectedJobKey = UUID.randomUUID();

        Optional<Method> method = testInfo.getTestMethod();
        expectedJobName = method.map(Method::getName).orElse(null);
    }

    @AfterEach
    public void removeRepositories()
        throws IOException,
        JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    @BeforeEach
    public void initialize()
        throws Exception
    {

        createStorage(STORAGE);

        RepositoryDto repository = createRepositoryMock(STORAGE, REPOSITORY, NpmLayoutProvider.ALIAS);
        repository.setType(RepositoryTypeEnum.PROXY.getType());

        MutableRemoteRepository remoteRepository = new MutableRemoteRepository();
        repository.setRemoteRepository(remoteRepository);

        remoteRepository.setUrl("https://registry.npmjs.org");

        NpmRemoteRepositoryConfigurationDto remoteRepositoryConfiguration = new NpmRemoteRepositoryConfigurationDto();
        remoteRepository.setCustomConfiguration(remoteRepositoryConfiguration);

        remoteRepositoryConfiguration.setReplicateUrl("https://replicate.npmjs.com");

        createRepository(STORAGE, repository);

        prepareArtifactResolverContext(this.getClass().getResourceAsStream("changesFeed.json"));
    }

    public static Set<RepositoryDto> getRepositoriesToClean()
    {
        Set<RepositoryDto> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE, REPOSITORY, NpmLayoutProvider.ALIAS));
        return repositories;
    }

    @Test
    @Transactional
    public void testFetchChangesFeed()
        throws Exception
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setUuid(expectedJobKey);
        configuration.setName(expectedJobName);
        configuration.setJobClass(TestFetchRemoteChangesFeedCronJob.class.getName());
        configuration.setCronExpression("0 0 * ? * * *");
        configuration.addProperty("storageId", STORAGE);
        configuration.addProperty("repositoryId", REPOSITORY);
        configuration.setImmediateExecution(true);

        cronTaskConfigurationService.saveConfiguration(configuration);

        Awaitility.await().timeout(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAtomic(receivedExpectedEvent,
                                                                                        org.hamcrest.Matchers.equalTo(1));

        Selector<RemoteArtifactEntry> selector = new Selector<>(RemoteArtifactEntry.class);
        selector.where(Predicate.of(ExpOperator.EQ.of("storageId", STORAGE)))
                .and(Predicate.of(ExpOperator.EQ.of("repositoryId", REPOSITORY)))
                .and(Predicate.of(ExpOperator.EQ.of("artifactCoordinates.coordinates.name", "MiniMVC")));

        OQueryTemplate<List<RemoteArtifactEntry>, RemoteArtifactEntry> queryTemplate = new OQueryTemplate<>(
                entityManager);
        List<RemoteArtifactEntry> artifactEntryList = queryTemplate.select(selector);

        assertEquals(1, artifactEntryList.size());

        RemoteArtifactEntry artifactEntry = artifactEntryList.iterator().next();
        assertFalse(artifactEntry.getIsCached());

        Repository repository = configurationManagementService.getConfiguration().getRepository(STORAGE, REPOSITORY);
        NpmRemoteRepositoryConfiguration customConfiguration = (NpmRemoteRepositoryConfiguration) ((RepositoryData) repository).getRemoteRepository()
                                                                                                                                    .getCustomConfiguration();
        assertEquals(Long.valueOf(330), customConfiguration.getLastChangeId());
    }

    public static class TestFetchRemoteChangesFeedCronJob extends FetchRemoteNpmChangesFeedCronJob
    {

        @Override
        public boolean enabled(CronTaskConfigurationDto configuration,
                               Environment env)
        {
            return true;
        }

    }

    void prepareArtifactResolverContext(InputStream feedInputStream)
    {

        Client mockedRestClient = Mockito.mock(Client.class);

        Invocation mockedInvocation = Mockito.mock(Invocation.class);
        Mockito.when(mockedInvocation.invoke(InputStream.class))
               .thenReturn(feedInputStream, new ByteArrayInputStream(EMPTY_FEED.getBytes()));

        Invocation.Builder mockedBuilder = Mockito.mock(Invocation.Builder.class);
        Mockito.when(mockedBuilder.buildGet()).thenReturn(mockedInvocation);

        WebTarget mockedWebTarget = Mockito.mock(WebTarget.class);
        Mockito.when(mockedWebTarget.path(anyString())).thenReturn(mockedWebTarget);
        Mockito.when(mockedWebTarget.queryParam(anyString(), any()))
               .thenReturn(mockedWebTarget);

        Mockito.when(mockedWebTarget.request()).thenReturn(mockedBuilder);

        Mockito.when(mockedRestClient.target(anyString())).thenReturn(mockedWebTarget);

        Mockito.when(proxyRepositoryConnectionPoolConfigurationService.getRestClient())
               .thenReturn(mockedRestClient);
    }

    @Override
    public void onApplicationEvent(CronTaskEvent event)
    {
        if (event.getType() != CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()
                || !StringUtils.equals(expectedJobKey.toString(), event.getName()))
        {
            return;
        }

        receivedExpectedEvent.incrementAndGet();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException
    {
        ((ConfigurableApplicationContext) applicationContext).addApplicationListener(this);
    }

    @Profile("FetchChangesFeedCronJobTestConfig")
    @Configuration
    public static class FetchChangesFeedCronJobTestConfig
    {

        @Primary
        @Bean
        public ProxyRepositoryConnectionPoolConfigurationService mockedProxyRepositoryConnectionPoolConfigurationService()
        {
            return Mockito.mock(ProxyRepositoryConnectionPoolConfigurationService.class);
        }

    }
}
