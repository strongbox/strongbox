package org.carlspring.strongbox.testing.storage.repository;

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
 *
 */
public class TestRepositoryManagementApplicationContext extends AnnotationConfigApplicationContext
        implements TestRepositoryManagementContext
{

    private static final Logger logger = LoggerFactory.getLogger(TestRepositoryManagementApplicationContext.class);

    private static ThreadLocal<TestRepositoryManagementContext> testApplicaitonContextHolder = new ThreadLocal<>();

    private Map<Class<? extends Annotation>, Boolean> extensionsToApply = new HashMap<>();

    private static Map<String, ReentrantLock> testRepositorySync = new ConcurrentSkipListMap<>();

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

        if (!parameterContext.isAnnotated(extensionType))
        {
            return false;
        }

        return true;
    }

    @Override
    public void refresh()
        throws BeansException,
        IllegalStateException
    {
        Boolean allApplied = extensionsToApply.values()
                                              .stream()
                                              .filter(applied -> applied)
                                              .findFirst()
                                              .orElse(false);
        if (!Boolean.TRUE.equals(allApplied))
        {
            return;
        }

        lockRepositories();
        super.refresh();
    }

    private void lockRepositories()
    {
        Set<Entry<String, ReentrantLock>> entrySet = testRepositorySync.entrySet();
        for (Entry<String, ReentrantLock> entry : entrySet)
        {
            String testRepositoryId = entry.getKey();
            if (!Arrays.stream(getBeanDefinitionNames()).anyMatch(n -> n.equals(testRepositoryId)))
            {
                continue;
            }

            ReentrantLock lock = entry.getValue();
            for (int i = 0; i < 30; i++)
            {
                try
                {
                    lock.tryLock(100, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException e)
                {
                    break;
                }
                if (lock.isHeldByCurrentThread())
                {
                    break;
                }
            }

            if (!lock.isHeldByCurrentThread())
            {
                throw new ApplicationContextException(
                        String.format("Failed to lock [%s].", testRepositoryId));
            }
            logger.info(String.format("Test Repository [%s] locked.", testRepositoryId));
        }
    }

    private void unlockRepositories()
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
        Set<Entry<String, ReentrantLock>> reversedEntrySet = testRepositorySync.entrySet()
                                                                               .stream()
                                                                               .sorted(reversed)
                                                                               .collect(Collectors.toSet());
        for (Entry<String, ReentrantLock> entry : reversedEntrySet)
        {
            String testRepositoryId = entry.getKey();
            if (!Arrays.stream(getBeanDefinitionNames()).anyMatch(n -> n.equals(testRepositoryId)))
            {
                continue;
            }

            ReentrantLock lock = entry.getValue();
            lock.unlock();

            logger.info(String.format("Test Repository [%s] unlocked.", testRepositoryId));
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
            unlockRepositories();
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
    public void register(TestRepository testRepository)
    {
        testRepositorySync.putIfAbsent(id(testRepository), new ReentrantLock());

        registerBean(TestRepositoryContext.id(testRepository), TestRepositoryContext.class, testRepository);
    }

}
