package org.carlspring.strongbox.authentication.api;

import java.util.Map;

public interface CustomAuthenticationItemMapper<T>
{

    String getConfigurationItemId();

    Map<String, Object> map(T source);
    
    T map(Map<String, Object> source);

}
