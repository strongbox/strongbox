package org.carlspring.strongbox.controllers.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.NONE)
public class ErrorXmlHandler
{

    @XmlElement(name = "message")
    private String message;

    public ErrorXmlHandler()
    {
    }

    public ErrorXmlHandler(String message)
    {
        this.message = message;
    }
}