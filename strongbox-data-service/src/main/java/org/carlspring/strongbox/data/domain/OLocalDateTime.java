package org.carlspring.strongbox.data.domain;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.serialization.OSerializableStream;

/**
 * @author Przemyslaw Fusik
 */
public class OLocalDateTime implements OSerializableStream
{

    private final LocalDateTime subject;

    OLocalDateTime()
    {
        this(null);
    }

    public OLocalDateTime(LocalDateTime subject)
    {
        this.subject = subject;
    }

    @Override
    public byte[] toStream()
            throws OSerializationException
    {
        return subject != null ? subject.toString().getBytes(StandardCharsets.UTF_8) : null;
    }

    @Override
    public OSerializableStream fromStream(byte[] iStream)
            throws OSerializationException
    {
        return iStream != null ? new OLocalDateTime(LocalDateTime.parse(new String(iStream, StandardCharsets.UTF_8))) :
               null;
    }

    public LocalDateTime getSubject()
    {
        return subject;
    }
}
