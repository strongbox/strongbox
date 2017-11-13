package org.carlspring.strongbox.data.domain;

import java.time.Instant;
import java.time.LocalDateTime;

import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializer;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author Przemyslaw Fusik
 */
public enum MyOObjectSerializer
        implements OObjectSerializer<LocalDateTime, Long>
{

    INSTANCE;

    @Override
    public Long serializeFieldValue(Class<?> iClass,
                                    LocalDateTime iFieldValue)
    {
        return iFieldValue != null ?
               iFieldValue.atZone(LocaleContextHolder.getTimeZone().toZoneId())
                          .toInstant()
                          .toEpochMilli() : null;
    }

    @Override
    public LocalDateTime unserializeFieldValue(Class<?> iClass,
                                               Long iFieldValue)
    {
        return iFieldValue != null ?
               LocalDateTime.ofInstant(Instant.ofEpochMilli(iFieldValue),
                                       LocaleContextHolder.getTimeZone().toZoneId()) : null;
    }
}
