package org.carlspring.strongbox.util;

import java.util.function.Supplier;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Exception>
{

    T get() throws E;

    static <T> Supplier<T> unchecked(ThrowingSupplier<T, ?> supplier)
    {
        return () ->
        {
            try
            {
                return supplier.get();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        };
    }
}
