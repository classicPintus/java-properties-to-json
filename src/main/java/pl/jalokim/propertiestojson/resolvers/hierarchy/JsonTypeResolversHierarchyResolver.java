package pl.jalokim.propertiestojson.resolvers.hierarchy;

import pl.jalokim.propertiestojson.object.AbstractJsonType;
import pl.jalokim.propertiestojson.resolvers.PrimitiveJsonTypesResolver;
import pl.jalokim.propertiestojson.resolvers.primitives.PrimitiveJsonTypeResolver;
import pl.jalokim.propertiestojson.resolvers.primitives.adapter.PrimitiveJsonTypeResolverToNewApiAdapter;
import pl.jalokim.propertiestojson.resolvers.primitives.object.ObjectToJsonTypeResolver;
import pl.jalokim.propertiestojson.util.exception.ParsePropertiesException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static pl.jalokim.propertiestojson.object.JsonNullReferenceType.NULL_OBJECT;
import static pl.jalokim.propertiestojson.util.exception.ParsePropertiesException.CANNOT_FIND_JSON_TYPE_OBJ;
import static pl.jalokim.propertiestojson.util.exception.ParsePropertiesException.CANNOT_FIND_TYPE_RESOLVER_MSG;

/**
 * It looks for sufficient resolver, firstly will looks for exactly match class type provided by method {@link PrimitiveJsonTypeResolver#getClassesWhichCanResolve()}
 * if not then will looks for closets parent class or parent interface.
 * If will find resolver for parent class or parent interface at the same level, then will get parent super class as first.
 * If will find only closets super interfaces (at the same level) then will throw exception...
 */
public class JsonTypeResolversHierarchyResolver {

    private final Map<Class<?>, List<ObjectToJsonTypeResolver<?>>> resolversByType = new HashMap<>();
    private final HierarchyClassResolver hierarchyClassResolver;

    public JsonTypeResolversHierarchyResolver(List<ObjectToJsonTypeResolver> resolvers) {
        for(ObjectToJsonTypeResolver<?> resolver : resolvers) {
            for(Class<?> canResolveType : resolver.getClassesWhichCanResolve()) {
                List<ObjectToJsonTypeResolver<?>> resolversByClass = resolversByType.get(canResolveType);
                if(resolversByClass == null) {
                    List<ObjectToJsonTypeResolver<?>> newResolvers = new ArrayList<>();
                    newResolvers.add(resolver);
                    resolversByType.put(canResolveType, newResolvers);
                } else {
                    resolversByClass.add(resolver);
                }
            }
        }
        List<Class<?>> typesWhichCanResolve = new ArrayList<>();
        for(ObjectToJsonTypeResolver<?> resolver : resolvers) {
            typesWhichCanResolve.addAll(resolver.getClassesWhichCanResolve());
        }
        hierarchyClassResolver = new HierarchyClassResolver(typesWhichCanResolve);
    }

    public AbstractJsonType returnConcreteJsonTypeObject(PrimitiveJsonTypesResolver mainResolver,
                                                         Object instance,
                                                         String propertyKey) {
        Objects.nonNull(instance);
        Class<?> instanceClass = instance.getClass();
        List<ObjectToJsonTypeResolver<?>> resolvers = resolversByType.get(instanceClass);
        if(resolvers == null) {
            Class<?> typeWhichCanResolve = hierarchyClassResolver.searchResolverClass(instance);
            if(typeWhichCanResolve == null) {
                throw new ParsePropertiesException(format(CANNOT_FIND_TYPE_RESOLVER_MSG, instanceClass));
            }
            resolvers = resolversByType.get(typeWhichCanResolve);
        }

        if(!resolvers.isEmpty()) {
            if(instanceClass != String.class && resolvers.size() > 1 &&
               resolvers.stream().anyMatch(resolver -> resolver instanceof PrimitiveJsonTypeResolverToNewApiAdapter)) {
                List<Class<?>> resolversClasses = resolvers.stream()
                                                           .map(resolver -> {
                                                               if(resolver instanceof PrimitiveJsonTypeResolverToNewApiAdapter) {
                                                                   PrimitiveJsonTypeResolverToNewApiAdapter adapter = (PrimitiveJsonTypeResolverToNewApiAdapter) resolver;
                                                                   PrimitiveJsonTypeResolver oldImplementation = adapter.getOldImplementation();
                                                                   return oldImplementation.getClass();
                                                               }
                                                               return resolver.getClass();
                                                           }).collect(toList());
                throw new ParsePropertiesException("Found: " + new ArrayList<>(resolversClasses) + " for type" + instanceClass + " expected only one!");
            }

            for(ObjectToJsonTypeResolver<?> resolver : resolvers) {
                Optional<AbstractJsonType> abstractJsonType = resolver.returnOptionalJsonType(mainResolver, instance, propertyKey);
                if(abstractJsonType.isPresent()) {
                    return abstractJsonType.get();
                }
            }
        }

        throw new ParsePropertiesException(format(CANNOT_FIND_JSON_TYPE_OBJ, instanceClass, propertyKey, instance));
    }
}
