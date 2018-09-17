package org.carlspring.strongbox.npm.metadata.test;

import static org.junit.Assert.assertEquals;

import org.carlspring.strongbox.npm.metadata.PackageFeed;
import org.carlspring.strongbox.npm.metadata.PackageVersion;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NpmPackageJsonTestCase
{

    @Test
    public void testParseTypesNode()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        PackageVersion packageDef = mapper.readValue(getClass().getResourceAsStream("/json/types/node/package.json"),
                                                     PackageVersion.class);

        assertEquals("definitely-typed", packageDef.getName());
    }

    @Test
    public void testParseAutosuggestFeed()
        throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        PackageFeed feedDef = mapper.readValue(getClass().getResourceAsStream("/json/antlr4-autosuggest/feed.json"),
                                               PackageFeed.class);

        assertEquals("antlr4-autosuggest", feedDef.getName());
    }
}
