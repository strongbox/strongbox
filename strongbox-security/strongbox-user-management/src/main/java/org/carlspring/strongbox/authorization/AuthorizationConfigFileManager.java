package org.carlspring.strongbox.authorization;

import com.google.common.annotations.VisibleForTesting;
import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.users.domain.SystemRole;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.YamlFileManager;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@Component
public class AuthorizationConfigFileManager
        extends YamlFileManager<AuthorizationConfigDto>
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;

    @Inject
    public AuthorizationConfigFileManager(YAMLMapperFactory yamlMapperFactory)
    {
        super(yamlMapperFactory);
    }

    @Override
    public String getPropertyKey()
    {
        return "strongbox.authorization.config.yaml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-authorization.yaml";
    }

    @Override
    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

    @Override
    public synchronized void store(AuthorizationConfigDto configuration) throws IOException
    {
        Set<SystemRole> changed = getChangedRestrictedRoles(configuration);
        if (changed.size() > 0)
        {
            String changedNames = changed.stream()
                    .map(SystemRole::toString)
                    .collect(Collectors.joining(", "));
            String message = String.format("System roles %s cannot be changed.", changedNames);
            throw new IOException(message);
        }
        super.store(configuration);
    }

    @VisibleForTesting
    public Set<SystemRole> getChangedRestrictedRoles(AuthorizationConfigDto configuration) throws IOException {
        Set<SystemRole> changed = new HashSet<>();
        Map<String, SystemRole> restricted = SystemRole.getRestricted();
        for (RoleDto role : configuration.getRoles())
        {
            if (!restricted.containsKey(role.getName()))
            {
                continue;
            }
            SystemRole systemRole = restricted.get(role.getName());
            // get MD5 hash for the role
            String hash = null;
            try {
                hash = MessageDigestUtils.calculateChecksum(role);
            } catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
            // compare hash with the hardcoded one in SystemRole
            if (!hash.equalsIgnoreCase(systemRole.getHash())) {
                changed.add(systemRole);
            }
            // remove SystemRole from the map
            restricted.remove(role.getName());
        }
        // deleted SystemRoles
        changed.addAll(restricted.values());

        return changed;
    }

}
