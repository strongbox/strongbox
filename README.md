# ![strongbox-logo][strongbox-logo]

[![Master Build Status][master-build-status-badge]][master-build-status-link]
[![Master Build Tests][master-build-tests-badge]][master-build-status-link]
[![RocketChat.Community.Channel][rocket-chat-badge]][rocket-chat-link]
[![License][license-badge]][license-link]
[![Help Contribute to Open Source][codetriage-badge]][codetriage-link]
[![GitHub issues by-label][good-first-issue-badge]][good-first-issue-link]
[![GitHub issues by-label][help-wanted-badge]][help-wanted-link]

Welcome to the Strongbox artifact repository manager's home.

# General

Strongbox is a modern OSS artifact repository manager. With a well-developed architecture, it provides native 
implementations for various layout formats, such as [Maven][docs-maven], [NPM][docs-npm], [NuGet][docs-nuget], and [Raw][docs-raw].

All of the implemented layout formats (a.k.a. "[layout providers][docs-providers]") are written natively in Java. 
Our goal is to implement a universal repository manager that can host and serve artifacts in every mainstream format.

Strongbox has a search engine and an [Artifact Query Language][docs-aql].

# What's in the works (Q2/2019)

* Web UI
* Spring Bootification
* NuGet (protocol v2) layout support finalization [#1215](https://github.com/strongbox/strongbox/issues/1215)
* PyPi (Wheel) layout support ([#807](https://github.com/strongbox/strongbox/issues/807), [#808](https://github.com/strongbox/strongbox/issues/808), [#810](https://github.com/strongbox/strongbox/issues/810), [#811](https://github.com/strongbox/strongbox/issues/811), [#812](https://github.com/strongbox/strongbox/issues/812), [#813](https://github.com/strongbox/strongbox/issues/813), [#814](https://github.com/strongbox/strongbox/issues/814), [#815](https://github.com/strongbox/strongbox/issues/815), [#816](https://github.com/strongbox/strongbox/issues/816), [#1179](https://github.com/strongbox/strongbox/issues/1179), [#1180](https://github.com/strongbox/strongbox/issues/1180), [#1185](https://github.com/strongbox/strongbox/issues/1185))

# Upcoming

* Log tailing over HTTP
* Debian/Ubuntu distribution

# News

* We have recently:
  * Upgraded to JUnit 5 and we've been busy making all of our tests parallelizable! (Please, reach out, if you'd like to help, as there is still work to be done!).
  * Migrated to Spring Boot! (We may need help improving our support).
  * Migrated our Github wiki to [gihub.io](https://strongbox.github.io/). 
  * Migrated our configuration files from XML to YAML format.
* We now have an RPM distribution and need help testing it! (We need testers).
* Our UI is under heavy development!

# Documentation

You can find our documentation [here][docs].

# Requirements

* Java 1.8.x
* Maven 3.3.9 (or higher)

# Building

Instructions on how to build the code, can be found [here][docs-building-the-code].

# Download

Strongbox is available in the following formats:
* [rpm][release-rpm]
* [tar.gz][release-tar.gz]
* [zip][release-zip]

Other release could be downloaded from [here][release-all].

# Installation

Please check [here][docs-user-getting-started] for detailed instruction on how to install and setup up Strongbox on the 
supported platforms.

# Contributing

Contributions and contributors are always welcome! For more details on how to contribute, please check [here][docs-contributing]. 
We are also looking for people who would like to test our code and help improve our documentation!

We have a helpful community of developers on our [chat channel][rocket-chat-link], so, please feel free to drop by, if 
you have questions, issues, or would like to contribute!

We need your help to make Strongbox better! Please, have a look at issues with these labels, if you'd like to get 
started with our project:

[![GitHub issues by-label][good-first-issue-badge]][good-first-issue-link]
[![GitHub issues by-label][help-wanted-badge]][help-wanted-link]


[<--# Generic Links -->]: #
[strongbox-logo]: ./strongbox.svg

[<--# Badges -->]: #
[master-build-status-link]: https://dev.carlspring.org/jenkins/blue/organizations/jenkins/strongbox%2Fstrongbox/activity?branch=master
[master-build-status-badge]: https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox/master
[master-build-tests-badge]: https://img.shields.io/jenkins/t/https/jenkins.carlspring.org/job/strongbox/job/strongbox/job/master.svg 
[rocket-chat-link]: https://chat.carlspring.org/channel/community
[rocket-chat-badge]: https://chat.carlspring.org/images/join-chat.svg
[license-link]: https://opensource.org/licenses/Apache-2.0
[license-badge]: https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg
[codetriage-link]: https://www.codetriage.com/strongbox/strongbox
[codetriage-badge]: https://www.codetriage.com/strongbox/strongbox/badges/users.svg
[good-first-issue-link]: https://github.com/strongbox/strongbox/issues?q=is%3Aissue+is%3Aopen+label%3A%22good%20first%20issue%22
[good-first-issue-badge]: https://img.shields.io/github/issues-raw/strongbox/strongbox/good%20first%20issue.svg?label=good%20first%20issue
[help-wanted-link]: https://github.com/strongbox/strongbox/issues?q=is%3Aissue+is%3Aopen+label%3A%22help%20wanted%22
[help-wanted-badge]: https://img.shields.io/github/issues-raw/strongbox/strongbox/help%20wanted.svg?label=help%20wanted&color=%23856bf9& 

[<--# Docs links -->]: #
[docs]: https://strongbox.github.io/
[docs-maven]: https://strongbox.github.io/developer-guide/layout-providers/maven-2-layout-provider.html
[docs-npm]: https://strongbox.github.io/developer-guide/layout-providers/npm-layout-provider.html
[docs-nuget]: https://strongbox.github.io/developer-guide/layout-providers/nuget-layout-provider.html
[docs-raw]: https://strongbox.github.io/developer-guide/layout-providers/raw-layout-provider.html
[docs-providers]: https://strongbox.github.io/knowledge-base/layout-providers.html
[docs-building-the-code]: https://strongbox.github.io/developer-guide/building-the-code.html
[docs-user-getting-started]: https://strongbox.github.io/user-guide/getting-started.html
[docs-contributing]: https://strongbox.github.io/contributing.html
[docs-aql]: https://strongbox.github.io/user-guide/artifact-query-language.html

[<--# Release links -->]: #
[release-all]: https://github.com/strongbox/strongbox/releases
[release-rpm]: https://github.com/strongbox/strongbox/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.rpm
[release-tar.gz]: https://github.com/strongbox/strongbox/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.tar.gz
[release-zip]: https://github.com/strongbox/strongbox/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.zip
