This is the Strongbox cron tasks project.

# Strongbox Cron Tasks
An API for configuring Java/Groovy cron tasks on RESTful services

## Getting Started

This guidelines will let you know how to integrate your own custom cron task using API either its in Java or in Groovy

### RESTful URLs
* PUT    - http://example.com/config/crontasks/crontask (To create new cron task)
* DELETE - http://example.com/config/crontasks/crontask?name=MyCron (To delete existing cron task)
* GET    - http://example.com/config/crontasks/crontask?name=MyCron (To get cron task by its name)
* GET    - http://example.com/config/crontasks/ (To get list of all configured cron tasks)
* PUT    - http://example.com/config/crontasks/crontask/groovy/?cronName=MyCron (To upload groovy script for its cron task)
* GET    - http://example.com/config/crontasks/groovy/names (To get list of all configured groovy scripts names)

### Java Cron Task
To create a java based cron task you need to extend a class org.carlspring.strongbox.cron.api.jobs.JavaCronJob and implements an abstract method executeInternal()

```
public class MyTask
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(MyTask.class);

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("My Rest Cron Task is working");
    }
}
```

### Groovy Cron Task

To create a groovy based cron task you need to save cron configuration without any job class, after that upload the groovy scripts with cron name that will auto start cron on that scripts 

# Build Status

[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/strongbox/strongbox-cron-tasks?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://dev.carlspring.org/jenkins/buildStatus/icon?job=strongbox/strongbox-cron-tasks)](https://dev.carlspring.org/jenkins/job/strongbox/job/strongbox-cron-tasks/)

# Contributing

Contributors are always welcome! For more details, please check [here](https://github.com/strongbox/strongbox/blob/master/CONTRIBUTING.md).

# License

This project is licensed under an Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details
