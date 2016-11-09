package org.carlspring.strongbox.utils;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Alex Oreshkevich
 */
@Component
public class CustomUrlPathHelper extends UrlPathHelper
{

    private static final Logger logger = LoggerFactory.getLogger(CustomUrlPathHelper.class);

    @Override
    public String getLookupPathForRequest(HttpServletRequest request)
    {
        logger.warn("#### \n getLookupPathForRequest " + request.getPathInfo());
        return super.getLookupPathForRequest(request);
    }
}
