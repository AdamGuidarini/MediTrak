package projects.medicationtracker.Models;

public class FilterField<T> {
    public enum FilterOptions {
        EQUALS,
        LESS_THAN,
        GREATER_THAN
    }

    private String field;
    private T value;
    private FilterOptions option;

    public FilterField() {
        field = "";
        value = null;
        option = null;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public FilterOptions getOption() {
        return option;
    }

    public void setOption(FilterOptions option) {
        this.option = option;
    }
}
