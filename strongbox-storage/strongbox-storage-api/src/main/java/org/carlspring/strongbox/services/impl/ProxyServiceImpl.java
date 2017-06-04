package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.providers.repository.proxy.RemoteRepositoryRegistry;
import org.carlspring.strongbox.services.ProxyService;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author carlspring
 */
@Service
@Transactional
public class ProxyServiceImpl implements ProxyService
{

    @Inject
    RemoteRepositoryRegistry remoteRepositoryRegistry;


    @Override
    public String checkStatus(String url)
            throws MalformedURLException
    {
        return null;
    }

    @Override
    public String checkStatus(URL url)
            throws MalformedURLException
    {
        return null;
    }

    @Override
    public void checkAllRemoteRepositories()
    {

    }

}
