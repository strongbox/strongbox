package org.carlspring.strongbox.forms.configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * @author Pablo Tirado
 */
public class ServerSettingsForm
{

    @NotBlank(message = "A base URL must be specified.")
    private String baseUrl;

    @Min(value = 1, message = "Port number must be an integer between 1 and 65535.")
    @Max(value = 65535, message = "Port number must be an integer between 1 and 65535.")
    private int port;

    public ServerSettingsForm()
    {
    }

    public ServerSettingsForm(@NotBlank(message = "A base URL must be specified.") String baseUrl,
                              @Min(value = 1, message = "Port number must be an integer between 1 and 65535.")
                              @Max(value = 65535, message = "Port number must be an integer between 1 and 65535.") int port)
    {
        this.baseUrl = baseUrl;
        this.port = port;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
