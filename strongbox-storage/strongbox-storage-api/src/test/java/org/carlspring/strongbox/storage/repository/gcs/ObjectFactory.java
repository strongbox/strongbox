package org.carlspring.strongbox.storage.repository.gcs;

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

    private final static QName GOOGLE_CLOUD_CONFIGURATION_QNAME = new QName("google-cloud-configuration");
    
    public GoogleCloudConfiguration createGoogleCloudConfiguration() {
        return new GoogleCloudConfiguration();
    }

    @XmlElementDecl(name = "google-cloud-configuration", substitutionHeadName="configuration")
    public JAXBElement<GoogleCloudConfiguration> createGoogleCloudConfiguration(GoogleCloudConfiguration value) {
        return new JAXBElement<GoogleCloudConfiguration>(GOOGLE_CLOUD_CONFIGURATION_QNAME, GoogleCloudConfiguration.class, null, value);
    }
    
}