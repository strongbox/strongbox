package org.carlspring.strongbox.storage.repository.aws;

import org.carlspring.strongbox.storage.repository.CustomConfiguration;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author carlspring
 */
@XmlRegistry
public class AwsObjectFactory
{


    @XmlElementDecl(name = "aws-configuration", scope = AwsConfiguration.class)
    JAXBElement<AwsConfiguration> createAwsConfiguration(AwsConfiguration configuration)
    {
        return new JAXBElement<AwsConfiguration>(new QName("", "aws-configuration"),
                                                 AwsConfiguration.class,
                                                 configuration);
    }

    public static AwsConfiguration createAwsConfiguration()
    {
        return new AwsConfiguration();
    }

/*
    @XmlElementDecl(name = "configuration", scope = CustomConfiguration.class)
    JAXBElement<CustomConfiguration> createAwsConfiguration(CustomConfiguration configuration)
    {
        return new JAXBElement<CustomConfiguration>(new QName("", "aws-configuration"), CustomConfiguration.class, configuration);
    }
*/

}