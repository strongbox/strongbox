# strongbox-ldap-authentication-provider

This module provides the functionality to authenticate users using an LDAP server as the data source.

## Manual testing with OpenLDAP

Sometimes you might need to do manual testing with an OpenLDAP server. In the `project root` path we already have
`docker-compose.yml` which has everything you need to proceed. The only thing you need is an installed Docker 
([guide here](https://docs.docker.com/get-docker/)). 

Terminal 1:

1. `cd project root`
2. `docker-compose up openldap` (if you've made changes to the ldif files you might need to 
`docker-compose up --force-recreate openldap` instead)

Terminal 2:

1. `cd project root`
2. `mvn clean install -DskipTests`
3. `mvn -pl strongbox-web-core spring-boot:run`

Browser:

1. Open `http://localhost:48080/` 
2. Go over the security settings
3. Testing using curl should return `Status 200` 
   ```
   curl -I -u an-existing-ldap-user http://localhost:48080/api/account
   ```

