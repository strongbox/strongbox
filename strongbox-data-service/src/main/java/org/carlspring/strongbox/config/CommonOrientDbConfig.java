package org.carlspring.strongbox.config;

import javax.inject.Inject;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.OrientDBConfig;

/**
 * @author Przemyslaw Fusik
 */
abstract class CommonOrientDbConfig
{
    static
    {
        // Has to be called before com.orientechnologies.orient.core.config.OGlobalConfiguration.readConfiguration
        System.setProperty("network.binary.maxLength", "64000");
    }

    @Inject
    protected ConnectionConfig connectionConfig;

    private OrientDBConfig orientDBConfig = OrientDBConfig.builder()
                                                          .addConfig(OGlobalConfiguration.DB_POOL_MIN, 1L)
                                                          .addConfig(OGlobalConfiguration.DB_POOL_MAX, 100L)
                                                          .build();

    OrientDBConfig getOrientDBConfig()
    {
        return orientDBConfig;
    }

}
