---
name: Dependency or Plugin Upgrade Task
about: Template for creating dependency, or plugin upgrade tasks
title: ''
labels: 'dependencies'
assignees: ''

---

# Task Description

We need to upgrade `com.foo:bar` from `1.2.3` to version `4.5.6`.

<!-- 
     Please, alo list any NOTABLE feacutres/changes in the new version by checking the CHANGELOG.md. 
     List only those changes which which might affect our code or which will be beneficial for us. 

     Remove this note when adding the issue.
-->

# Tasks

The following tasks will need to be carried out:

* [ ] Update the version in the [strongbox-parent] project.
* [ ] Apply any required code changes, or dependency excludes.
* [ ] Test and confirm that there are no issues with the core projects ([strongbox], [strongbox-web-integration-tests], etc) after upgrading.

# Task Relationships

This task:

* Is a sub-task of: 
* Depends on: 
* Is a follow-up of: 
* Relates to:

# Useful Links

* [link1]()
* [link2]()
* [link3]()

# Help

* Check our article on [how to upgrade dependencies and plugins].
* [Our chat](https://chat.carlspring.org/)
* Points of contact:
  * @carlspring
  * @sbespalov
  * @steve-todorov

[strongbox]: https://github.com/strongbox/strongbox/
[strongbox-parent]: https://github.com/strongbox/strongbox-parent/
[strongbox-web-integration-tests]: https://github.com/strongbox/strongbox-web-integration-tests/
[how to upgrade dependencies and plugins]: https://strongbox.github.io/developer-guide/upgrading-dependencies-and-plugins.html
