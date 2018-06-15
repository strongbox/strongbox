package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SnippetGenerator {

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    public List<CodeSnippet> generateSnippets(String layout,
                                              ArtifactCoordinates coordinates) {
        Map<String, DependencySynonymFormatter> implementations = compatibleDependencyFormatRegistry.getProviderImplementations(layout);

        List<CodeSnippet> snippets = new ArrayList<>();
        for (String compatibleDependencyFormat : implementations.keySet()) {
            DependencySynonymFormatter formatter = implementations.get(compatibleDependencyFormat);

            CodeSnippet snippet = new CodeSnippet(compatibleDependencyFormat, formatter.getDependencySnippet(coordinates));
            snippets.add(snippet);
        }

        return snippets;
    }

}
