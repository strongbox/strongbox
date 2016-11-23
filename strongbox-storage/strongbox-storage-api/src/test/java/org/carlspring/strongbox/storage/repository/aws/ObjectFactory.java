package org.carlspring.strongbox.storage.repository.aws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author carlspring
 */
@XmlRegistry
public class ObjectFactory
{

    private final static QName AWS_CONFIGURATION_QNAME = new QName("aws-configuration");
    
    public AwsConfiguration createAwsConfiguration() {
        return new AwsConfiguration();
    }

    @XmlElementDecl(name = "aws-configuration", substitutionHeadName="configuration")
    public JAXBElement<AwsConfiguration> createAwsConfiguration(AwsConfiguration value) {
        return new JAXBElement<AwsConfiguration>(AWS_CONFIGURATION_QNAME, AwsConfiguration.class, null, value);
    }
}