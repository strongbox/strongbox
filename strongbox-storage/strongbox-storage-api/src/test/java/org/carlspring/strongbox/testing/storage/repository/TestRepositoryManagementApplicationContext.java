package org.carlspring.strongbox.testing.storage.repository;

import org.carlspring.strongbox.configuration.ConfigurationUtils;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.artifact.TestArtifactContext;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;
import static org.carlspring.strongbox.testing.artifact.TestArtifactContext.id;
import static org.carlspring.strongbox.testing.storage.repository.TestRepositoryContext.id;

/**
 * @author sbespalov
 */
public class TestRepositoryManagementApplicationContext extends AnnotationConfigApplicationContext
        implements TestRepositoryManagementContext
{

    private static final long REPOSITORY_LOCK_TIMEOUT = 10000;

    private static final Logger logger = LoggerFactory.getLogger(TestRepositoryManagementApplicationContext.class);

    private static ThreadLocal<TestRepositoryManagementContext> testApplicationContextHolder = new ThreadLocal<>();

    private Map<Class<? extends Annotation>, Boolean[]> extensionsToApply = new HashMap<>();

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

            testApplicationContextHolder.set(testApplicationContext);
        }

        if (testApplicationContext.extensionsToApply.containsKey(extensionType))
        {
            throw new IllegalStateException(String.format("Extension [%s] already registered.", extensionType));
        }

        testApplicationContext.extensionsToApply.put(extensionType,
                                                     context.getTestMethod()
                                                            .map(m -> new Boolean[m.getParameterCount()])
                                                            .get());
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

        testApplicationContextHolder.remove();
        if (testApplicationContext.isActive())
        {
            testApplicationContext.close();
        }
    }

    public static TestRepositoryManagementContext getInstance()
    {
        return testApplicationContextHolder.get();
    }

    @Override
    public boolean tryToApply(Class<? extends Annotation> extensionType,
                              ParameterContext parameterContext)

    {
        if (!extensionsToApply.containsKey(extensionType))
        {
            throw new IllegalArgumentException(String.format("Unsupported extension type [%s]. ", extensionType.getSimpleName()));
        }

        boolean supports = AnnotatedElementUtils.isAnnotated(parameterContext.getParameter(), extensionType);
        
        int index = parameterContext.getIndex();
        int count = parameterContext.getDeclaringExecutable().getParameterCount();
        extensionsToApply.get(extensionType)[index] = supports;
        
        boolean isLastTestMethodParameter = index == (count - 1);
        if (isLastTestMethodParameter && !checkIfAnyExtensionCheckedAndSupportsTestMethodParameter(index))
        {
            tryToStart();
        }
        
        return supports;
    }

    protected boolean checkIfAnyExtensionCheckedAndSupportsTestMethodParameter(int index)
    {
        return extensionsToApply.values()
                                .stream()
                                .map(parametersSupportedByExtension -> parametersSupportedByExtension[index])
                                .anyMatch(Boolean.TRUE::equals);
    }

    @Override
    public boolean tryToStart()
            throws BeansException,
                   IllegalStateException
    {
        if (isActive())
        {
            throw new IllegalStateException("Context already active.");
        }

        if (!shouldBeRefreshed())
        {
            return false;
        }

        lock();
        try
        {
            refresh();
        }
        catch (Throwable e)
        {
            unlockWithExceptionPropagation(e);
        }
        
        return true;
    }

    protected boolean shouldBeRefreshed()
    {
        return extensionsToApply.values()
                                .stream()
                                .flatMap(parametersSupportedByExtension -> Arrays.stream(parametersSupportedByExtension))
                                .allMatch(Objects::nonNull);
    }

    private void unlockWithExceptionPropagation(Throwable e)
            throws BeansException,
                   IllegalStateException
    {
        try
        {
            unlock();
        }
        catch (Throwable e1)
        {
            e1.addSuppressed(e);
            throw new ApplicationContextException("Failed to unlock test resources.", e1);
        }
        if (e instanceof BeansException)
        {
            throw (BeansException) e;
        }
        else if (e instanceof IllegalStateException)
        {
            throw (IllegalStateException) e;
        }
        throw new UndeclaredThrowableException(e);
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
            long now = System.currentTimeMillis();
            while (System.currentTimeMillis() - now < REPOSITORY_LOCK_TIMEOUT)
            {
                try
                {
                    if (lock.tryLock() || lock.tryLock(100, TimeUnit.MILLISECONDS))
                    {
                        logger.debug("Test resource [{}] locked.", resourceId);
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
                    String.format("Failed to lock [%s] after [%s] seconds. Consider to use unique resource ID for your test.",
                                  resourceId,
                                  REPOSITORY_LOCK_TIMEOUT/1000));

        }
    }

    private void unlock()
    {
        Comparator<Entry<String, ReentrantLock>> reversed = (o1, o2) -> o2.getKey().compareTo(o1.getKey());
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

            logger.debug("Test resource [{}] unlocked.", id);
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
                                        .filter(TestRepositoryContext::isOpened)
                                        .map(r -> r.getTestRepository().repositoryId())
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
    public void register(TestRepository testRepository,
                         Remote remoteRepository,
                         Group groupRepository,
                         RepositoryAttributes repositoryAttributes)
    {
        idSync.putIfAbsent(id(testRepository), new ReentrantLock());
        registerBean(id(testRepository), TestRepositoryContext.class, testRepository, remoteRepository, groupRepository, repositoryAttributes);
        
        if (groupRepository == null)
        {
            return;
        }
        
        Arrays.stream(groupRepository.repositories()).forEach(r -> {
            BeanDefinition beanDefinition = getBeanDefinition(id(ConfigurationUtils.getStorageId(testRepository.storageId(), r), ConfigurationUtils.getRepositoryId(r)));
            beanDefinition.setDependsOn(id(testRepository));
        });
    }

    @Override
    public void register(TestArtifact testArtifact,
                         Map<String, Object> attributesMap,
                         TestInfo testInfo)
    {
        idSync.putIfAbsent(id(testArtifact), new ReentrantLock());
        registerBean(id(testArtifact), TestArtifactContext.class, testArtifact, attributesMap, testInfo);
        if (testArtifact.repositoryId().isEmpty())
        {
            return;
        }
        
        BeanDefinition beanDefinition = getBeanDefinition(id(testArtifact));
        beanDefinition.setDependsOn(id(testArtifact.storageId(), testArtifact.repositoryId()));
    }

}
