package org.carlspring.strongbox.providers.header;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class HeaderMappingRegistry
{

    private static final Logger logger = LoggerFactory.getLogger(HeaderMappingRegistry.class);

    private static final String USER_AGENT_FORMAT = "%s/*";

    private Map<String, String> userAgentMap = new LinkedHashMap<>();

    private Map<String, String> layoutMap = new LinkedHashMap<>();


    public HeaderMappingRegistry()
    {
    }

    @PostConstruct
    public void initialize()
    {
        logger.info("Initialized the header mapping registry.");
    }

    public void register(String layoutProviderAlias,
                         String userAgentPrefix)
    {
        userAgentMap.put(userAgentPrefix, String.format(USER_AGENT_FORMAT, userAgentPrefix));

        layoutMap.put(layoutProviderAlias, String.format(USER_AGENT_FORMAT, userAgentPrefix));
    }

    public Map<String, String> getUserAgentMap()
    {
        return userAgentMap;
    }

    public void setUserAgentMap(Map<String, String> userAgentMap)
    {
        this.userAgentMap = userAgentMap;
    }

    public Map<String, String> getLayoutMap()
    {
        return layoutMap;
    }

    public void setLayoutMap(Map<String, String> layoutMap)
    {
        this.layoutMap = layoutMap;
    }

}
