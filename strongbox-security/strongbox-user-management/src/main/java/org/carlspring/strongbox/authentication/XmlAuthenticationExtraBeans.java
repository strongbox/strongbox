package org.carlspring.strongbox.authentication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "extra-beans")
@XmlAccessorType(XmlAccessType.NONE)
public class XmlAuthenticationExtraBeans
{

    @XmlElement(name = "extra-bean")
    private List<XmlAuthenticationExtraBean> extraBeans;

    public List<XmlAuthenticationExtraBean> getExtraBeans()
    {
        return extraBeans;
    }

}
