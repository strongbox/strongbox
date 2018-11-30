package org.carlspring.strongbox.authentication.support;

import java.io.IOException;

public interface AuthenticationItemConfigurationManager
{

    void updateAuthenticationItems(AuthenticationItems items)
        throws IOException;

    AuthenticationItems getAuthenticationItems();

    <T> T getCustomAuthenticationItem(CustomAuthenticationItemMapper<T> mapper);

    <T> void putCustomAuthenticationItem(T customAuthenticationItem,
                                         CustomAuthenticationItemMapper<T> mapper)
        throws IOException;

}