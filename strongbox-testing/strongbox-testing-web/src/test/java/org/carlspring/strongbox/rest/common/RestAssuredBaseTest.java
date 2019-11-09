package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.web.context.WebApplicationContext;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * General settings for the testing sub-system.
 *
 * @author Alex Oreshkevich
 */
public abstract class RestAssuredBaseTest
{
    protected static final String UNAUTHORIZED_MESSAGE_CODE = "ExceptionTranslationFilter.insufficientAuthentication";

    public final static int DEFAULT_PORT = 48080;

    public final static String DEFAULT_HOST = "localhost";

    protected static final String STORAGE0 = "storage0";
    
    /**
     * Share logger instance across all tests.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    @Inject
    protected WebApplicationContext context;

    @Inject
    protected ObjectMapper objectMapper;

    @Inject
    protected RestAssuredArtifactClient client;

    @Value("${strongbox.url}")
    private String contextBaseUrl;

    @Inject
    protected MockMvcRequestSpecification mockMvc;

    public void init()
            throws Exception
    {
        logger.debug("Initializing RestAssured...");

        client.setContextBaseUrl(contextBaseUrl);
    }

    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    protected MockMvcRequestSpecification givenCustom()
    {
        return mockMvc;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
    }
    
    public static void removeDir(String path)
    {
        removeDir(new File(path));
    }

    /**
     * Recursively removes directory or file #file and all it's content.
     *
     * @param file directory or file to be removed
     */
    public static void removeDir(File file)
    {
        if (file == null || !file.exists())
        {
            return;
        }

        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File f : files)
                {
                    removeDir(f);
                }
            }
        }

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    protected String getI18nInsufficientAuthenticationErrorMessage()
    {
        String defaultErrorMessage = messages.getMessage(UNAUTHORIZED_MESSAGE_CODE,
                                                         Locale.ENGLISH);

        String errorMessage = messages.getMessage(UNAUTHORIZED_MESSAGE_CODE,
                                                  defaultErrorMessage);

        return new String(errorMessage.getBytes(ISO_8859_1),
                          Charset.defaultCharset());
    }
}
