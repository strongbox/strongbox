Defines user management subsystem. 

If no configuration file is specified, the users are pre-loaded from internal configuration source `etc/conf/strongbox-security-users.xml`,

CRUD operations for users are defined in the (UserServiceImpl)[https://github.com/strongbox/strongbox/blob/master/strongbox-user-management/strongbox-user-management-api/src/main/java/org/carlspring/strongbox/users/service/impl/UserServiceImpl.java] class. We are using OrientDB as a persistence storage.
