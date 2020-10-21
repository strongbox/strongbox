package org.carlspring.strongbox.forms.configuration;

/**
 * @author Przemyslaw Fusik
 */
public interface CustomRepositoryConfigurationFormVisitor<T>
{

    T visit(MavenRepositoryConfigurationForm form);

    T visit(NugetRepositoryConfigurationForm form);

    T visit(RawRepositoryConfigurationForm form);
}
