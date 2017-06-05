# Strongbox
[![Master Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox/master)](https://dev.carlspring.org/jenkins/blue/organizations/jenkins/strongbox%2Fstrongbox/activity?branch=master)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/strongbox/strongbox?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Welcome to the Strongbox artifact repository manager's home.

# What's implemented:
* [Repositories](https://github.com/strongbox/strongbox/wiki/Repositories):
  * Hosted
  * Group
    * Support for repository ordering
    * Support for routing rules
    * Support for nested group repositories
* Layout providers:
  * Maven 2.x/3.x
* [Search providers](https://github.com/strongbox/strongbox/wiki/Searching):
  * OrientDB (default implementation for all repositories and layout formats)
  * [Maven Indexer](https://github.com/strongbox/strongbox/wiki/Maven-Indexer) (additional implementation for Maven repositories)
* Cache
  * For performance optimisation and for resolving concurrency issues when authenticate using OrientDB second-level cache EhCache is used
* Directory browsing
* Security
  * HTTP Basic authentication
  * JWT authentication
  * Custom authentication provider based on users that resides in second-level cache that exists in OrientDB
  * Users are predefined in the `etc/conf/strongbox-security-users.xml` file
* [REST API](https://github.com/strongbox/strongbox/wiki/REST-API):
  * Features:
    * Search for artifacts
    * Manage the server's core configuration
    * Manage repositories
    * Manage users
    * Manage logging
    * Manage cron tasks
  * Automated generation of documentation using Swagger
  * Ready-to-use Java-based client(s) covering each REST command.
* [Cron Tasks](https://github.com/strongbox/strongbox/wiki/Cron-Tasks)
  * Implementations:
    * Java
    * Groovy
* Logging:
  * Retrieve logs over HTTP

# What's in the works:
* [Proxy repositories](https://github.com/strongbox/strongbox/wiki/Repositories#proxy)
* [Maven Metadata](https://github.com/strongbox/strongbox/wiki/Maven-Metadata)
* NuGet repository layout
* Cron tasks for core functionality
* Security
  * Expression-based roles and privileges subsystem
* Web UI
* Logging:
  * Log tailing over HTTP
* Deploy as transaction
* Maven settings.xml generator over REST API
* ~~OSGI repository layouts (P2, OBR)~~ [on hold]

# Upcoming:
* RPM distribution
* Debian/Ubuntu distribution

# What's not yet implemented:
* Plugins
* Event handling

# Requirements:
* Java 1.8.x
* Maven 3.3.9 (or higher)

# Download
* Standalone:
  * [tar.gz](https://github.com/strongbox/strongbox-assembly/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.tar.gz), [zip](https://github.com/strongbox/strongbox-assembly/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.zip)
  * [other releases](https://github.com/strongbox/strongbox-assembly/releases)
* Webapp
  * [war](https://github.com/strongbox/strongbox-webapp/releases/download/1.0-SNAPSHOT/strongbox-webapp-1.0-SNAPSHOT.war)
  * [other releases](https://github.com/strongbox/strongbox-webapp/releases)

# Installation
Please check [here](https://github.com/strongbox/strongbox/wiki/Installation) for detailed instruction on how to install and setup up Strongbox on the supported platforms.

# Building
Instructions on how to build the code, can be found [here](https://github.com/strongbox/strongbox/wiki/Building-the-code).

# Contributing
Contributions and contributors are always welcome! For more details, please check [here](https://github.com/strongbox/strongbox/blob/master/CONTRIBUTING.md).
