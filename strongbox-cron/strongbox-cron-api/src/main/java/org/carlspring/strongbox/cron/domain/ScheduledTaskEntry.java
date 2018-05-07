package org.carlspring.strongbox.cron.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.Entity;
import java.util.Date;

/**
 * @author Aditya Srinivasan
 */
@Entity
public class ScheduledTaskEntry
        extends GenericEntity
{

    private String taskName;
    private String threadName;
    private Date startDateTime;
    private boolean endDateTime;
    private String status;
    private String exceptionMessage;

    public ScheduledTaskEntry()
    {
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public String getThreadName()
    {
        return threadName;
    }

    public void setThreadName(String threadName)
    {
        this.threadName = threadName;
    }

    public Date getStartDateTime()
    {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime)
    {
        this.startDateTime = startDateTime;
    }

    public boolean isEndDateTime()
    {
        return endDateTime;
    }

    public void setEndDateTime(boolean endDateTime)
    {
        this.endDateTime = endDateTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getExceptionMessage()
    {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage)
    {
        this.exceptionMessage = exceptionMessage;
    }
}
