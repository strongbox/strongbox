package org.carlspring.strongbox.storage.validation.groupId;

/**
 * Created by dinesh on 12/6/17.
 */
public class GroupIdValidationException extends Exception {
    public GroupIdValidationException(){

    }

    public GroupIdValidationException(String message)
    {
        super(message);
    }

    public GroupIdValidationException(String message,
                                      Throwable cause)
    {
        super(message, cause);
    }

    public GroupIdValidationException(Throwable cause)
    {
        super(cause);
    }

}
