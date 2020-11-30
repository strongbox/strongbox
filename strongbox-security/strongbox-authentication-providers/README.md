# strongbox-authentication-providers

Strongbox authentication providers enumerates all supported built-in authentication providers.

NOTE: 

There is currently some conflicting terminology due to Spring's conventions:

* `Authentication Provider` in Spring is a mechanism which provides some sort of authentication credentials 
   (i.e. Basic Authentication).
* `Authentication Provider` in Strongbox is a `database` of some sort, which provides the users to authenticate against.

