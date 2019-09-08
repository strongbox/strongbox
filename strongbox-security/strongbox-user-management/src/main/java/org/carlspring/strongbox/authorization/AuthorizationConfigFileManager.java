package org.carlspring.strongbox.authorization;

import org.carlspring.strongbox.authorization.dto.AuthorizationConfigDto;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.YamlFileManager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.carlspring.commons.encryption.EncryptionAlgorithmsEnum;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.authorization.dto.RoleDto;
import org.carlspring.strongbox.users.domain.SystemRole;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

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
        Map<SystemRole, String> changed = getChangedRestrictedRoles(configuration);
        if (changed.size() > 0)
        {
            String message = String.join("; ", changed.values());
            throw new RuntimeException(message);
        }
        super.store(configuration);
    }

    public Map<SystemRole, String> getChangedRestrictedRoles(AuthorizationConfigDto configuration) throws IOException {
        Map<SystemRole, String> changed = new HashMap<>();
        Map<String, SystemRole> restricted = SystemRole.getRestricted();
        for (RoleDto role : configuration.getRoles())
        {
            if (restricted.containsKey(role.getName()))
            {
                SystemRole systemRole = restricted.get(role.getName());
                // get MD5 hash for the role
                String hash = "";
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     ObjectOutputStream oos = new ObjectOutputStream(baos))
                {
                    oos.writeObject(role);
                    MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(baos);
                    mdos.write(baos.toByteArray());
                    hash = mdos.getMessageDigestAsHexadecimalString(EncryptionAlgorithmsEnum.MD5.getAlgorithm());
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw new IOException(e);
                }
                // compare hash with the hardcoded one in SystemRole
                if (!hash.equalsIgnoreCase(systemRole.getHash()))
                {
                    changed.put(systemRole, String.format("Restricted system role %s has been removed", systemRole));
                }
                // remove SystemRole from the map
                restricted.remove(role.getName());
            }
        }
        // deleted SystemRoles
        for (SystemRole systemRole : restricted.values())
        {
            changed.put(systemRole, String.format("Restricted system role%s %s has been removed"
                    , restricted.size() > 1 ? "s" : ""
                    , String.join(", ", restricted.keySet())));
        }

        return changed;
    }

}
