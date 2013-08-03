package org.carlspring.strongbox.annotations;

import org.apache.maven.artifact.Artifact;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
public class ArtifactResourceMapper
{

    // Key:     groupId:artifactId
    // Sub-key: version
    // Entry:   resource information

    private static final Map<String, Map<String, ArtifactResource>> artifactResources = new LinkedHashMap<String, Map<String, ArtifactResource>>();


    public static void addResource(ArtifactResource resource)
    {
        final String ga = resource.groupId() + ":" + resource.artifactId();
        final Map<String, ArtifactResource> entryMap = artifactResources.get(ga);

        if (entryMap == null)
        {
            Map<String, ArtifactResource> resourceMap = new LinkedHashMap<String, ArtifactResource>();
            resourceMap.put(resource.version(), resource);

            artifactResources.put(ga, resourceMap);
        }
        else
        {
            if (!entryMap.containsKey(resource.version()))
            {
                entryMap.put(resource.version(), resource);
            }
        }
    }

    public static ArtifactResource getResource(String groupId, String artifactId, String version)
    {
        final String ga = groupId + ":" + artifactId;
        if (artifactResources.containsKey(ga))
        {
            return artifactResources.get(ga).get(version);
        }
        else
        {
            return null;
        }
    }

    public static void removeResources(String groupId, String artifactId, String version)
    {
        final String ga = groupId + ":" + artifactId;
        if (artifactResources.containsKey(ga))
        {
            artifactResources.get(ga).remove(version);
        }
    }

    public static ArtifactResource getArtifactResourceInstance(String repository,
                                                               Artifact artifact,
                                                               long length,
                                                               ArtifactExistenceState state)
    {
        return getArtifactResourceInstance(repository,
                                           artifact.getGroupId(),
                                           artifact.getArtifactId(),
                                           artifact.getVersion(),
                                           artifact.getType(),
                                           artifact.getClassifier(),
                                           length,
                                           state);
    }

    public static ArtifactResource getArtifactResourceInstance(final String repository,
                                                               final String groupId,
                                                               final String artifactId,
                                                               final String version,
                                                               final String type,
                                                               final String classifier,
                                                               final long length,
                                                               final ArtifactExistenceState state)
    {
        return new ArtifactResource() {

            @Override
            public String repository()
            {
                return repository;
            }

            @Override
            public String groupId()
            {
                return groupId;
            }

            @Override
            public String artifactId()
            {
                return artifactId;
            }

            @Override
            public String version()
            {
                return version;
            }

            @Override
            public String type()
            {
                return type;
            }

            @Override
            public String classifier()
            {
                return classifier;
            }

            @Override
            public long length()
            {
                return length;
            }

            @Override
            public ArtifactExistenceState state()
            {
                return state;
            }

            @Override
            public Class<? extends Annotation> annotationType()
            {
                return ArtifactResource.class;
            }
        };
    }

}
