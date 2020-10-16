package org.carlspring.strongbox.controllers.support;

import static org.carlspring.strongbox.db.schema.Properties.NAME;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Steve Todorov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleEntityBody
{

    @JsonProperty(NAME)
    private String name;

    @JsonCreator
    public ExampleEntityBody(@JsonProperty(NAME) String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
