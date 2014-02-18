package org.carlspring.strongbox.url;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mtodorov
 */
public class ClasspathURLStreamHandlerFactory
        implements URLStreamHandlerFactory
{

    private final Map<String, URLStreamHandler> protocolHandlers = new HashMap<String, URLStreamHandler>();


    public ClasspathURLStreamHandlerFactory(String protocol, URLStreamHandler urlHandler)
    {
        addHandler(protocol, urlHandler);
    }

    public final void addHandler(String protocol, URLStreamHandler urlHandler)
    {
        protocolHandlers.put(protocol, urlHandler);
    }

    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        return protocolHandlers.get(protocol);
    }

}
