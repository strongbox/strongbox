package org.carlspring.strongbox.actuator;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

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
        strongboxInfo.put("version", propertiesBooter.getStrongboxVersion());
        strongboxInfo.put("revision", propertiesBooter.getStrongboxRevision());

        builder.withDetail("strongbox", strongboxInfo);
    }
}
