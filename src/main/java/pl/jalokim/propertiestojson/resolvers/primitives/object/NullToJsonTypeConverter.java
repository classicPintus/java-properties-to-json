package pl.jalokim.propertiestojson.resolvers.primitives.object;

import pl.jalokim.propertiestojson.object.AbstractJsonType;
import pl.jalokim.propertiestojson.object.JsonNullReferenceType;
import pl.jalokim.propertiestojson.resolvers.PrimitiveJsonTypesResolver;

import java.util.Optional;

public class NullToJsonTypeConverter extends AbstractObjectToJsonTypeConverter<JsonNullReferenceType> {

    public static final NullToJsonTypeConverter NULL_TO_JSON_RESOLVER = new NullToJsonTypeConverter();

    @Override
    public Optional<AbstractJsonType> convertToJsonTypeOrEmpty(PrimitiveJsonTypesResolver primitiveJsonTypesResolver,
                                                               JsonNullReferenceType convertedValue,
                                                               String propertyKey) {
        return Optional.of(convertedValue);
    }
}
