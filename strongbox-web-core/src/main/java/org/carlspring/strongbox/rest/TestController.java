package org.carlspring.strongbox.rest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/test")
public class TestController
{

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Inject
    ObjectMapper objectMapper;

    @RequestMapping(value = "/{storageId}/{repositoryId}/**", method = RequestMethod.GET)
    public
    @ResponseBody
    List<String> test(@PathVariable(name = "storageId") String storageId,
                      @PathVariable(name = "repositoryId") String repositoryId,
                      HttpServletRequest request)
    {
        logger.debug("Request URI " + request.getRequestURI());

        String path = convertRequestToPath("/test", storageId, repositoryId, request);

        logger.debug("storageId " + storageId);
        logger.debug("repositoryId " + repositoryId);
        logger.debug("path " + path);

        List<String> result = new LinkedList<>();
        result.add(storageId);
        result.add(repositoryId);
        result.add(path);

        return result;
    }

    private String convertRequestToPath(String rootMapping,
                                        String storageId,
                                        String repositoryId,
                                        HttpServletRequest request)
    {
        int totalPrefixLength = rootMapping.length() + storageId.length() + repositoryId.length() + 3;
        return request.getRequestURI().substring(totalPrefixLength);
    }
}