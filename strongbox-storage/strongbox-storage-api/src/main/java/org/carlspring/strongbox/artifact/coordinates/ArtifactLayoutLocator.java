package org.carlspring.strongbox.artifact.coordinates;

import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * With this class you can get all avaliable Layouts from classpath.
 * 
 * 
 * @author sbespalov
 * 
 * @see ArtifactCoordinatesLayout 
 * @see ArtifactLayoutCoordinate
 * @see ArtifactCoordinates
 * 
 */
public class ArtifactLayoutLocator
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLayoutLocator.class);

    private volatile static Map<String, ArtifactLayoutDescription> layoutEntityMap;

    public static Map<String, ArtifactLayoutDescription> getLayoutEntityMap()
    {
        if (layoutEntityMap != null)
        {
            return layoutEntityMap;
        }
        return locateAvaliableLayouts();
    }

    private static synchronized Map<String, ArtifactLayoutDescription> locateAvaliableLayouts()
    {
        if (layoutEntityMap != null)
        {
            return layoutEntityMap;
        }

        Map<String, ArtifactLayoutDescription> layoutEntityMapLocal = new HashMap<>();

        Reflections reflections = new Reflections("org.carlspring.strongbox.artifact.coordinates");
        for (Class<? extends ArtifactCoordinates> artifactCoordinatesClass : reflections.getSubTypesOf(ArtifactCoordinates.class))
        {
            ArtifactCoordinatesLayout artifactLayout = artifactCoordinatesClass.getAnnotation(ArtifactCoordinatesLayout.class);
            if (artifactLayout == null)
            {
                logger.warn("[{}] should be provided for [{}] class",
                            ArtifactCoordinatesLayout.class.getSimpleName(),
                            artifactCoordinatesClass.getSimpleName());
                continue;
            }

            ArtifactLayoutDescription layoutDesc = ArtifactLayoutDescription.parse(artifactCoordinatesClass);
            layoutEntityMapLocal.put(layoutDesc.getLayoutAlias(), layoutDesc);
        }

        return layoutEntityMap = layoutEntityMapLocal;
    }

}
