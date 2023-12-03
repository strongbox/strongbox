# What's this all about?

The path `./resources/ldap` contains the `strongbox-generic.ldif` which is used both in UnboundID and OpenLDAP via 
Docker Compose.

The file `strongbox-base.ldif` is mounted "as-is" into the container and is imported into OpenLDAP upon startup. 
Might be worth visiting [osxia/openldap](https://github.com/osixia/docker-openldap#osixiaopenldap).

More about how to test using OpenLDAP can be found in [strongbox-ldap-authentication-provider's readme](../../../../../../strongbox-security/strongbox-authentication-providers/strongbox-ldap-authentication-provider/README.md).
