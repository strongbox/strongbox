package org.carlspring.strongbox.domain;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.carlspring.strongbox.artifact.ArtifactTag;
import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * @author Sergey Bespalov
 *
 */
public class ArtifactTagEntry extends GenericEntity implements ArtifactTag, Serializable
{

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int hashCode()
    {
        if (name == null)
        {
            return 0;
        }
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ArtifactTagEntry))
        {
            return false;
        }
        return StringUtils.equals(name, ((ArtifactTagEntry) obj).name);
    }

}
