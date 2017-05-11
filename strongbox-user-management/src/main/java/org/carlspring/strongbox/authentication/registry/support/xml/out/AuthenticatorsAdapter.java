package org.carlspring.strongbox.authentication.registry.support.xml.out;

import org.carlspring.strongbox.authentication.api.Authenticator;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Przemyslaw Fusik
 */
public class AuthenticatorsAdapter
        extends XmlAdapter<AuthenticatorList, Authenticator[]>
{
    @Override

    public Authenticator[] unmarshal(AuthenticatorList value)
            throws Exception
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthenticatorList marshal(Authenticator[] snapshot)
            throws Exception
    {
        final AuthenticatorList authenticatorList = new AuthenticatorList();
        for (int index = 0; index < snapshot.length; index++)
        {
            authenticatorList.addElement(new AuthenticatorListElement(index, snapshot[index].getName()));
        }
        return authenticatorList;

    }
}
