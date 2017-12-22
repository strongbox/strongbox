
# Code Style

While we appreciate all the help we can get, here are some more details on the [coding convention](https://github.com/strongbox/strongbox/wiki/Coding-Convention). Please, follow these guidelines and set up your IDE to use the respective code style configuration file.

# Code of Conduct

If would like to contribute to our project and contribute to our work, please follow our [Code of Conduct](https://github.com/strongbox/strongbox/blob/master/CODE-OF-CONDUCT.md).

# Pull Requests

Please, follow these basic rules when creating pull requests. Pull requests:
* Should:
  * Be tidy
  * Easy to read
  * Have optimized imports
  * Contain a sufficient amount of comments, if this is a new feature
  * Where applicable, contain a reasonable, (even, if it's just a minimalistic), set of test cases, that cover the bases
* Should not:
  * Change the existing formatting of code, unless this is really required, especially of files that have no other changes, or are not related to the pull request at all. (Please, don't enable pre-commit features in IDE-s such as "Reformat code", "Re-arrange code" and so on, as this may add extra noise to the pull and make the diff harder to read. When adding, or changing code, apply the re-formatting, only to the respective changed code blocks).
  * Have unresolved merge conflicts with the base branch
  * Have failing tests
  * Contain unaddressed **critical** issues reported by Sonar
  * Have commented out dead code. (Commented out code is fine, just not blocks and blocks of it).
  * Contain `public static void(String[] args])` methods (as those would clearly have been used for the sake of quick testing without an actual test case)

Once you've created a new pull request, kindly first review the diff thoroughly yourselves, before requesting it to be reviewed by others and merged.

# Legal

Contributing code to this project means that you accept your contributions to be
licensed under the Apache 2.0 license. Signing the ICLA below covers contributions to:
* Any project in the [Strongbox](https://github.com/strongbox) organization on Github
* Any dependent project in the [carlspring](https://github.com/carlspring) repositories

To accept, please:
- Print, sign and scan the [Individual Contributor's License Agreement (ICLA)](https://github.com/strongbox/strongbox/blob/master/ICLA.md), or, alternatively, fill in the [ICLA PDF](https://github.com/strongbox/strongbox/wiki/resources/pdfs/ICLA.pdf) file
  and mail it back to [martin.todorov@carlspring.com](mailto:martin.todorov@carlspring.com).
- Place your name and e-mail below and open a pull request.

| Name                         | Company                                  | Location                                | Date       |
|------------------------------|------------------------------------------|-----------------------------------------|------------|
| Martin Todorov               | Carlspring Consulting & Development Ltd. | London, United Kingdom                  | 2013-08-02 |
| Steve Todorov                | Carlspring Consulting & Development Ltd. | Sofia, Bulgaria                         | 2014-01-12 |
| Dmytro Chyzhykov             |                                          | Frankfurt am Main, Germany              | 2014-06-07 |
| Nicolay Karakulov            |                                          | Kharkhiv, Ukraine                       | 2014-10-05 |
| Denis Ivaykin                |                                          | Moscow, Russia                          | 2014-04-19 |
| Juan Ignacio Bais            |                                          | Buenos Aires, Argentina                 | 2016-02-24 |
| Ivan Ursul                   |                                          | Lviv, Ukraine                           | 2016-05-02 |
| Alex Oreshkevich             | redsoft.pro                              | Minsk, Republic of Belarus              | 2016-05-12 |
| Faisal Hameed                | DevFactory                               | Lahore, Islamic Republic of Pakistan    | 2016-06-10 |
| Orest Kyrylchuk              |                                          | Lviv, Ukraine                           | 2016-05-28 |
| Bohdan Hliva                 |                                          | Lviv, Ukraine                           | 2016-08-09 |
| Yougeshwar Khatri            |                                          | Karachi, Pakistan                       | 2016-04-01 |
| Kate Novik                   | redsoft.pro                              | Minsk, Republic of Belarus              | 2016-11-16 |
| Sergey Bespalov              |                                          | Novosibirsk, Russia                     | 2016-11-02 |
| Sergey Panov                 |                                          | Kiev, Ukraine                           | 2016-11-02 |
| Nenko Tabakov                |                                          | Sofia, Bulgaria                         | 2016-11-07 |
| Przemyslaw Fusik             |                                          | Maszewo, Poland                         | 2017-04-09 |
| Dinesh Arora                 |                                          | Charlotte, USA                          | 2017-12-07 |
| Sanket Sawant                |                                          | Mumbai, India                           | 2017-12-09 |
| Andrey Mochalov              |                                          | Saint-Petersburg, Russia                | 2017-12-22 |
