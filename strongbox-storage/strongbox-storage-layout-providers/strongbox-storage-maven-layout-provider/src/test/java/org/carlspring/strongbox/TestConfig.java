package org.carlspring.strongbox;

import org.carlspring.strongbox.config.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ Maven2LayoutProviderConfig.class,
          MockedIndexResourceFetcherConfig.class,
          StorageCoreConfig.class,
          StorageApiConfig.class,
          CommonConfig.class,
          ClientConfig.class,
          DataServiceConfig.class,
          TestingCoreConfig.class
})
public class TestConfig
{

}
