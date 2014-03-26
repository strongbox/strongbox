package org.carlspring.strongbox.security.jaas.principal;

/**
 * <p> This class implements the <code>Principal</code> interface
 * and represents a user.
 * <p/>
 * <p> Principals such as this <code>UserPrincipal</code>
 * may be associated with a particular <code>Subject</code>
 * to augment that <code>Subject</code> with an additional
 * identity.  Refer to the <code>Subject</code> class for more information
 * on how to achieve this.  Authorization decisions can then be based upon
 * the Principals associated with a <code>Subject</code>.
 *
 * @see java.security.Principal
 * @see javax.security.auth.Subject
 */
public class UserPrincipal extends BasePrincipal
{

    public UserPrincipal()
    {
    }

    public UserPrincipal(String name)
    {
        super(name);
    }

}
