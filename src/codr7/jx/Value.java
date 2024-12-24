package codr7.jx;

public record Value<T>(IDataType<T> type, T data) implements IValue {
    public <U> U cast(IDataType<U> type) { return (U)data; }
    public IValue dup(final VM vm) { return type.dup(vm, this); }
    public String dump(final VM vm) { return type.dump(vm, this); }

    @Override
    public boolean equals(final Object other) {
        return other instanceof Value && type.equals(this, (Value<T>)other);
    }

    public void emit(final VM vm, final int rResult, final Loc loc) {
        type.emit(vm, this, rResult, loc);
    }

    public boolean toBit(final VM vm) { return type.toBit(vm, this); }
    public String toString(final VM vm) { return type.toString(vm, this); }
}