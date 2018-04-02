package org.carlspring.strongbox.nuget.file;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.text.MessageFormat;
import java.util.EnumSet;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssemblyTargetFrameworkAdapter
        extends XmlAdapter<String, EnumSet<Framework>>
{

    private static final Logger LOG = LoggerFactory.getLogger(AssemblyTargetFrameworkAdapter.class);
    private static final String FRAMEWORKS_DELIMETER = ", ";

    public AssemblyTargetFrameworkAdapter()
    {
    }

    public String marshal(EnumSet<Framework> frameworks)
            throws Exception
    {
        if (frameworks != null && !frameworks.isEmpty())
        {
            String result = Joiner.on(", ").join(frameworks);
            return result;
        }
        else
        {
            return null;
        }
    }

    public EnumSet<Framework> unmarshal(String farmeworks)
            throws Exception
    {
        if (Strings.isNullOrEmpty(farmeworks))
        {
            return null;
        }
        else
        {
            String[] names = farmeworks.split(", ");
            EnumSet<Framework> result = EnumSet.noneOf(Framework.class);
            String[] var4 = names;
            int var5 = names.length;

            for (int var6 = 0; var6 < var5; ++var6)
            {
                String name = var4[var6];

                try
                {
                    Framework framework = Framework.getByFullName(name);
                    if (framework != null)
                    {
                        result.add(framework);
                    }
                }
                catch (Exception var9)
                {
                    LOG.warn(MessageFormat.format("Csn not add framework: \"{0}\"", name), var9);
                }
            }

            return result.isEmpty() ? null : result;
        }
    }
}
