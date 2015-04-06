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

# What's in the works:
* Proxy repositories
* Metadata
* Deploy as transaction

# Upcoming:
* Directory browsing
* Logging:
  * Configuration over REST
  * Log tailing over HTTP

# What's not yet implemented:
* Scheduled tasks
* Security
  * Currently there is no proper security set up. The only thing that is implemented is a hard-coded check for HTTP Basic authentication with maven/password as credentials.
* Web UI
* An assembly with a pre-configured Jetty for standalone mode.

# Requirements:
* Java 1.7.x
* Maven 3.2.x

# Contributing
Contributions and contributors are always welcome!

[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/strongbox/strongbox?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Build Status
[![Build Status](http://dev.carlspring.org/status/jenkins/strongbox)](https://dev.carlspring.org/jenkins/view/strongbox/job/strongbox/)
