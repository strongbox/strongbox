package org.carlspring.strongbox.data;

import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * @author sbespalov
 *
 */
public class CacheManagerTestExecutionListener extends AbstractTestExecutionListener
{

    @Override
    public void beforeTestClass(TestContext testContext)
        throws Exception
    {

    }

    @Override
    public void afterTestClass(TestContext testContext)
        throws Exception
    {
        clearCache(testContext);
    }

    @Override
    public void afterTestMethod(TestContext testContext)
        throws Exception
    {
        clearCache(testContext);
    }

    private void clearCache(TestContext testContext)
    {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        
        CacheManager cacheManager = applicationContext.getBean(CacheManager.class);
        cacheManager.getCacheNames().parallelStream().forEach(name -> cacheManager.getCache(name).clear());
    }

    
}
