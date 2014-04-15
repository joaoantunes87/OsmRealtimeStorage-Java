package co.realtime.storage.exceptions;

/**
 * The Enum ErrorSourceEnum.
 */
public enum ErrorSourceEnum {

    /** The web service. */
    WEB_SERVICE(1),
    /** The services. */
    SERVICES(2),
    /** The business. */
    BUSINESS(3),
    /** The data access. */
    DATA_ACCESS(4);

    /** The Constant DEFAULT. */
    private final static ErrorSourceEnum DEFAULT = WEB_SERVICE;

    /** The source code. */
    private final int sourceCode;

    /**
     * Instantiates a new error source enum.
     * @param sourceCode
     *            the source code
     */
    private ErrorSourceEnum(final int sourceCode) {
        this.sourceCode = sourceCode;
    }

    /**
     * Gets the source code.
     * @return the source code
     */
    public int getSourceCode() {
        return this.sourceCode;
    }

    /**
     * Value of.
     * @param sourceCode
     *            the source code
     * @return the error source enum
     */
    public static ErrorSourceEnum fromSourceCode(final int sourceCode) {

        for (final ErrorSourceEnum currentErrorSource : ErrorSourceEnum.values()) {
            if (currentErrorSource.sourceCode == sourceCode) {
                return currentErrorSource;
            }
        }

        return DEFAULT;

    }

}
