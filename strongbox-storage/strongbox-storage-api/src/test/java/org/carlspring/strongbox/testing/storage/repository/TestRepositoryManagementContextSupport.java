package org.carlspring.strongbox.testing.storage.repository;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @author sbespalov
 *
 * @param <T>
 */
public abstract class TestRepositoryManagementContextSupport<T extends Annotation>
        implements ParameterResolver, BeforeTestExecutionCallback, AfterTestExecutionCallback
{

    private final Class<T> type;

    public TestRepositoryManagementContextSupport(Class<T> type)
    {
        this.type = type;
    }

    protected TestRepositoryManagementContext getTestRepositoryManagementContext()
    {
        return TestRepositoryManagementApplicationContext.getInstance();
    }

    @Override
    public void beforeTestExecution(ExtensionContext context)
        throws Exception
    {
        TestRepositoryManagementApplicationContext.registerExtension(type, context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context)
        throws Exception
    {
        TestRepositoryManagementApplicationContext.closeExtension(type, context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        TestRepositoryManagementContext testRepositoryManagementContext = getTestRepositoryManagementContext();
        if (testRepositoryManagementContext == null)
        {
            return false;
        }

        return testRepositoryManagementContext.tryToApply(type, parameterContext);
    }

}
