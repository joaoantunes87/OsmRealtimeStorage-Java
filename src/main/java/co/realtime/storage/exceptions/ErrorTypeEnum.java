package co.realtime.storage.exceptions;

/**
 * The Enum ErrorTypeEnum.
 */
public enum ErrorTypeEnum {

    /** The invalid type. */
    INVALID_TYPE("02"),
    /** The missing argument. */
    MISSING_ARGUMENT("03"),
    /** The invalid request. */
    INVALID_REQUEST("04"),
    /** The access not allowed. */
    ACCESS_NOT_ALLOWED("05"),
    /** The unauthentication. */
    UNAUTHENTICATION("06"),
    /** The unauthorized. */
    UNAUTHORIZED("07"),
    /** The profile not found. */
    PROFILE_NOT_FOUND("08"),
    /** The credentials mismatch. */
    CREDENTIALS_MISMATCH("09"),
    /** The operation unavailable. */
    OPERATION_UNAVAILABLE("10"),
    /** The resource not found. */
    RESOURCE_NOT_FOUND("11"),
    /** The resource exists. */
    RESOURCE_EXISTS("12"),
    /** The account exists. */
    ACCOUNT_EXISTS("13"),
    /** The unknown. */
    UNKNOWN("14"),
    /** The resource unavailable. */
    RESOURCE_UNAVAILABLE("15"),
    /** The validation. */
    VALIDATION("16"),
    /** The authentication policy. */
    AUTHENTICATION_POLICY("17"),
    /** The throttling. */
    THROTTLING("18"),
    /** The provisioned throughput exceeded. */
    PROVISIONED_THROUGHPUT_EXCEEDED("19"),
    /** The role not found. */
    ROLE_NOT_FOUND("20"),
    /** The account blocked. */
    ACCOUNT_BLOCKED("21");

    /** The Constant DEFAULT. */
    private final static ErrorTypeEnum DEFAULT = UNKNOWN;

    /** The code. */
    private final String code;

    /**
     * Instantiates a new error type enum.
     * @param code
     *            the code
     */
    private ErrorTypeEnum(final String code) {
        this.code = code;
    }

    /**
     * Gets the code.
     * @return the code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * From code type.
     * @param codeType
     *            the code type
     * @return the error type enum
     */
    public static ErrorTypeEnum fromCodeType(final String codeType) {

        if (codeType == null) {
            throw new IllegalArgumentException("codeType argument is required!");
        }

        for (final ErrorTypeEnum currentErrorType : values()) {
            if (codeType.equals(currentErrorType.code)) {
                return currentErrorType;
            }
        }

        return DEFAULT;

    }

}
