package mobi.tattu.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper de enums que implementa el metodo toString tomando el valor del strings.xml.
 * Provee metodos estaticos para wrappear todos los valores de un Enum dado o solo algunos.
 */
public class EnumWrapper<T extends Enum> {

    public final T value;

    public EnumWrapper(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ResourceUtils.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumWrapper<?> that = (EnumWrapper<?>) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public static <R extends Enum<R>> List<EnumWrapper<R>> values(Class<R> type) {
        return wrap(type.getEnumConstants());
    }

    public static <R extends Enum<R>> List<EnumWrapper<R>> wrap(R... values) {
        List<EnumWrapper<R>> list = new ArrayList<>(values.length);
        for (Enum<R> e : values) {
            list.add(new EnumWrapper(e));
        }
        return list;
    }

}