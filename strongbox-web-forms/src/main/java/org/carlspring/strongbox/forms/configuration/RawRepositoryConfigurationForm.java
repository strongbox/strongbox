package org.carlspring.strongbox.forms.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Przemyslaw Fusik
 */
@JsonTypeName("rawRepositoryConfiguration")
public class RawRepositoryConfigurationForm
        extends CustomRepositoryConfigurationForm
{

    @Override
    public <T> T accept(final CustomRepositoryConfigurationFormVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

}
