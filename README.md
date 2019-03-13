# ![strongbox](./strongbox.svg)

[![Master Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox/master)](https://dev.carlspring.org/jenkins/blue/organizations/jenkins/strongbox%2Fstrongbox/activity?branch=master)
[![RocketChat.Community.Channel](https://chat.carlspring.org/images/join-chat.svg)](https://chat.carlspring.org/channel/community)
[![License](https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg)](https://opensource.org/licenses/Apache-2.0)
[![Help Contribute to Open Source](https://www.codetriage.com/strongbox/strongbox/badges/users.svg)](https://www.codetriage.com/strongbox/strongbox)
[![Jenkins tests](https://img.shields.io/jenkins/t/https/jenkins.carlspring.org/job/strongbox/job/strongbox/job/master.svg)](https://jenkins.carlspring.org/job/strongbox/job/strongbox/job/master/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/50f1af2c3b2d4e31a5c686c9a9395cd2)](https://www.codacy.com/app/strongbox/strongbox?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=strongbox/strongbox&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/50f1af2c3b2d4e31a5c686c9a9395cd2)](https://www.codacy.com/app/strongbox/strongbox?utm_source=github.com&utm_medium=referral&utm_content=strongbox/strongbox&utm_campaign=Badge_Coverage)

Welcome to the Strongbox artifact repository manager's home.

## What's implemented:
* [Repositories](https://strongbox.github.io/knowledge-base/repositories.html):
  * Hosted
  * Proxy
  * Group
    * Support for repository ordering
    * Support for routing rules
    * Support for nested group repositories
* Layout providers:
  * [Maven 2.x/3.x](https://strongbox.github.io/developer-guide/layout-providers/maven-2-layout-provider.html)
  * [NPM](https://strongbox.github.io/developer-guide/layout-providers/npm-layout-provider.html)
  * [NuGet v2](https://strongbox.github.io/developer-guide/layout-providers/nuget-layout-provider.html)
  * [Raw](https://strongbox.github.io/developer-guide/layout-providers/raw-layout-provider.html)
* Search
  * [Search providers](https://strongbox.github.io/developer-guide/search-providers.html):
    * OrientDB (default implementation for all repositories and layout formats)
    * [Maven Indexer](https://strongbox.github.io/developer-guide/maven-indexer.html) (additional implementation for Maven repositories)
  * [Artifact Query Language](https://strongbox.github.io/user-guide/artifact-query-language.html)
* Directory browsing
* Security
  * HTTP Basic authentication
  * JWT authentication
  * LDAP
  * Custom authentication provider based on users that resides in second-level cache that exists in OrientDB
  * Users are predefined in the `etc/conf/strongbox-security-users.xml` file
* [REST API](https://strongbox.github.io/user-guide/rest-api.html):
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
* [Event handling](https://strongbox.github.io/developer-guide/using-the-event-api.html)
* Logging:
  * Retrieve logs over HTTP

## What's in the works (Q1/2019):
* Web UI
* Spring Bootification
* Convert the strongbox configuration files from XML to YAML format ([#965](https://github.com/strongbox/strongbox/issues/965), [#1056](https://github.com/strongbox/strongbox/pull/1056))

## Upcoming:
* Logging:
  * Log tailing over HTTP
* Deploy as a transaction
* Debian/Ubuntu distribution

## What's not yet implemented:
* Plugins

## Requirements:
* Java 1.8.x
* Maven 3.3.9 (or higher)

## Download
* Standalone:
  * [rpm](https://github.com/strongbox/strongbox/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.rpm), [tar.gz](https://github.com/strongbox/strongbox/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.tar.gz), [zip](https://github.com/strongbox/strongbox/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.zip)
  * [other releases](https://github.com/strongbox/strongbox/releases)

# Installation
Please check [here](https://strongbox.github.io/user-guide/getting-started.html) for detailed instruction on how to install and setup up Strongbox on the supported platforms.

# Building
Instructions on how to build the code, can be found [here](https://strongbox.github.io/developer-guide/building-the-code.html).

# Contributing
Contributions and contributors are always welcome! For more details, please check [here](https://strongbox.github.io/contributing.html).
We have a helpful community of developers on our [channel](https://chat.carlspring.org/channel/community), please feel free to drop by, if you have questions, issues, or would like to contribute!

