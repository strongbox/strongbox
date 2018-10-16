package org.carlspring.strongbox.mapper;

import org.carlspring.strongbox.forms.configuration.MavenRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.NugetRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.RawRepositoryConfigurationForm;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.NpmRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.NugetRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.RawRepositoryConfiguration;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author Alex Oreshkevich
 */
@Component
@Primary
public class CustomObjectMapper
        extends ObjectMapper
{

    @PostConstruct
    public void postConstruct()
    {

        if (GenericParser.IS_OUTPUT_FORMATTED)
        {
            enable(SerializationFeature.INDENT_OUTPUT);
        }
        disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector introspector = AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
        setAnnotationIntrospector(introspector);

        registerSubtypes(new NamedType(MavenRepositoryConfigurationForm.class, Maven2LayoutProvider.ALIAS));
        registerSubtypes(new NamedType(NugetRepositoryConfigurationForm.class, NugetLayoutProvider.ALIAS));
        registerSubtypes(new NamedType(RawRepositoryConfigurationForm.class, RawLayoutProvider.ALIAS));

        registerSubtypes(new NamedType(MavenRepositoryConfiguration.class, Maven2LayoutProvider.ALIAS));
        registerSubtypes(new NamedType(NugetRepositoryConfiguration.class, NugetLayoutProvider.ALIAS));
        registerSubtypes(new NamedType(RawRepositoryConfiguration.class, RawLayoutProvider.ALIAS));
        registerSubtypes(new NamedType(NpmRepositoryConfiguration.class, NpmLayoutProvider.ALIAS));

    }


}
