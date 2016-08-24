package org.carlspring.strongbox.rest.exception;

/**
 * @author Alex Oreshkevich
 */
/**
 * Class to map application related exceptions
 *
 * @author amacoder
 *
 */
public class AppException extends Exception {

    private static final long serialVersionUID = -8999932578270387947L;

    /**
     * contains redundantly the HTTP status of the response sent back to the client in case of error, so that
     * the developer does not have to look into the response headers. If null a default
     */
    Integer status;

    /** application specific error code */
    int code;

    /** link documenting the exception */
    String link;

    /** detailed error description for developers*/
    String developerMessage;

    /**
     *
     * @param status
     * @param code
     * @param message
     * @param developerMessage
     * @param link
     */
    public AppException(int status, int code, String message,
                        String developerMessage, String link) {
        super(message);
        this.status = status;
        this.code = code;
        this.developerMessage = developerMessage;
        this.link = link;
    }

    public AppException() { }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
