Welcome to the Strongbox Maven artifact repository manager's home.

# What's implemented:
* Repositories:
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

# What's in the works:
* Proxy repositories
* [Metadata](https://github.com/strongbox/strongbox/wiki/Maven-Metadata)
* Logging:
  * Configuration over REST
  * Log tailing over HTTP
* Deploy as transaction

# Upcoming:
* Directory browsing

# What's not yet implemented:
* Scheduled tasks
* Security
  * Currently there is no proper security set up. The only thing that is implemented is a hard-coded check for HTTP Basic authentication with maven/password as credentials.
* Web UI
* An assembly with a pre-configured Jetty for standalone mode.

# Requirements:
* Java 1.8.x
* Maven 3.2.x

# Download
* Standalone:
  * [tar.gz](https://github.com/strongbox/strongbox-assembly/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.tar.gz), [zip](https://github.com/strongbox/strongbox-assembly/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.zip)
  * [other releases](https://github.com/strongbox/strongbox-assembly/releases)
* Webapp
  * [war](https://github.com/strongbox/strongbox-webapp/releases/download/1.0-SNAPSHOT/strongbox-webapp-1.0-SNAPSHOT.war)
  * [other releases](https://github.com/strongbox/strongbox-webapp/releases)

# Installation
Please check [here](https://github.com/strongbox/strongbox/wiki/Installation) for detailed instruction on how to install and setup up Strongbox on the supported platforms.

# Contributing
Contributions and contributors are always welcome!

[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/strongbox/strongbox?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Build Status
[![Build Status](http://dev.carlspring.org/status/jenkins/strongbox)](https://dev.carlspring.org/jenkins/view/strongbox/job/strongbox/)
