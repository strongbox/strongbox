package org.carlspring.strongbox.storage.repository.gcs;

import org.carlspring.strongbox.storage.repository.CustomConfiguration;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author carlspring
 */
@XmlRegistry
public class GoogleCloudObjectFactory
{

    @XmlElementDecl(name = "google-cloud-configuration", scope = GoogleCloudConfiguration.class)
    JAXBElement<GoogleCloudConfiguration> createGoogleConfiguration(GoogleCloudConfiguration configuration)
    {

        return new JAXBElement<GoogleCloudConfiguration>(new QName("", "google-cloud-configuration"), GoogleCloudConfiguration.class,
                                                         configuration);
    }

    public static GoogleCloudConfiguration createGoogleCloudConfiguration()
    {
        return new GoogleCloudConfiguration();
    }

/*
    @XmlElementDecl(name = "configuration", scope = CustomConfiguration.class)
    JAXBElement<CustomConfiguration> createGoogleCloudeConfiguration(CustomConfiguration configuration)
    {
        return new JAXBElement<CustomConfiguration>(new QName("", "google-cloud-configuration"), CustomConfiguration.class, configuration);
    }
*/

}