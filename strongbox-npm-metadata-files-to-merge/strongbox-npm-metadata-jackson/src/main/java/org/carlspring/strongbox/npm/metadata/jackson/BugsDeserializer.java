package org.carlspring.strongbox.npm.metadata.jackson;

import java.io.IOException;

import org.carlspring.strongbox.npm.metadata.Bugs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class BugsDeserializer extends JsonDeserializer<Bugs>
{

    @Override
    public Bugs deserialize(JsonParser jp,
                            DeserializationContext c)
        throws IOException,
        JsonProcessingException
    {

        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);

        String value = node.asText();

        if (value.startsWith("{"))
        {
            return codec.treeToValue(node, Bugs.class);
        }

        Bugs bugs = new Bugs();
        bugs.setUrl(value);
            
        return bugs;
    }

}
