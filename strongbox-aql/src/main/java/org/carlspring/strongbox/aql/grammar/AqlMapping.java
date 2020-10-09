package org.carlspring.strongbox.aql.grammar;

import org.carlspring.strongbox.db.schema.Properties;

public enum AqlMapping
{

    STORAGE(Properties.STORAGE_ID),
    REPOSITORY(Properties.REPOSITORY_ID),
    LAYOUT("artifactCoordinates.@class"),
    VERSION("artifactCoordinates.version"),
    TAG("tagSet.name"),
    FROM(Properties.LAST_UPDATED),
    TO(Properties.LAST_UPDATED),
    AGE(Properties.LAST_UPDATED);

    private String property;

    private AqlMapping(String property)
    {
        this.property = property;
    }

    public String property()
    {
        return property;
    }

}
