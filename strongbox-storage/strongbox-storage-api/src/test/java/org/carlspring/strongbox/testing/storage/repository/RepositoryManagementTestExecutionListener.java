package org.carlspring.strongbox.testing.storage.repository;

import static org.carlspring.strongbox.testing.storage.repository.TestRepositoryContext.id;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;

import org.carlspring.strongbox.storage.repository.Repository;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @author sbespalov
 *
 */
public class RepositoryManagementTestExecutionListener
        implements ParameterResolver, BeforeTestExecutionCallback, AfterTestExecutionCallback
{

    private TestRepositoryManagementContext getTestRepositoryManagementContext()
    {
        return TestRepositoryManagementApplicationContext.getInstance();
    }

    @Override
    public void beforeTestExecution(ExtensionContext context)
        throws Exception
    {
        TestRepositoryManagementApplicationContext.registerExtension(TestRepository.class, context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context)
        throws Exception
    {
        TestRepositoryManagementApplicationContext.closeExtension(TestRepository.class, context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        boolean applying = getTestRepositoryManagementContext().tryToApply(TestRepository.class,
                                                                                    parameterContext);
        if (!applying)
        {
            getTestRepositoryManagementContext().refresh();
        }

        return applying;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        Parameter parameter = parameterContext.getParameter();
        TestRepository testRepository = parameter.getAnnotation(TestRepository.class);

        TestRepositoryManagementContext testApplicationContext = getTestRepositoryManagementContext();
        testApplicationContext.register(testRepository);
        testApplicationContext.refresh();

        return Proxy.newProxyInstance(RepositoryManagementTestExecutionListener.class.getClassLoader(),
                                      new Class[] { Repository.class },
                                      new TestRepositoryProxyInvocationHandler(
                                              id(testRepository)));
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

        private TestRepositoryProxyInvocationHandler(String id)
        {
            this.id = id;
        }

        @Override
        public Object invoke(Object proxy,
                             Method method,
                             Object[] args)
            throws Throwable
        {
            if (target == null)
            {
                target = ((TestRepositoryContext) getTestRepositoryManagementContext().getTestRepositoryContext(id)).getRepository();
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
