package org.carlspring.strongbox.testing.artifact;

import static org.carlspring.strongbox.testing.artifact.TestArtifactContext.id;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.carlspring.strongbox.io.ProxyPathInvocationHandler;
import org.carlspring.strongbox.testing.storage.repository.TestRepositoryManagementContext;
import org.carlspring.strongbox.testing.storage.repository.TestRepositoryManagementContextSupport;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * @author sbespalov
 *
 */
public class ArtifactManagementTestExecutionListener extends TestRepositoryManagementContextSupport<TestArtifact>
{

    public ArtifactManagementTestExecutionListener()
    {
        super(TestArtifact.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        Parameter parameter = parameterContext.getParameter();
        TestArtifact testArtifact = parameter.getAnnotation(TestArtifact.class);

        TestRepositoryManagementContext testApplicationContext = getTestRepositoryManagementContext();
        testApplicationContext.register(testArtifact, new TestInfo()
        {

            @Override
            public String getDisplayName()
            {
                return extensionContext.getDisplayName();
            }

            @Override
            public Set<String> getTags()
            {
                return extensionContext.getTags();
            }

            @Override
            public Optional<Class<?>> getTestClass()
            {
                return extensionContext.getTestClass();
            }

            @Override
            public Optional<Method> getTestMethod()
            {
                return extensionContext.getTestMethod();
            }

        });
        testApplicationContext.refresh();

        return Proxy.newProxyInstance(ArtifactManagementTestExecutionListener.class.getClassLoader(),
                                      new Class[] { Path.class },
                                      new TestArtifactProxyInvocationHandler(
                                              id(testArtifact)));
    }

    /**
     * This class provides lazy initialization for resolved artifact Path
     * instance.
     * 
     * @author sbespalov
     *
     */
    private class TestArtifactProxyInvocationHandler extends ProxyPathInvocationHandler
    {

        private Path target;
        private final String id;

        private TestArtifactProxyInvocationHandler(String id)
        {
            this.id = id;
        }

        public Path getTarget()
        {
            if (target == null)
            {
                target = getTestRepositoryManagementContext().getTestArtifactContext(id).getArtifact();
            }

            return target;
        }

    }
}
