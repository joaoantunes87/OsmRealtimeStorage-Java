package co.realtime.storage.api;

/**
 * The Class ErrorResponse.
 */
public class Error {

    /** The code. */
    private String code;

    /** The message. */
    private String message;

    /**
     * Instantiates a new error response.
     */
    public Error() {

    }

    /**
     * Instantiates a new error response.
     * @param code
     *            the code
     * @param message
     *            the message
     */
    public Error(final Integer code, final String message) {

    }

    /**
     * Gets the code.
     * @return the code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Sets the code.
     * @param code
     *            the new code
     */
    public void setCode(final String code) {
        this.code = code;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Sets the message.
     * @param message
     *            the new message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("Code: %d; Message: %s ", this.code, this.message);
    }

}
