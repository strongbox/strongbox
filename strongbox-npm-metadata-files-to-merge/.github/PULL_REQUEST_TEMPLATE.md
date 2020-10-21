# Pull Request Description

This pull request closes strongbox/strongbox#

# Acceptance Test

* [ ] Building the code with `mvn clean install -Dintegration.tests` still works.
* [ ] Running `mvn spring-boot:run` in the `strongbox-web-core` still starts up the application correctly.
* [ ] Building the code and running the `strongbox-distribution` from a `zip` or `tar.gz` still works.
* [ ] The tests in the [`strongbox-web-integration-tests`](https://github.com/strongbox/strongbox-web-integration-tests/) still run properly.

# Questions

* Does this pull request break backward compatibility? 
  * [ ] Yes
  * [ ] No

* Does this pull request require other pull requests to be merged first? 
  * [ ] Yes, please see #...
  * [ ] No

* Does this require an update of the documentation?
  * [ ] Yes, please see strongbox/strongbox-docs#{PR_NUMBER}
  * [ ] No

# Code Review And Pre-Merge Checklist

* [ ] My code follows the [coding convention](https://strongbox.github.io/developer-guide/coding-convention.html) of this project.
* [ ] I have performed a self-review of my own code.
* [ ] I have commented my code in hard-to-understand areas.
* [ ] My changes generate no new warnings.
* [ ] I have added tests that prove my fix is effective or that my feature works.
* [ ] New and existing unit tests pass locally with my changes.
