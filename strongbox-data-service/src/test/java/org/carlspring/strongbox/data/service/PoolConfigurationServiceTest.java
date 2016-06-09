package org.carlspring.strongbox.data.service;

import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.data.domain.PoolConfiguration;
import org.carlspring.strongbox.data.repository.PoolConfigurationRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

/**
 * @author korest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DataServiceConfig.class })
@Rollback
public class PoolConfigurationServiceTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolConfigurationServiceTest.class);

    @Autowired
    private PoolConfigurationService poolConfigurationService;
    @Autowired
    private PoolConfigurationRepository poolConfigurationRepository;

    private String repositoryUrl = "http://repo.spring.io/snapshot";

    @Test
    public void createOrUpdateNumberOfConnectionsForRepositoryTest()
    {
        PoolConfiguration savedPoolConfiguration =
                poolConfigurationService.createOrUpdateNumberOfConnectionsForRepository(repositoryUrl, 5);
        assertNotNull(savedPoolConfiguration.getId());
        assertEquals(5, savedPoolConfiguration.getMaxConnections());

        Optional<PoolConfiguration> poolConfigurationOptional = poolConfigurationService.findByRepositoryUrl(repositoryUrl);
        poolConfigurationOptional.ifPresent(poolConfiguration -> {
            assertEquals(repositoryUrl, poolConfiguration.getRepositoryUrl());
            assertEquals(5, poolConfiguration.getMaxConnections());
        });

        poolConfigurationOptional.orElseThrow(
                () -> new RuntimeException("Pool configuration for repository url " + repositoryUrl + " not found"));

        savedPoolConfiguration =
                poolConfigurationService.createOrUpdateNumberOfConnectionsForRepository(repositoryUrl, 3);
        assertNotNull(savedPoolConfiguration.getId());

        poolConfigurationOptional = poolConfigurationService.findByRepositoryUrl(repositoryUrl);
        poolConfigurationOptional.ifPresent(poolConfiguration -> {
            assertEquals(repositoryUrl, poolConfiguration.getRepositoryUrl());
            assertEquals(3, poolConfiguration.getMaxConnections());
        });

        poolConfigurationOptional.orElseThrow(
                () -> new RuntimeException("Pool configuration for repository url " + repositoryUrl + " not found"));
    }

    @Test
    public void findByRepositoryUrlTest()
    {
        PoolConfiguration savedPoolConfiguration =
                poolConfigurationService.createOrUpdateNumberOfConnectionsForRepository(repositoryUrl, 10);
        assertNotNull(savedPoolConfiguration.getId());
        assertEquals(10, savedPoolConfiguration.getMaxConnections());

        Optional<PoolConfiguration> dbFindByRepositoryUrl = poolConfigurationService.findByRepositoryUrl(repositoryUrl);
        dbFindByRepositoryUrl.ifPresent(poolConfiguration -> {
            assertEquals(repositoryUrl, poolConfiguration.getRepositoryUrl());
            assertEquals(10, poolConfiguration.getMaxConnections());
        });

        dbFindByRepositoryUrl.orElseThrow(
                () -> new RuntimeException("Pool configuration for repository url " + repositoryUrl + " not found"));
    }

    @Test
    public void findAllTest()
    {
        poolConfigurationRepository.deleteAll();

        Optional<Iterable<PoolConfiguration>> emptyPoolConfigurationsOptional = poolConfigurationService.findAll();
        emptyPoolConfigurationsOptional.ifPresent(poolConfigurations -> {
            assertEquals(0, StreamSupport.stream(poolConfigurations.spliterator(), false).count());
        });

        PoolConfiguration savedPoolConfiguration1 =
                poolConfigurationService.createOrUpdateNumberOfConnectionsForRepository(repositoryUrl, 10);
        assertNotNull(savedPoolConfiguration1.getId());
        String secondRepositoryUrl = "https://repo.maven.apache.org/maven2/";
        PoolConfiguration savedPoolConfiguration2 =
                poolConfigurationService.createOrUpdateNumberOfConnectionsForRepository(secondRepositoryUrl, 5);
        assertNotNull(savedPoolConfiguration2.getId());

        Optional<Iterable<PoolConfiguration>> poolConfigurationsOptional = poolConfigurationService.findAll();
        poolConfigurationsOptional.ifPresent(poolConfigurations -> {
            assertEquals(2, StreamSupport.stream(poolConfigurations.spliterator(), false).count());
            Set<String> ids = StreamSupport.stream(poolConfigurations.spliterator(), false).map(PoolConfiguration::getId).collect(
                    Collectors.toSet());
            assertTrue(ids.contains(savedPoolConfiguration1.getId()));
            assertTrue(ids.contains(savedPoolConfiguration2.getId()));

        });

        poolConfigurationsOptional.orElseThrow(() -> new RuntimeException("Pool configurations are empty"));
    }

}
