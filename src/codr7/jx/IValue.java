package codr7.jx;

public sealed interface IValue permits Value {
    <U> U cast(IDataType<U> type);
    String dump(VM vm);
    IValue dup(VM vm);
    void emit(VM vm, int rResult, Loc loc);
    boolean eq(IValue other);
    boolean is(IValue other);
    boolean toBit(VM vm);
    String toString(VM vm);
    IType type();
}
