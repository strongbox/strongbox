Welcome to the Strongbox Maven artifact repository manager's home.

# What's implemented:
* [Repositories](https://github.com/strongbox/strongbox/wiki/Repositories):
  * Hosted
  * Group
    * Support for repository ordering
    * Support for routing rules
    * Support for nested group repositories
* Indexing
  * Currently using Lucene just for the sake of the PoC, but will be re-worked with OrientDB, or Titan.
* REST API features:
  * Search for artifacts
  * Manage the server's core configuration
  * Manage repositories
* Ready-to-use Java-based REST API Client(s) covering each REST command.
* Logging:
  * Configuration over REST
  * Retrieve logs over HTTP
* Automated generation of REST API documentation using Swagger

# What's in the works:
* Proxy repositories
* [Metadata](https://github.com/strongbox/strongbox/wiki/Maven-Metadata)
* Logging:
  * Log tailing over HTTP
* Deploy as transaction
* Maven settings.xml generator over REST API

# Upcoming:
* Directory browsing
* RPM distribution
* Debian/Ubuntu distribution

# What's not yet implemented:
* Scheduled tasks
* Security
  * Currently there is no proper security set up. The only thing that is implemented is a hard-coded check for HTTP Basic authentication with maven/password as credentials.
* Web UI
* Plugins

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
| OpenSuse 13.2 | [![Build Status](http://dev.carlspring.org/status/jenkins/strongbox)](https://dev.carlspring.org/jenkins/view/strongbox/job/strongbox/) |
| Ubuntu 15.10 | [![Build Status](https://dev.carlspring.org/jenkins/job/strongbox-ubuntu-15.10/badge/icon)](https://dev.carlspring.org/jenkins/job/strongbox-ubuntu-15.10) |
| Windows x64 | [![Build Status](https://dev.carlspring.org/jenkins/view/strongbox-win64/job/strongbox-win64/badge/icon)](https://dev.carlspring.org/jenkins/view/strongbox-win64/job/strongbox-win64/) |
