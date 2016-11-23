package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;

/**
 * @author carlspring
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="custom-configuration")
public abstract class CustomConfiguration
        implements Serializable
{

}
