package org.carlspring.strongbox.users.domain;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

/**
 * Security system atomic item that is used for access restriction.
 *
 * @author Alex Oreshkevich
 */
public enum Roles implements GrantedAuthority
{
    ROOT, // Administration: All
    ADMIN_CREATE_REPO,
    ADMIN_UPDATE_REPO,
    ADMIN_DELETE_REPO,
    ADMIN_LIST_REPO,
    CREATE_USER,
    UPDATE_USER,
    VIEW_USER,
    IMPERSONATE_USER,
    DELETE_USER,
    ARTIFACTS_DEPLOY,
    ARTIFACTS_DELETE,
    ARTIFACTS_VIEW,
    ARTIFACTS_READ,
    VIEW_OWN_TOKEN,
    VIEW_ANY_TOKEN,
    VIEW_LOGS,
    CONFIGURE_LOGS,
    RSS_FEED,
    UI_LOGIN,
    UI_BROWSE;

    @Override
    public String getAuthority()
    {
        return this.name();
    }

    /**
     * Helper method for accessing all admin roles.
     *
     * @return all roles related to full admin privileges
     */
    public static EnumSet<Roles> adminAll()
    {
        return EnumSet.allOf(Roles.class);
    }

    // TODO temporary method until security system tests will be ready
    public static String[] all()
    {
        List<String> roles = new LinkedList<>();
        for (int i = 0; i < Roles.values().length; i++)
        {
            roles.add(Roles.values()[i].name());
        }
        return roles.toArray(new String[roles.size()]);
    }

    public static EnumSet<Roles> repoAll()
    {
        return EnumSet.of(ADMIN_CREATE_REPO, ADMIN_DELETE_REPO, ADMIN_LIST_REPO, ADMIN_UPDATE_REPO);
    }

    public static EnumSet<Roles> artifactsAll()
    {
        return EnumSet.of(ARTIFACTS_DEPLOY, ARTIFACTS_DELETE, ARTIFACTS_VIEW, ARTIFACTS_READ);
    }

    public static EnumSet<Roles> usersAll()
    {
        return EnumSet.of(CREATE_USER, UPDATE_USER, VIEW_USER, IMPERSONATE_USER, DELETE_USER);
    }

    public static EnumSet<Roles> tokenAll()
    {
        return EnumSet.of(VIEW_OWN_TOKEN, VIEW_ANY_TOKEN);
    }

    public static EnumSet<Roles> logsAll()
    {
        return EnumSet.of(VIEW_LOGS, CONFIGURE_LOGS, RSS_FEED);
    }

    public static EnumSet<Roles> uiAll()
    {
        return EnumSet.of(UI_LOGIN, UI_BROWSE);
    }
}
