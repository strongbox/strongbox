
This is the Strongbox authentication support.

This module contains miscellaneous utilities, helpers and components widely used in the strongbox-security submodules.

Examples:
* AuthoritiesExternalToInternalMapper transforms external roles to internal strongbox roles
** if you want to deliver some non-standard external authentication provider you may want to add this module as a dependency to use existing AuthoritiesExternalToInternalMapper implementation
* you may also want to check your own non-standard external authentication provider via integration test
** use this module TestConfig configuration and BaseGenericXmlApplicationContextTest for that purpose
** see strongbox-ldap-authentication-provider tests for reference and configuration