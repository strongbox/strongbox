package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.configuration.CorsConfiguration;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CorsConfigurationForm
{

    private List<String> allowedOrigins = new ArrayList<>();

    public CorsConfigurationForm()
    {
    }

    public CorsConfigurationForm(List<String> allowedOrigins)
    {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOrigins()
    {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins)
    {
        this.allowedOrigins = allowedOrigins;
    }

    public static CorsConfigurationForm fromConfiguration(CorsConfiguration source)
    {
        return new CorsConfigurationForm(source.getAllowedOrigins());
    }
}
