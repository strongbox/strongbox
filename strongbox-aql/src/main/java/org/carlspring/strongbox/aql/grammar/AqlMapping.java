package org.carlspring.strongbox.aql.grammar;

import static org.carlspring.strongbox.db.schema.Properties.STORAGE_ID;
import static org.carlspring.strongbox.db.schema.Properties.REPOSITORY_ID;
import static org.carlspring.strongbox.db.schema.Properties.LAST_UPDATED;

public enum AqlMapping
{

    STORAGE(STORAGE_ID),
    REPOSITORY(REPOSITORY_ID),
    LAYOUT("artifactCoordinates.@class"),
    VERSION("artifactCoordinates.version"),
    TAG("tagSet.name"),
    FROM(LAST_UPDATED),
    TO(LAST_UPDATED),
    AGE(LAST_UPDATED);

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
