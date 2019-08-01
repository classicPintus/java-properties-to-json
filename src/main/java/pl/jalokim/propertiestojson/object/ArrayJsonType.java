package pl.jalokim.propertiestojson.object;


import pl.jalokim.propertiestojson.PropertyArrayHelper;
import pl.jalokim.propertiestojson.path.PathMetadata;
import pl.jalokim.propertiestojson.resolvers.PrimitiveJsonTypesResolver;
import pl.jalokim.propertiestojson.util.exception.CannotOverrideFieldException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static pl.jalokim.propertiestojson.Constants.ARRAY_END_SIGN;
import static pl.jalokim.propertiestojson.Constants.ARRAY_START_SIGN;
import static pl.jalokim.propertiestojson.Constants.EMPTY_STRING;
import static pl.jalokim.propertiestojson.Constants.NEW_LINE_SIGN;
import static pl.jalokim.propertiestojson.object.JsonNullReferenceType.NULL_OBJECT;
import static pl.jalokim.propertiestojson.object.MergableObject.mergeObjectIfPossible;
import static pl.jalokim.propertiestojson.util.ListUtil.getLastIndex;
import static pl.jalokim.propertiestojson.util.ListUtil.isLastIndex;


public class ArrayJsonType extends AbstractJsonType implements MergableObject<ArrayJsonType> {

    public static final int INIT_SIZE = 100;
    private AbstractJsonType[] elements = new AbstractJsonType[INIT_SIZE];
    private int maxIndex = -1;

    public ArrayJsonType() {
    }

    public ArrayJsonType(PrimitiveJsonTypesResolver primitiveJsonTypesResolver, Collection<?> elements, PathMetadata currentPathMetadata) {
        Iterator<?> iterator = elements.iterator();
        int index = 0;
        while(iterator.hasNext()) {
            Object element = iterator.next();
            addElement(index, primitiveJsonTypesResolver.resolvePrimitiveTypeAndReturn(element), currentPathMetadata);
            index++;
        }
    }

    public void addElement(int index, AbstractJsonType elementToAdd, PathMetadata currentPathMetadata) {
        if(maxIndex < index) {
            maxIndex = index;
        }
        rewriteArrayWhenIsFull(index);
        AbstractJsonType oldObject = elements[index];

        if(oldObject != null) {
            if(oldObject instanceof MergableObject && elementToAdd instanceof MergableObject) {
                mergeObjectIfPossible(oldObject, elementToAdd, currentPathMetadata);
            } else {
                // TODO test this
                 throw new CannotOverrideFieldException(currentPathMetadata.getOriginalFieldName(), oldObject, currentPathMetadata.getOriginalPropertyKey());
            }
        } else {
            elements[index] = elementToAdd;
        }
    }

    public void addElement(PropertyArrayHelper propertyArrayHelper, AbstractJsonType elementToAdd, PathMetadata currentPathMetadata) {
        List<Integer> indexes = propertyArrayHelper.getDimensionalIndexes();
        int size = propertyArrayHelper.getDimensionalIndexes().size();
        ArrayJsonType currentArray = this;
        for(int index = 0; index < size; index++) {
            if(isLastIndex(propertyArrayHelper.getDimensionalIndexes(), index)) {
                currentArray.addElement(indexes.get(index), elementToAdd, currentPathMetadata);
            } else {
                currentArray = createOrGetNextDimensionOfArray(currentArray, indexes, index, currentPathMetadata);
            }
        }
    }

    public static ArrayJsonType createOrGetNextDimensionOfArray(ArrayJsonType currentArray, List<Integer> indexes, int index, PathMetadata currentPathMetadata) {
        if(currentArray.existElementByGivenIndex(indexes.get(index))) {
            AbstractJsonType element = currentArray.getElement(indexes.get(index));
            if(element instanceof ArrayJsonType) {
                return (ArrayJsonType) element;
            } else {
                // TODO done it and test this one
                // expected type which is in (AbstractJsonType element)  at given array in given path...
                //throwException();
                List<Integer> currentIndexes = indexes.subList(0, index);
                throw new RuntimeException("current type " + element.getClass() + " with value: " + element
                                           + " at given array in given path " + currentIndexes);
            }
        } else {
            ArrayJsonType newArray = new ArrayJsonType();
            currentArray.addElement(indexes.get(index), newArray, currentPathMetadata);
            return newArray;
        }
    }

    public AbstractJsonType getElementByGivenDimIndexes(PathMetadata currentPathMetaData) {
        PropertyArrayHelper propertyArrayHelper = currentPathMetaData.getPropertyArrayHelper();
        List<Integer> indexes = propertyArrayHelper.getDimensionalIndexes();
        int size = propertyArrayHelper.getDimensionalIndexes().size();
        ArrayJsonType currentArray = this;
        for(int i = 0; i < size; i++) {
            if(isLastIndex(propertyArrayHelper.getDimensionalIndexes(), i)) {
                return currentArray.getElement(indexes.get(i));
            } else {
                AbstractJsonType element = currentArray.getElement(indexes.get(i));
                if(element == null) {
                    return null;
                }
                if(element instanceof ArrayJsonType) {
                    currentArray = (ArrayJsonType) element;
                } else {
                    // TODO done it and test this one
                    // expected type which is in (AbstractJsonType element)  at given array in given path...
                    //throwException();
                    List<Integer> currentIndexes = indexes.subList(0, i);
                    throw new RuntimeException("expected type " + element.getClass() + " at given array in given path: " + currentPathMetaData.getCurrentFullPath() + " current indexes: " + currentIndexes);
                }
            }
        }
        throw new UnsupportedOperationException("cannot return expected object for " + currentPathMetaData.getCurrentFullPath() + " " + currentPathMetaData.getPropertyArrayHelper().getDimensionalIndexes());
    }

    public boolean existElementByGivenIndex(int index) {
        return getElement(index) != null;
    }

    private void rewriteArrayWhenIsFull(int index) {
        if(indexHigherThanArraySize(index)) {
            int predictedNewSize = elements.length + INIT_SIZE;
            int newSize = predictedNewSize > index ? predictedNewSize : index + 1;
            AbstractJsonType[] elementsTemp = new AbstractJsonType[newSize];
            System.arraycopy(elements, 0, elementsTemp, 0, elements.length);
            elements = elementsTemp;
        }
    }

    private boolean indexHigherThanArraySize(int index) {
        return index > getLastIndex(elements);
    }

    public AbstractJsonType getElement(int index) {
        rewriteArrayWhenIsFull(index);
        return elements[index];
    }

    @Override
    public String toStringJson() {
        StringBuilder result = new StringBuilder().append(ARRAY_START_SIGN);
        int index = 0;
        List<AbstractJsonType> elementsAsList = convertToListWithoutRealNull();
        int lastIndex = getLastIndex(elementsAsList);
        for(AbstractJsonType element : elementsAsList) {
            String lastSign = index == lastIndex ? EMPTY_STRING : NEW_LINE_SIGN;
            result.append(element.toStringJson())
                  .append(lastSign);
            index++;
        }
        return result.append(ARRAY_END_SIGN).toString();
    }

    public List<AbstractJsonType> convertToListWithoutRealNull() {
        List<AbstractJsonType> elementsList = new ArrayList<>();

        for(int i = 0; i < maxIndex + 1; i++) {
            AbstractJsonType element = elements[i];
            if(element != null) {
                elementsList.add(element);
            } else {
                elementsList.add(NULL_OBJECT);
            }
        }
        return elementsList;
    }

    private List<AbstractJsonType> convertToListWithRealNull() {
        List<AbstractJsonType> elementsList = new ArrayList<>();
        for(int i = 0; i < maxIndex + 1; i++) {
            AbstractJsonType element = elements[i];
                elementsList.add(element);
        }
        return elementsList;
    }

    @Override
    public void merge(ArrayJsonType mergeWith, PathMetadata currentPathMetadata) {
        int index = 0;
        for(AbstractJsonType abstractJsonType : mergeWith.convertToListWithRealNull()) {
            addElement(index, abstractJsonType, currentPathMetadata);
            index++;
        }
    }
}
