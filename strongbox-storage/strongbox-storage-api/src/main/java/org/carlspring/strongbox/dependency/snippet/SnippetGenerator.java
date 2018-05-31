package org.carlspring.strongbox.dependency.snippet;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.springframework.stereotype.Component;

@Component
public class SnippetGenerator
{

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    public Map<String, String> generateSnippets(String layout,
                                                ArtifactCoordinates coordinates)
    {
        Map<String, DependencySynonymFormatter> implementations = compatibleDependencyFormatRegistry.getProviderImplementations(layout);

        Map<String, String> snippets = new LinkedHashMap<>();
        for (String compatibleDependencyFormat : implementations.keySet())
        {
            DependencySynonymFormatter formatter = implementations.get(compatibleDependencyFormat);

            snippets.put(compatibleDependencyFormat,
                         formatter.getDependencySnippet(coordinates));
        }

        return snippets;
    }

}
