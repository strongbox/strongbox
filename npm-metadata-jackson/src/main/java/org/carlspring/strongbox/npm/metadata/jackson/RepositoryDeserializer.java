package org.carlspring.strongbox.npm.metadata.jackson;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.carlspring.strongbox.npm.metadata.Repository;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class RepositoryDeserializer extends JsonDeserializer<Repository>
{

    private static final Set<String> vcsProtocolSet = Collections.unmodifiableSet(new HashSet<String>()
    {
        {
            add("git");
            add("github");
            add("gist");
            add("bitbucket");
            add("gitlab");
        }
    });

    @Override
    public Repository deserialize(JsonParser jp,
                                  DeserializationContext c)
        throws IOException,
        JsonProcessingException
    {

        ObjectCodec codec = jp.getCodec();
        JsonNode node = codec.readTree(jp);

        String repositoryValue = node.asText();

        if (repositoryValue.startsWith("{"))
        {
            return codec.treeToValue(node, Repository.class);
        }

        String vcsProtocol;
        if ((vcsProtocol = vcsProtocolSet.stream()
                                         .filter(p -> repositoryValue.startsWith(String.format("%s:", p)))
                                         .findFirst()
                                         .orElse(null)) != null)
        {
            Repository repository = new Repository();
            repository.setType(vcsProtocol);
            repository.setUrl(URI.create(repositoryValue));

            return repository;
        }

        Repository repository = new Repository();
        repository.setType("npm");
        repository.setUrl(URI.create(repositoryValue));

        return repository;
    }

}
