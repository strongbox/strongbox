
This module contains the following resources/facilities which are useful for testing:

- An etc/configuration.xml.

- A log4j.properties with debugging on for org.carlspring.

- A META-INF/spring/strongbox-storage-context.xml.

- A facility class that creates the directory structure for a simple server and copies the required
  resources from the classpath in order to provide a minimalistic environment. For more details, check:

      org.carlspring.strongbox.storage.StorageManager.createServerLayout(String basedir)

You should add this project as a test scoped dependency to any Strongbox module which needs to have
a proper directory skeleton with the required resources. This would avoid you having to duplicate resources,
while also simplifying your tests' readability.

