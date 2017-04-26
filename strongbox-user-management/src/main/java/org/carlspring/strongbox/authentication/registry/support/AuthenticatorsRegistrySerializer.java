package org.carlspring.strongbox.authentication.registry.support;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticatorsRegistrySerializer
        extends StdSerializer<AuthenticatorsRegistry>
{

    public AuthenticatorsRegistrySerializer()
    {
        super(AuthenticatorsRegistry.class);
    }

    @Override
    public void serialize(AuthenticatorsRegistry value,
                          JsonGenerator gen,
                          SerializerProvider provider)
            throws IOException
    {
        final List<Authenticator> view = value.get();
        gen.writeStartArray(view.size());
        for (int index = 0; index < view.size(); index++)
        {
            gen.writeStartObject();
            final Authenticator authenticator = view.get(index);
            gen.writeNumberField("index", index);
            gen.writeStringField("name", authenticator.getName());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}
