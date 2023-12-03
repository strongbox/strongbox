This is the Strongbox cron tasks project.

# Strongbox Cron Tasks

This is the API for configuring Java/Groovy cron tasks on RESTful services. For more details check
[here](https://github.com/strongbox/strongbox/wiki/Cron-Tasks).

## Getting Started

This guidelines will let you know how to integrate your own custom cron task using API either its in Java or in Groovy

### RESTful URLs
* PUT    - http://localhost:48080/configuration/crontasks/crontask (To create a new cron task)
* DELETE - http://localhost:48080/configuration/crontasks/crontask?name=MyCron (To delete an existing cron task)
* GET    - http://localhost:48080/configuration/crontasks/crontask?name=MyCron (To get a cron task by it's name)
* GET    - http://localhost:48080/configuration/crontasks/ (To get list of all configured cron tasks)
* PUT    - http://localhost:48080/configuration/crontasks/crontask/groovy/?cronName=MyCron (To upload a Groovy script for it's cron task)
* GET    - http://localhost:48080/configuration/crontasks/groovy/names (To get list of all the configured Groovy script names)

### Java Cron Task
To create a Java-based cron task, you need to extend the `org.carlspring.strongbox.cron.jobs.JavaCronJob` class
and implement the `executeTask(JobExecutionContext jobExecutionContext)` method as shown below:

```
public class MyTask
        extends JavaCronJob
{

    private final Logger logger = LoggerFactory.getLogger(MyTask.class);

    @Override
    protected void executeTask(JobExecutionContext jobExecutionContext)
            throws JobExecutionException
    {
        logger.debug("My Rest Cron Task is working");
    }
    
}
```

### Groovy Cron Task

To create a Groovy-based cron task, you need to save the cron configuration without any job class and then upload
the Groovy script with cron name that will auto start cron on that scripts 
