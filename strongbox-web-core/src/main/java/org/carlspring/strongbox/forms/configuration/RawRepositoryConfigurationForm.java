package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Przemyslaw Fusik
 */
@JsonTypeName(RawLayoutProvider.ALIAS)
public class RawRepositoryConfigurationForm
        extends CustomRepositoryConfigurationForm
{

    @Override
    public <T> T accept(final CustomRepositoryConfigurationFormVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

}
