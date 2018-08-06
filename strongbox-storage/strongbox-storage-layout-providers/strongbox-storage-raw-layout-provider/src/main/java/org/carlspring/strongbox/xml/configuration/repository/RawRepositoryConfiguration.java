package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
@JsonTypeName(RawLayoutProvider.ALIAS)
public class RawRepositoryConfiguration
        extends CustomRepositoryConfiguration
{


    public RawRepositoryConfiguration(final MutableRawRepositoryConfiguration delegate)
    {
        // maybe one day I'll have some implementation here :)
    }

}
