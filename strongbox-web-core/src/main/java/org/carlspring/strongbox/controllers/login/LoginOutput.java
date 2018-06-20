package org.carlspring.strongbox.controllers.login;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "login-output")
@XmlAccessorType(XmlAccessType.NONE)
public class LoginOutput
{

    @XmlElement
    private String token;

    @XmlElement()
    private LinkedHashSet<String> authorities;

    public LoginOutput()
    {
    }

    public LoginOutput(String token,
                       Collection<? extends GrantedAuthority> authorities)
    {
        this.token = token;
        this.authorities = authorities.stream().map(GrantedAuthority::getAuthority).sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
