package org.carlspring.strongbox.xml.repository;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class CustomRepositoryConfiguration
        implements RepositoryConfiguration
{

}
