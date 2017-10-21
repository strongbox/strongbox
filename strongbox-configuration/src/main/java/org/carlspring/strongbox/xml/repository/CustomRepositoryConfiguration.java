package org.carlspring.strongbox.xml.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class CustomRepositoryConfiguration
        implements RepositoryConfiguration
{

}
