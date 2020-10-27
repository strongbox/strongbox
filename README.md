<p align="center" width="100%">
    <img src="./logo.png"/>
</p>

[![Master Build Status][master-build-status-badge]][master-build-status-link]
[![Master Build Tests][master-build-tests-badge]][master-build-status-link]
[![RocketChat.Community.Channel][rocket-chat-badge]][rocket-chat-link]
[![License][license-badge]][license-link]
[![Help Contribute to Open Source][codetriage-badge]][codetriage-link]
[![GitHub issues by-label][good-first-issue-badge]][good-first-issue-link]
[![GitHub issues by-label][help-wanted-badge]][help-wanted-link]
[![GitHub issues by-label][hacktoberfest-badge]][hacktoberfest-link]
[![GitHub issues by-label][stackoverflow-badge]][stackoverflow-link]

Welcome to the Strongbox artifact repository manager's home.

# General

Strongbox is a modern OSS artifact repository manager. With a well-developed architecture, it provides native 
implementations for various layout formats, such as [Maven][docs-maven], [NPM][docs-npm], [NuGet][docs-nuget], and [Raw][docs-raw].

All of the implemented layout formats (a.k.a. "[layout providers][docs-providers]") are written natively in Java. 
Our goal is to implement a universal repository manager that can host and serve artifacts in every mainstream format.

Strongbox has a search engine and an [Artifact Query Language][docs-aql].

# What's in the works

* Working on migrating from OrientDB to JanusGraph ([#1649](https://github.com/strongbox/strongbox/issues/1649)).
* Adding support for JDK11 ([#1111](https://github.com/strongbox/strongbox/issues/1111), [#1775](https://github.com/strongbox/strongbox/issues/1649)).
  We only support JDK8 in the `master`. The work for this is based off the `issues/1649` branch.
* Thorough Maven dependency and plugin upgrade (against the `master`). 

# Documentation

You can find our documentation [here][docs].

# Requirements

* Java 1.8.x (we do not currently support higher versions)
* Maven 3.5.4 (or higher)

# Building

Instructions on how to build the code, can be found [here][docs-building-the-code].

# Download

Strongbox is available in the following formats:
* [deb][release-deb]
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
[![GitHub issues by-label][hacktoberfest-badge]][hacktoberfest-link]

# Hackfests

We would like to welcome everybody interested in participating in hackathons and hackfests to join our project and
try to contribute.

We'd like to invite you to:
* Check out the Developer's Guide section of our [wiki](https://strongbox.github.io/developer-guide/getting-started.html).
* Check out our [Hackfests](https://strongbox.github.io/hackfests/index.html) pages.
* [Get the project to build](https://strongbox.github.io/developer-guide/building-the-code.html).
* Have a look at our pre-selected issues for [![GitHub issues by-label][hacktoberfest-badge]][hacktoberfest-link].
* Join our [chat channel](https://chat.carlspring.org/]) to discuss things and get help started.

# Sponsors

We'd also like to thank our sponsors for generously providing us with licenses for our opensource initiative!

[![][jprofiler-logo]][jprofiler-link]

[JProfiler][jprofiler-link] generously supports Strongbox with its full-featured Java Profiler that provides ways to investigate bottlenecks, memory leaks, CPU loads, threading and performance issues.

[![][yourkit-logo]][yourkit-link]

[YourKit][yourkit-link] supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications. YourKit is the creator of [YourKit Java Profiler][yourkit-java-profiler-link], [YourKit .NET Profiler][yourkit-dotnet-profiler-link] and [YourKit YouMonitor][yourkit-monitor-link].


[<--# Generic Links -->]: #
[strongbox-logo]: https://gist.githubusercontent.com/steve-todorov/c71ed46b5b07f9a7a64f507e62899619/raw/fadaea8443814846a3494c2044c74b2b6966761d/logo.png

[<--# Badges -->]: #
[master-build-status-link]: https://jenkins.carlspring.org/blue/organizations/jenkins/strongbox%2Fbuilds%2Fstrongbox/activity?branch=master
[master-build-status-badge]: https://jenkins.carlspring.org/buildStatus/icon?job=strongbox/builds/strongbox/master
[master-build-tests-badge]: https://img.shields.io/jenkins/t/https/jenkins.carlspring.org/job/strongbox/job/builds/job/strongbox/job/master.svg 
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

[hacktoberfest-link]: https://github.com/strongbox/strongbox/issues?q=is%3Aissue+is%3Aopen+label%3A%22hacktoberfest%22
[hacktoberfest-badge]: https://img.shields.io/github/issues-raw/strongbox/strongbox/hacktoberfest.svg?label=hacktoberfest&color=orange

[stackoverflow-link]: https://stackoverflow.com/tags/strongbox/
[stackoverflow-badge]: https://img.shields.io/badge/stackoverflow-ask-orange.svg

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
[release-deb]: https://github.com/strongbox/strongbox/releases/download/1.0-SNAPSHOT/strongbox-distribution-1.0-SNAPSHOT.deb

[<--# Sponsors -->]: #
[jprofiler-logo]: https://www.ej-technologies.com/images/product_banners/jprofiler_large.png
[jprofiler-link]: https://www.ej-technologies.com/products/jprofiler/overview.html

[yourkit-logo]: https://www.yourkit.com/images/yklogo.png
[yourkit-link]: https://www.yourkit.com
[yourkit-java-profiler-link]: https://www.yourkit.com/java/profiler
[yourkit-dotnet-profiler-link]: https://www.yourkit.com/.net/profiler
[yourkit-monitor-link]: https://www.yourkit.com/youmonitor/
