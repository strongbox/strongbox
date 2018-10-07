package org.carlspring.strongbox.authentication.registry.support.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "authenticator-list-element")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthenticatorListElement
{

    private int index;

    private String name;


    public AuthenticatorListElement(int index,
                                    String name)
    {
        this.index = index;
        this.name = name;
    }

    AuthenticatorListElement()
    {
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
