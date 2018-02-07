package org.carlspring.strongbox.log;

import com.google.common.base.CaseFormat;

public class LoggingUtils
{

    public static String caclucateCronContextName(Class<?> contextClass)
    {
        return String.format("strongbox-%s",
                             CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, contextClass.getSimpleName()));
    }

}
