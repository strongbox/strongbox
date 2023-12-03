package org.carlspring.strongbox.testing.storage.repository;

import static org.carlspring.strongbox.testing.storage.repository.TestRepositoryContext.id;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * @author sbespalov
 *
 */
public class RepositoryManagementTestExecutionListener extends TestRepositoryManagementContextSupport<TestRepository>
{

    public RepositoryManagementTestExecutionListener()
    {
        super(TestRepository.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        Parameter parameter = parameterContext.getParameter();
        TestRepository testRepository = AnnotatedElementUtils.findMergedAnnotation(parameter, TestRepository.class);
        Remote remoteRepository = AnnotatedElementUtils.findMergedAnnotation(parameter, Remote.class);
        Group groupRepository = AnnotatedElementUtils.findMergedAnnotation(parameter, Group.class);
        RepositoryAttributes repositoryAttributes = AnnotatedElementUtils.findMergedAnnotation(parameter, RepositoryAttributes.class);
        
        TestRepositoryManagementContext testApplicationContext = getTestRepositoryManagementContext();
        testApplicationContext.register(testRepository, remoteRepository, groupRepository, repositoryAttributes);
        testApplicationContext.tryToStart();

        return Proxy.newProxyInstance(RepositoryManagementTestExecutionListener.class.getClassLoader(),
                                      new Class[] { Repository.class },
                                      new TestRepositoryProxyInvocationHandler(
                                              id(testRepository), getTestRepositoryManagementContext()));
    }

    /**
     * This class provides lazy initialization for resolved Repository instance.
     * 
     * @author sbespalov
     *
     */
    private class TestRepositoryProxyInvocationHandler implements InvocationHandler
    {

        private Repository target;

        private final String id;

        private final TestRepositoryManagementContext context;

        private TestRepositoryProxyInvocationHandler(String id,
                                                     TestRepositoryManagementContext context)
        {
            this.id = id;
            this.context = context;
        }

        @Override
        public Object invoke(Object proxy,
                             Method method,
                             Object[] args)
            throws Throwable
        {
            if (target == null)
            {
                target = context.getTestRepositoryContext(id).getRepository();
            }

            try
            {
                return method.invoke(target, args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }

    }

}
