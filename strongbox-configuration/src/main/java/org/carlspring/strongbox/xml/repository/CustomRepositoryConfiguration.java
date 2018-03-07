package org.carlspring.strongbox.xml.repository;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@Embeddable
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class CustomRepositoryConfiguration
        implements RepositoryConfiguration
{

}
