package org.carlspring.strongbox.controllers.support;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Jan Bucko
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaxUploadSizeEntityBody
{

    @JsonProperty("maxUploadSize")
    private String maxUploadSize;

    @JsonCreator
    public MaxUploadSizeEntityBody(@JsonProperty("maxUploadSize") String maxUploadSize)
    {
        this.maxUploadSize = maxUploadSize;
    }

    public String getMaxUploadSize()
    {
        return maxUploadSize;
    }

    public void setMaxUploadSize(String maxUploadSize)
    {
        this.maxUploadSize = maxUploadSize;
    }
}