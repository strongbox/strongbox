# strongbox-testing-core

This module contains the following resources/facilities which are useful for testing:

- A facility class which creates the directory structure for a simple server and copies the required
  resources from the classpath in order to provide a minimalistic environment. For more details, check:

      org.carlspring.strongbox.storage.StorageManager.createServerLayout(String basedir)

You should add this project as a test scoped dependency to any Strongbox module which needs to have
a proper directory skeleton with the required resources. This would avoid you having to duplicate resources,
while also simplifying your tests' readability.

## LdapServerTestConfig

The `LdapServerTestConfig` class is responsible for instantiating a customized embedded `UnboundID` server, which is 
used by integration tests. It needs to be registered like so:

```
@SpringBootTest
@ContextHierarchy({ @ContextConfiguration(classes = { LdapServerTestAutoConfig.class }) })
class MyTest {}
```

This will start an `UnboundID` server which is configured to look similar to OpenLDAP in order to unify all configurations
and make it easier to do manual testing with `OpenLDAP`. By default the `UnboundID` server will be loading the 
`strongbox-base.ldif` file from the `strongbox-testing-core` module. You can overwrite this by setting a property:

```
@SpringBootTest(properties = {"tests.unboundid.importLdifs=/ldap/strongbox-base.ldif,/ldap/strongbox-additional.ldif"})
```

If there are more properties which need to be configured, you would be better off creating a custom `.properties` file
and loading it via `@TestPropertySource(locations="classpath:test.properties")`.

For manual testing with OpenLDAP check [strongbox-ldap-authentication-provider/README.md](../../strongbox-security/strongbox-authentication-providers/strongbox-ldap-authentication-provider/README.md)
