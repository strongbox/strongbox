package org.carlspring.strongbox.mapper;

import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.MutableNpmRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.MutableNugetRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.MutableRawRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.remote.MutableMavenRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.remote.MutableNpmRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.remote.MutableNugetRemoteRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.remote.MutableRawRemoteRepositoryConfiguration;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.stereotype.Component;

/**
 * @author Pablo Tirado
 */
@Component
public class CustomYamlMapper
        extends YAMLMapper
{

    @PostConstruct
    public void postConstruct()
    {
        enable(SerializationFeature.WRAP_ROOT_VALUE);
        enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
        setAnnotationIntrospector(new JacksonAnnotationIntrospector());
        setSerializationInclusion(JsonInclude.Include.NON_NULL);

        registerSubtypes(new NamedType(MutableMavenRepositoryConfiguration.class, "mavenRepositoryConfiguration"));
        registerSubtypes(new NamedType(MutableNpmRepositoryConfiguration.class, "npmRepositoryConfiguration"));
        registerSubtypes(new NamedType(MutableNugetRepositoryConfiguration.class, "nugetRepositoryConfiguration"));
        registerSubtypes(new NamedType(MutableRawRepositoryConfiguration.class, "rawRepositoryConfiguration"));

        registerSubtypes(
                new NamedType(MutableMavenRemoteRepositoryConfiguration.class, "mavenRemoteRepositoryConfiguration"));
        registerSubtypes(
                new NamedType(MutableNpmRemoteRepositoryConfiguration.class, "npmRemoteRepositoryConfiguration"));
        registerSubtypes(
                new NamedType(MutableNugetRemoteRepositoryConfiguration.class, "nugetRemoteRepositoryConfiguration"));
        registerSubtypes(
                new NamedType(MutableRawRemoteRepositoryConfiguration.class, "rawRemoteRepositoryConfiguration"));
    }


}
