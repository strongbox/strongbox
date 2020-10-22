package org.carlspring.strongbox.npm.metadata.jackson;

import java.io.IOException;

import org.carlspring.strongbox.npm.metadata.Engines;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class EnginesDeserializer extends JsonDeserializer<Engines>
{

    @Override
    public Engines deserialize(JsonParser jp,
                               DeserializationContext c)
        throws IOException,
        JsonProcessingException
    {

        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);

        String value = node.asText();

        if (value.startsWith("{"))
        {
            return codec.treeToValue(node, Engines.class);
        }

        Engines engines = new Engines();
        // TODO: we should replace this {packageName} placeholer with package
        // name.
        engines.setAdditionalProperty(value, null);

        return engines;
    }

}
