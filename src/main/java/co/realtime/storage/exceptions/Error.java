package co.realtime.storage.exceptions;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * The Class ErrorResponse.
 */
public class Error {

    /** The Constant CODE_SIZE. */
    private static final int CODE_SIZE = 3;

    /** The code. */
    private final String code;

    /** The message. */
    private final String message;

    /** The error type. */
    private final ErrorTypeEnum errorType;

    /** The error source. */
    private final ErrorSourceEnum errorSource;

    /**
     * Instantiates a new error response.
     * @param code
     *            the code
     * @param message
     *            the message
     */
    public Error(final String code, final String message) {

        this.code = code;
        this.message = message;

        if (code == null || code.length() != CODE_SIZE || !NumberUtils.isDigits(code)) {
            throw new IllegalArgumentException(String.format("The argument code must an integer and has the size %d", Integer.valueOf(CODE_SIZE)));
        }

        this.errorSource = ErrorSourceEnum.fromSourceCode(Integer.valueOf(code.substring(0, 1)).intValue());
        this.errorType = ErrorTypeEnum.valueOf(code.substring(1, CODE_SIZE));

    }

    /**
     * Instantiates a new error.
     * @param source
     *            the source
     * @param type
     *            the type
     * @param message
     *            the message
     */
    public Error(final ErrorSourceEnum source, final ErrorTypeEnum type, final String message) {

        if (source == null || type == null) {
            throw new IllegalArgumentException("The arguments source and type are required");
        }

        this.errorSource = source;
        this.errorType = type;
        this.message = message;
        this.code = source.getSourceCode() + type.getCode();

    }

    /**
     * Gets the code.
     * @return the code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the error type.
     * @return the error type
     */
    public ErrorTypeEnum getErrorType() {
        return this.errorType;
    }

    /**
     * Gets the error source.
     * @return the error source
     */
    public ErrorSourceEnum getErrorSource() {
        return this.errorSource;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("Code: %d; Message: %s ", this.code, this.message);
    }

}
