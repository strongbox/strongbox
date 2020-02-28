import static org.reflections.ReflectionUtils.withAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.neo4j.ogm.annotation.NodeEntity;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

public class Main
{

    public static void main(String args[])
        throws Exception
    {
        Class<? extends Annotation> annotation = NodeEntity.class;
        Method annotationValue = ReflectionUtils.getMethods(annotation, m -> "value".equals(m.getName()))
                                                .iterator()
                                                .next();
        Reflections reflections = new Reflections("org.carlspring.strongbox.artifact.coordinates",
                "org.carlspring.strongbox.domain");
        Map<Class<? extends ArtifactCoordinates>, String> collect = reflections.getSubTypesOf(ArtifactCoordinates.class)
                                                                               .stream()
                                                                               .filter(withAnnotation(annotation))
                                                                               .collect(Collectors.toMap(c -> c,
                                                                                                         c -> extracted(annotation,
                                                                                                                        annotationValue,
                                                                                                                        c)));
        System.out.println(collect);
    }

    private static String extracted(Class<? extends Annotation> annotation,
                                    Method annotationValue,
                                    Class<?> c)
    {
        try
        {
            return (String) annotationValue.invoke(c.getAnnotation(annotation));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
