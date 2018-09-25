package org.carlspring.strongbox.xml.configuration.repository;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonTypeName;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
@JsonTypeName(NpmLayoutProvider.ALIAS)
public class NpmRepositoryConfiguration
        extends CustomRepositoryConfiguration
{

    NpmRepositoryConfiguration()
    {

    }

    public NpmRepositoryConfiguration(final MutableNpmRepositoryConfiguration delegate)
    {
    }

}
