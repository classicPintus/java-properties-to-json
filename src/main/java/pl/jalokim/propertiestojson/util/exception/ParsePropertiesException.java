package pl.jalokim.propertiestojson.util.exception;

import pl.jalokim.propertiestojson.resolvers.primitives.StringJsonTypeResolver;

public class ParsePropertiesException extends RuntimeException {

    private static final String REASON_DESCRIBE = " because already this field has value: %s";
    private static final String FIRST_PART_OF_MESSAGE = "Current field: '%s' is already considered as ";
    private static final String WRONG_PROPERTY_KEY = " \n(error for given wrong property key: '%s')";
    private static final String FIRST_PART_OF_MESSAGE_ARRAY = "Current array element: '%s[%d]' is already considered as ";
    public static final String EXPECTED_OBJECT_JSON_TYPE = FIRST_PART_OF_MESSAGE + "JSON object" + REASON_DESCRIBE + WRONG_PROPERTY_KEY;
    public static final String EXPECTED_PRIMITIVE_JSON_TYPE = FIRST_PART_OF_MESSAGE + "primitive type" + REASON_DESCRIBE + WRONG_PROPERTY_KEY;
    public static final String EXPECTED_ARRAY_JSON_TYPE = FIRST_PART_OF_MESSAGE + "array JSON type" + REASON_DESCRIBE + WRONG_PROPERTY_KEY;
    public static final String EXPECTED_ELEMENT_ARRAY_PRIMITIVE_TYPES = FIRST_PART_OF_MESSAGE_ARRAY + "primitive JSON Element not as JSON object" + REASON_DESCRIBE + WRONG_PROPERTY_KEY;
    public static final String EXPECTED_ELEMENT_ARRAY_JSON_OBJECT_TYPES = FIRST_PART_OF_MESSAGE_ARRAY + "object JSON Element not as primitive" + REASON_DESCRIBE + WRONG_PROPERTY_KEY;
    public static final String STRING_RESOLVER_AS_NOT_LAST = "Added some type resolver after " + StringJsonTypeResolver.class.getCanonicalName() +
                                                             ". This type resolver always should be last when is in configuration of resolvers";
    public static final String PROPERTY_KEY_NEEDS_TO_BE_STRING_TYPE = "Unsupported property key type: %s for key: %s, Property key needs to be a string type";

    public ParsePropertiesException(String message) {
        super(message);
    }
}
