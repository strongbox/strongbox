package org.carlspring.strongbox.authentication.registry.support.xml.out;

import org.carlspring.strongbox.authentication.api.Authenticator;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticatorsAdapter
        extends XmlAdapter<AuthenticatorList, List<Authenticator>>
{


    @Override
    public List<Authenticator> unmarshal(AuthenticatorList value)
            throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthenticatorList marshal(List<Authenticator> view)
            throws Exception
    {
        final AuthenticatorList authenticatorList = new AuthenticatorList();
        for (int index = 0; index < view.size(); index++)
        {
            authenticatorList.addElement(new AuthenticatorListElement(index, view.get(index).getName()));
        }
        return authenticatorList;

    }
}
