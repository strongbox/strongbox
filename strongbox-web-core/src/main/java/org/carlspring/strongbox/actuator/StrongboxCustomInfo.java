package org.carlspring.strongbox.actuator;

import static org.carlspring.strongbox.db.schema.Properties.VERSION;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * @author: adavid9
 */
@Component(value = "extendedInfoEndpoint")
public class StrongboxCustomInfo implements InfoContributor
{

    @Inject
    private PropertiesBooter propertiesBooter;

    @Override
    public void contribute(Info.Builder builder)
    {
        Map<String, String> strongboxInfo = new HashMap<>();
        strongboxInfo.put(VERSION, propertiesBooter.getStrongboxVersion());
        strongboxInfo.put("revision", propertiesBooter.getStrongboxRevision());

        builder.withDetail("strongbox", strongboxInfo);
    }
}
