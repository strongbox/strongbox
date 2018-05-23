package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.xml.CustomTag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public abstract class MutableCustomConfiguration
        implements CustomTag
{

    public abstract CustomConfiguration getImmutable();
}
