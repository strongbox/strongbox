package org.carlspring.strongbox.aql.grammar;

import java.util.HashMap;
import java.util.Map;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.ArtifactLayout;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sbespalov
 *
 */
public class ArtifactLayoutLocator
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLayoutLocator.class);

    private volatile static Map<String, Class<? extends ArtifactCoordinates>> layoutEntityMap;

    public static Map<String, Class<? extends ArtifactCoordinates>> getLayoutEntityMap()
    {
        if (layoutEntityMap != null)
        {
            return layoutEntityMap;
        }
        return locateAvaliableLayouts();
    }

    private static synchronized Map<String, Class<? extends ArtifactCoordinates>> locateAvaliableLayouts()
    {
        if (layoutEntityMap != null)
        {
            return layoutEntityMap;
        }

        Map<String, Class<? extends ArtifactCoordinates>> layoutEntityMapLocal = new HashMap<>();

        Reflections reflections = new Reflections("org.carlspring.strongbox.artifact.coordinates");
        for (Class<? extends ArtifactCoordinates> artifactCoordinatesClass : reflections.getSubTypesOf(ArtifactCoordinates.class))
        {
            ArtifactLayout artifactLayout = artifactCoordinatesClass.getAnnotation(ArtifactLayout.class);
            if (artifactLayout == null)
            {
                logger.warn(String.format("[%s] should be provided for [%s] class",
                                          ArtifactLayout.class.getSimpleName(),
                                          artifactCoordinatesClass.getSimpleName()));
                continue;
            }
            layoutEntityMapLocal.put(artifactLayout.value(), artifactCoordinatesClass);
        }

        return layoutEntityMap = layoutEntityMapLocal;
    }

}
