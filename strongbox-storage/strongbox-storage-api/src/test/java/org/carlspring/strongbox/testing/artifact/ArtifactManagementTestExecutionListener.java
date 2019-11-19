package org.carlspring.strongbox.testing.artifact;

import static org.carlspring.strongbox.testing.artifact.TestArtifactContext.id;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.carlspring.strongbox.io.ProxyPathInvocationHandler;
import org.carlspring.strongbox.testing.storage.repository.TestRepositoryManagementContext;
import org.carlspring.strongbox.testing.storage.repository.TestRepositoryManagementContextSupport;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.springframework.core.annotation.AnnotatedElementUtils;

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
        TestArtifact testArtifact = AnnotatedElementUtils.findMergedAnnotation(parameterContext.getParameter(), TestArtifact.class);
        
        Map<String, Object> attributesMap = Arrays.stream(parameterContext.getParameter().getAnnotations())
                                                  .map(a -> a.annotationType().getName())
                                                  .flatMap(a -> Stream.concat(Stream.of(a),
                                                                              AnnotatedElementUtils.getMetaAnnotationTypes(parameter,
                                                                                                                           a)
                                                                                                   .stream()))
                                                  .flatMap(a -> AnnotatedElementUtils.getMergedAnnotationAttributes(parameterContext.getParameter(),
                                                                                                                    a)
                                                                                     .entrySet()
                                                                                     .stream())
                                                  .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), (k1,
                                                                                                                 k2) -> k1));
        
        TestRepositoryManagementContext testApplicationContext = getTestRepositoryManagementContext();
        testApplicationContext.register(testArtifact, attributesMap, new TestInfo()
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
        testApplicationContext.tryToStart();

        if (List.class == parameter.getType())
        {
            return Proxy.newProxyInstance(ArtifactManagementTestExecutionListener.class.getClassLoader(),
                                          new Class[] { List.class },
                                          new ListInvocationHandler(id(testArtifact), testApplicationContext));
        }

        return Proxy.newProxyInstance(ArtifactManagementTestExecutionListener.class.getClassLoader(),
                                      new Class[] { Path.class },
                                      new TestArtifactProxyInvocationHandler(id(testArtifact), testApplicationContext));
    }
    
    private class ListInvocationHandler implements InvocationHandler
    {

        private List<Path> target;

        private final String id;

        private final TestRepositoryManagementContext context;

        private ListInvocationHandler(String id,
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

            try
            {
                return method.invoke(getTarget(), args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }

        public List<Path> getTarget()
        {
            if (target == null)
            {
                target = context.getTestArtifactContext(id).getArtifacts();
            }

            return target;
        }

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

        private final TestRepositoryManagementContext context;

        private TestArtifactProxyInvocationHandler(String id,
                                                   TestRepositoryManagementContext context)
        {
            this.id = id;
            this.context = context;
        }

        public Path getTarget()
        {
            if (target == null)
            {
                target = context.getTestArtifactContext(id).getArtifacts().stream().findFirst().get();
            }

            return target;
        }

    }

}
