package org.carlspring.strongbox.config.orientdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.strongbox.db.server.OrientDbServerConfiguration;
import org.strongbox.db.server.OrientDbServerProperties;
import org.strongbox.db.server.OrientDbStudioConfiguration;
import org.strongbox.db.server.OrientDbStudioProperties;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.OrientDBConfig;

/**
 * @author Przemyslaw Fusik
 */
abstract class CommonOrientDbConfig
{

    private OrientDBConfig orientDBConfig = OrientDBConfig.builder()
                                                          .addConfig(OGlobalConfiguration.DB_POOL_MIN, 1L)
                                                          .addConfig(OGlobalConfiguration.DB_POOL_MAX, 100L)
                                                          .build();

    @Bean
    @DependsOn("connectionConfig")
    public OrientDbStudioConfiguration orientDbStudioProperties(@Value("${strongbox.orientdb.studio.enabled}") boolean studioEnabled,
                                                                @Value("${strongbox.orientdb.studio.ip.address}") String studioIpAddress,
                                                                @Value("${strongbox.orientdb.studio.port}") int studioPort,
                                                                OrientDbServerConfiguration serverConfiguration)
    {
        OrientDbStudioProperties studioProperties = new OrientDbStudioProperties();
        studioProperties.setEnabled(studioEnabled);
        studioProperties.setIpAddress(studioIpAddress);
        studioProperties.setPort(studioPort);

        studioProperties.setPath(serverConfiguration.getPath());
        
        return studioProperties;
    }

    @Bean
    @DependsOn("connectionConfig")
    public OrientDbServerConfiguration orientDbServerProperties(@Value("${strongbox.orientdb.server.protocol}") String protocol,
                                                                @Value("${strongbox.orientdb.server.host}") String host,
                                                                @Value("${strongbox.orientdb.server.port}") String port,
                                                                @Value("${strongbox.orientdb.server.database}") String database,
                                                                @Value("${strongbox.orientdb.server.username}") String username,
                                                                @Value("${strongbox.orientdb.server.password}") String password,
                                                                @Value("${strongbox.server.database.path}") String path)
    {
        OrientDbServerProperties serverProperties = new OrientDbServerProperties();
        serverProperties.setUsername(username);
        serverProperties.setPassword(password);
        serverProperties.setHost(host);
        serverProperties.setPort(port);
        serverProperties.setProtocol(protocol);
        serverProperties.setPath(path);
        serverProperties.setDatabase(database);

        return serverProperties;
    }

    public OrientDBConfig getOrientDBConfig()
    {
        return orientDBConfig;
    }

}
