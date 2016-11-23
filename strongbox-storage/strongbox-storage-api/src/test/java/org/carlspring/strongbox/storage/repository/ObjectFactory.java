package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * @author carlspring
 */
@XmlRegistry
class ObjectFactory {

	private final static QName CONFIGURATION_QNAME = new QName("configuration");

	@XmlElementDecl(name = "configuration")
	public JAXBElement<CustomConfiguration> createCustomConfiguration(CustomConfiguration value) {
		return new JAXBElement<CustomConfiguration>(CONFIGURATION_QNAME, CustomConfiguration.class, null, value);
	}

}
