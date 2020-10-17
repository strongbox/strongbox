package org.carlspring.strongbox.npm.metadata.test;

import org.carlspring.strongbox.npm.metadata.PackageFeed;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.carlspring.strongbox.npm.metadata.jackson.NpmJacksonMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NpmPackageJsonTestCase
{

    @Test
    public void testParseTypesNode()
        throws Exception
    {
        ObjectMapper mapper = NpmJacksonMapperFactory.createObjectMapper();

        PackageVersion packageDef = mapper.readValue(getClass().getResourceAsStream("/json/types/node/package.json"), PackageVersion.class);

        assertThat(packageDef.getName()).isEqualTo("definitely-typed");
    }

    @Test
    public void testParseAutosuggestFeed()
        throws Exception
    {
        ObjectMapper mapper = NpmJacksonMapperFactory.createObjectMapper();

        PackageFeed feedDef = mapper.readValue(getClass().getResourceAsStream("/json/antlr4-autosuggest/feed.json"), PackageFeed.class);

        assertThat(feedDef.getName()).isEqualTo("antlr4-autosuggest");
    }
    
}
