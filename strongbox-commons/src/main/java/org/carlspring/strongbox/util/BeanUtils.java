package org.carlspring.strongbox.util;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author Przemyslaw Fusik
 */
public final class BeanUtils
{

    private BeanUtils()
    {
        // avoid instantiation
    }

    public static <T> void populate(T bean,
                                    Map<String, ? extends Object> properties)
            throws IllegalAccessException
    {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            field.setAccessible(true);
            for (Map.Entry<String, ? extends Object> entry : properties.entrySet())
            {
                if (field.getName().equals(entry.getKey()))
                {
                    field.set(bean, entry.getValue());
                }
            }
        }

    }
}
