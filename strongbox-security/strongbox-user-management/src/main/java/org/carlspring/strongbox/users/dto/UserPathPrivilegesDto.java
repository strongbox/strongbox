package org.carlspring.strongbox.users.dto;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "path-privilege")
@XmlAccessorType(XmlAccessType.NONE)
public class UserPathPrivilegesDto
        implements Serializable
{

    @XmlAttribute(required = true)
    private String path;

    @XmlAttribute
    private boolean wildcard;

    @XmlElement(name = "privilege")
    @XmlElementWrapper(name = "privileges")
    private Set<PrivilegeDto> privileges = new LinkedHashSet<>();

    public String getPath()
    {
        return path;
    }

    public void setPath(final String path)
    {
        this.path = path;
    }

    public boolean isWildcard()
    {
        return wildcard;
    }

    public Set<PrivilegeDto> getPrivileges()
    {
        return privileges;
    }

    public void setWildcard(final boolean wildcard)
    {
        this.wildcard = wildcard;
    }
}
