package org.carlspring.strongbox.gremlin.repositories;

import java.lang.annotation.Annotation;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * @author sbespalov
 *
 */
public enum EntityType
{
    VERTEX(NodeEntity.class), EDGE(RelationshipEntity.class);

    private final Class<? extends Annotation> entityTypeAnnotation;

    private EntityType(Class<? extends Annotation> entityTypeAnnotation)
    {
        this.entityTypeAnnotation = entityTypeAnnotation;
    }

    public Class<? extends Annotation> getEntityTypeAnnotation()
    {
        return entityTypeAnnotation;
    }

}
