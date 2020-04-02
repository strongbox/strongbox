# General

This project is a research and development effort lead by Carlspring Consulting & Development Ltd. with the goal of providing an alternative to existing artifact repository managers. We aim to create a robust and easy to use, well-developed universal artifact repository manager with native implementations of the layout formats which we support. We are building on top of a modern, well-devised architecture and need help in various areas such as research, development and testing.

We are also truly grateful to all of our countless contributors, without whom we wouldn't have come so far!

# Who Can Help

We are always pleased to get help with our project! Both experienced and not so experienced developers are welcome!

There are many tasks you can help with, so please feel free to look around our [issue tracker](https://github.com/strongbox/strongbox/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22+label%3A%22good+first+issue%22) for some good first issues that we need help with.

Let us know what you're good at on out [chat] channel, as well as what sort of tasks you're typically interested in, or what areas of your skills you'd like to develop and we'll try to accomodate, by finding you the right tasks. It might be the case that we have tasks that are suitable for you, but just haven't yet been added to the issue tracker, so feel free to ask us how you can help!

We try our best to help new-joiners get started and eased into the project.

*Please, note that while a certain level of help will be provided to newcomers, anyone who wishes to contribute to this project must be able to work independently.*

## Academics

While our aim is provide a stable usable application, we are also treating the project, as if it were a "learning excercise" meaning that we're all here to learn cool new things and share the knowledge. If you'd like to see an exciting new technology, or framework on this project and have a good use case for it, we'd be open to hear about it and help you implement it.

### Professors

Please, reach out to us, if you're teaching programming classes and would like your students to do some work on OSS projects!

Either raise a question in the issue tracker, join our [chat], or contact @carlspring for more details. We would be happy to hear about your curriculum and expectations of your students. 

### Students And Interns

We welcome students from all backgrounds, who have sane knowledge of programming, a willingness to learn, an openness to constructive criticism and a passion to deliver working code!

Finding your first few jobs might sometimes be challenging, but having contributed to an OSS project could give your CV quite a boost, as it shows initiative, dedication and self-drivenness, among other things.

# How To Help

We could use all the help we can get, so, please feel free to have a good look at our issue tracker and find something of interest, that you think you would be able to help with and just add a comment under the respective issue that you'll be looking into it. If somebody else was looking at it, but seems to have been inactive for more than a few days, please feel free to ask them if they've abandoned the task, if they're blocked, or waiting for information. They might still be researching the topic, but also, please keep in mind that sometimes people can no longer work on an issue (time constraints, change of circumstances, etc)

# Code Style

While we appreciate all the help we can get, here are some more details on the [coding convention](https://strongbox.github.io/developer-guide/coding-convention.html). Please, follow these guidelines and set up your IDE to use the respective code style configuration file.

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

To accept, please:
* Fill in all the mandatory fields
  * `Full name` (**mandatory**)
  * `Company/Organization/University` (**optional** -- please, only fill this, if you're contributing work on behalf of a company, organization, or are studying)
  * `E-mail` (**mandatory**)
  * `Mailing address` (**mandatory**)
  * `Country` (**mandatory**)
  * `Telephone` (**optional**)
* Print, sign and scan the [Individual Contributor's License Agreement (ICLA)](https://github.com/strongbox/strongbox/blob/master/ICLA.md), or, alternatively, fill in the [ICLA PDF](https://strongbox.github.io/assets/resources/pdfs/ICLA.pdf) file and mail it back to [carlspring@gmail.com](mailto:carlspring@gmail.com).
* Add your name and basic details below and open a pull request.


**Notes:** Please, note that none of this information is shared with third-parties and is only required due to the legal agreement which you will be entering when contributing your code to the project. We require this minimal amount of information in order to be able to identify you, as we're not keeping record, or more sensitive information, such as passport/ID details. We will not send you any spam, or share your details with third parties.

<!--  Please keep the empty line above! -->
[chat]: https://chat.carlspring.org/
[issue tracker]: https://github.com/strongbox/strongbox/issues?utf8=%E2%9C%93&q=is%3Aissue+is%3Aopen+label%3A%22help+wanted%22+label%3A%22good+first+issue%22
<!-- Please keep the empty line below! -->

# Contributors

| Name                         | Company / Organization / University          | Location                                | Date       |
|------------------------------|----------------------------------------------|-----------------------------------------|------------|
| Martin Todorov               | Carlspring Consulting & Development Ltd.     | London, United Kingdom                  | 2013-08-02 |
| Steve Todorov                | Carlspring Consulting & Development Ltd.     | Sofia, Bulgaria                         | 2014-01-12 |
| Dmytro Chyzhykov             |                                              | Frankfurt am Main, Germany              | 2014-06-07 |
| Nicolay Karakulov            |                                              | Kharkhiv, Ukraine                       | 2014-10-05 |
| Denis Ivaykin                |                                              | Moscow, Russia                          | 2014-04-19 |
| Juan Ignacio Bais            |                                              | Buenos Aires, Argentina                 | 2016-02-24 |
| Ivan Ursul                   |                                              | Lviv, Ukraine                           | 2016-05-02 |
| Alex Oreshkevich             | redsoft.pro                                  | Minsk, Republic of Belarus              | 2016-05-12 |
| Faisal Hameed                | DevFactory                                   | Lahore, Islamic Republic of Pakistan    | 2016-06-10 |
| Orest Kyrylchuk              |                                              | Lviv, Ukraine                           | 2016-05-28 |
| Bohdan Hliva                 |                                              | Lviv, Ukraine                           | 2016-08-09 |
| Yougeshwar Khatri            |                                              | Karachi, Pakistan                       | 2016-04-01 |
| Sergey Bespalov              |                                              | Novosibirsk, Russia                     | 2016-11-02 |
| Sergey Panov                 |                                              | Kiev, Ukraine                           | 2016-11-02 |
| Nenko Tabakov                |                                              | Sofia, Bulgaria                         | 2016-11-07 |
| Kate Novik                   | redsoft.pro                                  | Minsk, Republic of Belarus              | 2016-11-16 |
| Dmitry Sviridov              |                                              | Moscow, Russia                          | 2016-11-23 |
| Przemyslaw Fusik             |                                              | Maszewo, Poland                         | 2017-04-09 |
| Dinesh Arora                 |                                              | Charlotte, USA                          | 2017-12-07 |
| Sanket Sawant                |                                              | Mumbai, India                           | 2017-12-09 |
| Pablo Tirado                 |                                              | Madrid, Spain                           | 2018-01-05 |
| Gokhan Kuyucak               |                                              | Izmir, Turkey                           | 2018-01-14 |
| Guido Grazioli               |                                              | London, United Kingdom                  | 2018-01-15 |
| Maxim Antonov                |                                              | Moscow, Russian Federation              | 2018-01-14 |
| Aditya Srinivasan            |                                              | Washington DC, USA                      | 2018-02-02 |
| Sevastyan Pigarev			   |   				    						  | Barnaul, Russian Federation				| 2018-03-27 |
| Mariusz Kaligowski		   |  				    						  | Juszczyn, Poland                        | 2018-04-26 |
| Michael Altenburger          | 				    		                  | Vienna, Austria                     	| 2018-09-29 |
| Leonora Der                  | ELTE Faculty of Informatics                  | Budapest, Hungary                       | 2018-10-20 |
| Benjamin March               |                                 			  | Munich, Germany                         | 2018-10-28 |
| Konstantina Papadopoulou	   | Aristotle University of Thessaloniki    	  | Thessaloniki, Greece		            | 2018-12-28 |
| Jitesh Golatkar              |                                              | Charlotte, USA                          | 2019-01-25 |
| Forrest Whiting              | Forbes Media                                 | Jersey City, New Jersey, USA            | 2019-02-04 |
| Dawid Antecki                |                                              | Bielsko-Biala, Poland                   | 2019-02-18 |
| Ekrem Candemir               |                                              | Ankara, Turkey                          | 2019-02-24 |
| Eugeniusz Fedchenko          |                                              | Warsaw, Poland                          | 2019-02-25 |
| Shumail Ahmed                | Middlesex University                         | London, United Kingdom                  | 2019-03-05 |
| Oleksandr Gryniuk            |                                              | Kyiv, Ukraine                           | 2019-03-07 |
| Kaloyan Dimitrov             |                                              | Sofia, Bulgaria                         | 2019-03-12 |
| Leif Brooks                  |                                              | Charlotte, USA                          | 2019-03-13 |
| Mujahid Thoufeek             |                                              | Colombo, Sri Lanka                      | 2019-03-19 |
| Sarfraz Anwar                |                                              | Bengaluru, India                        | 2019-03-19 |
| Brian Hill                   |                                              | Orlando, Florida, USA                   | 2019-03-26 |
| Ben Falter                   |                                              | New York, New York, USA                 | 2019-04-04 |
| Evrim Nur Celik              |                                              | Istanbul, Turkey                        | 2019-04-14 |
| Akhilesh Bedre               |                                              | Hyderabad, India                        | 2019-04-07 |
| Alec Goldberg                | University of Michigan                       | Saint Louis, USA                        | 2019-04-10 |
| David Whalen                 | University of Michigan                       | Ann Arbor, Michigan, USA                | 2019-04-13 |
| Theodore Ravindranathh       |                                              | Chennai, India                          | 2019-04-13 |
| Solomon Yakubov              |                                              | New York, New York, USA                 | 2019-04-15 |
| Yugander Krishan Singh       |                                              | Himachal Pradesh, India                 | 2019-04-25 |
| Shubhang Sharma              |                                              | Rajasthan, India                        | 2019-06-09 |
| Virakti Jain                 |                                              | Hyderabad, India                        | 2019-06-11 |
| Serdyuk Sergey               |                                              | Moscow, Russian Federation              | 2019-06-20 |
| Bartosz Dabek                |                                              | Hajnowka, Poland                        | 2019-06-30 |
| Bogdan Sukonnov              |                                              | Saint Petersburg, Russia                | 2019-08-19 |
| Marcin Słowiak               |                                              | Sanok, Poland                           | 2019-09-23 |
| David Barda                  |                                              | Tel-Aviv, Israel                        | 2019-10-14 |
| Bishnu Gopal Patro           |                                              | Pune, India                             | 2019-10-19 |
| Bartosz Czyzowski            |                                              | Jozefow, Poland                         | 2019-11-17 |
| Ankit Tomar                  |                                              | Gurgaon, India                          | 2019-11-10 |
| Valentin Bojinov             |                                              | Sofia, Bulgaria                         | 2020-04-01 |

