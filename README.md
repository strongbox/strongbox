Welcome to the Strongbox Maven artifact repository manager's home.

# What's implemented:
* [Repositories](https://github.com/strongbox/strongbox/wiki/Repositories):
  * Hosted
  * Group
    * Support for repository ordering
    * Support for routing rules
    * Support for nested group repositories
* Directory browsing
* Indexing
  * Currently using Lucene just for the sake of the PoC, but will be re-worked with OrientDB, or Titan.
* Persistence
  * All necessary data persisted using customised open-source spring-data-orientdb connector in OrientDB
* Caching
  * For performance optimisation and for resolving concurrency issues when authenticate using OrientDB second-level cache EhCache is used
* Security
  * HTTP Basic authentication
  * Custom authentication provider based on users that resides in second-level cache that exists in OrientDB
  * Users are predefined in the `etc/conf/security-users.xml` file
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
    * [Java](https://github.com/strongbox/strongbox-cron-tasks#java-cron-task)
    * [Groovy](https://github.com/strongbox/strongbox-cron-tasks#groovy-cron-task)
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

[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/strongbox/strongbox?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Build Status

| Operating System | Status | 
| --- | ------ |
| OpenSuse 13.2 | [![Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox)](https://dev.carlspring.org/jenkins/job/strongbox/job/strongbox/) |
| Debian 8.4 | [![Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox-debian-x64)](https://dev.carlspring.org/jenkins/job/strongbox/view/debian-x64/job/strongbox-debian-x64/) |
| Ubuntu 15.10 | [![Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox-ubuntu-15.10)](https://dev.carlspring.org/jenkins/job/strongbox/view/ubuntu-x64/job/strongbox-ubuntu-15.10/) |
| CentOS 7.2 | [![Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox-centos-x64)](https://dev.carlspring.org/jenkins/job/strongbox/view/centos-x64/job/strongbox-centos-x64/) |
| Windows x64 | [![Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox-win64)](https://dev.carlspring.org/jenkins/job/strongbox/view/win-x64/job/strongbox-win64/) |
