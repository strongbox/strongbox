package org.carlspring.strongbox.services;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author carlspring
 */
public interface ProxyService
{

    String checkStatus(String url) throws MalformedURLException;

    String checkStatus(URL url) throws MalformedURLException;

    void checkAllRemoteRepositories();

}
