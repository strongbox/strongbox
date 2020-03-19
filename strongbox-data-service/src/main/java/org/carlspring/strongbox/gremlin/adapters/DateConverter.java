package org.carlspring.strongbox.gremlin.adapters;

import java.time.LocalDateTime;
import java.util.Date;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author sbespalov
 */
public class DateConverter implements AttributeConverter<LocalDateTime, LocalDateTime>
{

    @Override
    public LocalDateTime toGraphProperty(LocalDateTime value)
    {
        return value;
    }

    @Override
    public LocalDateTime toEntityAttribute(LocalDateTime value)
    {
        return value;
    }
}