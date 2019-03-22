package org.carlspring.strongbox.testing.storage.repository;

import static org.carlspring.strongbox.testing.artifact.TestArtifactContext.id;
import static org.carlspring.strongbox.testing.storage.repository.TestRepositoryContext.id;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.artifact.TestArtifactContext;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

/**
 * @author sbespalov
 */
public class TestRepositoryManagementApplicationContext extends AnnotationConfigApplicationContext
        implements TestRepositoryManagementContext
{

    private static final int REPOSITORY_LOCK_ATTEMPTS = 10;

    private static final Logger logger = LoggerFactory.getLogger(TestRepositoryManagementApplicationContext.class);

    private static ThreadLocal<TestRepositoryManagementContext> testApplicaitonContextHolder = new ThreadLocal<>();

    private Map<Class<? extends Annotation>, Boolean> extensionsToApply = new HashMap<>();

    private static Map<String, ReentrantLock> idSync = new ConcurrentSkipListMap<>();

    public static void registerExtension(Class<? extends Annotation> extensionType,
                                         ExtensionContext context)
    {
        TestRepositoryManagementApplicationContext testApplicationContext = (TestRepositoryManagementApplicationContext) getInstance();
        if (testApplicationContext == null)
        {
            ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
            Assert.notNull(applicationContext, "Application Context required.");

            testApplicationContext = new TestRepositoryManagementApplicationContext();
            testApplicationContext.setParent(applicationContext);

            testApplicaitonContextHolder.set(testApplicationContext);
        }

        if (testApplicationContext.extensionsToApply.containsKey(extensionType))
        {
            throw new IllegalStateException(String.format("Extensoon [%s] already registered.", extensionType));
        }
        testApplicationContext.extensionsToApply.put(extensionType, false);
    }

    public static void closeExtension(Class<? extends Annotation> extensionType,
                                      ExtensionContext context)
    {
        TestRepositoryManagementApplicationContext testApplicationContext = (TestRepositoryManagementApplicationContext) getInstance();
        if (testApplicationContext == null)
        {
            return;
        }

        testApplicationContext.extensionsToApply.remove(extensionType);
        if (!testApplicationContext.extensionsToApply.isEmpty())
        {
            return;
        }

        testApplicaitonContextHolder.remove();
        if (testApplicationContext.isActive())
        {
            testApplicationContext.close();
        }
    }

    public static TestRepositoryManagementContext getInstance()
    {
        return testApplicaitonContextHolder.get();
    }

    @Override
    public boolean tryToApply(Class<? extends Annotation> extensionType,
                              ParameterContext parameterContext)

    {
        if (!extensionsToApply.containsKey(extensionType))
        {
            return false;
        }

        int index = parameterContext.getIndex();
        int count = parameterContext.getDeclaringExecutable().getParameterCount();
        extensionsToApply.put(extensionType, index == (count - 1));

        return parameterContext.isAnnotated(extensionType);
    }

    @Override
    public void refresh()
        throws BeansException,
        IllegalStateException
    {
        if (isActive())
        {
            return;
        }

        Boolean allApplied = extensionsToApply.values()
                                              .stream()
                                              .filter(applied -> applied)
                                              .findFirst()
                                              .orElse(false);
        if (!Boolean.TRUE.equals(allApplied))
        {
            return;
        }

        lock();
        super.refresh();
    }

    private void lock()
    {
        Set<Entry<String, ReentrantLock>> entrySet = idSync.entrySet();
        outer: for (Entry<String, ReentrantLock> entry : entrySet)
        {
            String resourceId = entry.getKey();
            if (!Arrays.stream(getBeanDefinitionNames()).anyMatch(n -> n.equals(resourceId)))
            {
                continue;
            }

            ReentrantLock lock = entry.getValue();
            for (int i = 0; i < 10; i++)
            {
                try
                {
                    if (lock.tryLock() || lock.tryLock(100, TimeUnit.MILLISECONDS))
                    {
                        logger.info(String.format("Test resource [%s] locked.", resourceId));
                        continue outer;
                    }
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    throw new ApplicationContextException(String.format("Failed to lock [%s].", resourceId), e);

                }
            }

            throw new ApplicationContextException(
                    String.format("Failed to lock [%s] after [%s] attempts. Consider to use unique resource ID for your test.",
                                  resourceId,
                                  REPOSITORY_LOCK_ATTEMPTS));

        }
    }

    private void unlock()
    {
        Comparator<Entry<String, ReentrantLock>> reversed = new Comparator<Entry<String, ReentrantLock>>()
        {

            @Override
            public int compare(Entry<String, ReentrantLock> o1,
                               Entry<String, ReentrantLock> o2)
            {
                return o2.getKey().compareTo(o1.getKey());
            }

        };
        Set<Entry<String, ReentrantLock>> reversedEntrySet = idSync.entrySet()
                                                                               .stream()
                                                                               .sorted(reversed)
                                                                               .collect(Collectors.toSet());
        String[] beanDefinitionNames = getBeanDefinitionNames();
        for (Entry<String, ReentrantLock> entry : reversedEntrySet)
        {
            String id = entry.getKey();
            if (!Arrays.stream(beanDefinitionNames).anyMatch(n -> n.equals(id)))
            {
                continue;
            }

            ReentrantLock lock = entry.getValue();
            lock.unlock();

            logger.info(String.format("Test resource [%s] unlocked.", id));
        }
    }

    @Override
    public void close()
    {
        Collection<TestRepositoryContext> testRepositoryContexts = getTestRepositoryContexts();
        try
        {
            super.close();
            repositoriesShouldBeClosed(testRepositoryContexts);
        }
        catch (IOException e)
        {
            throw new ApplicationContextException("Failed to close context.", e);
        } 
        finally
        {
            unlock();
        }
    }

    private void repositoriesShouldBeClosed(Collection<TestRepositoryContext> testRepositoryContexts)
        throws IOException
    {
        try
        {
            // Error if we have some not properly closed Repository Contexts
            throw testRepositoryContexts.stream()
                                        .filter(r -> r.isOpened())
                                        .map(r -> r.getTestRepository().repository())
                                        .reduce((s1,
                                                 s2) -> String.format("%s, %s", s1, s2))
                                        .map(m -> new IOException(
                                                String.format("Failed to close following repositories: [%s]",
                                                              m)))
                                        .get();
        }
        catch (NoSuchElementException e)
        {
            // Everything is ok, there is no opened Repository Contexts.
            return;
        }
    }

    @Override
    public Collection<TestRepositoryContext> getTestRepositoryContexts()
    {
        return getBeansOfType(TestRepositoryContext.class).values();
    }

    @Override
    public TestRepositoryContext getTestRepositoryContext(String id)
    {
        return (TestRepositoryContext) getBean(id);
    }

    @Override
    public TestArtifactContext getTestArtifactContext(String id)
    {
        return (TestArtifactContext) getBean(id);
    }

    @Override
    public void register(TestRepository testRepository)
    {
        idSync.putIfAbsent(id(testRepository), new ReentrantLock());
        registerBean(id(testRepository), TestRepositoryContext.class, testRepository);
    }

    @Override
    public void register(TestArtifact testArtifact,
                         TestInfo testInfo)
    {
        idSync.putIfAbsent(id(testArtifact), new ReentrantLock());
        registerBean(id(testArtifact), TestArtifactContext.class, testArtifact, testInfo);
    }

}
