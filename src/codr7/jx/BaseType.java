package codr7.jx;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseType<T> implements IDataType<T> {
    public final String id;
    public final Map<IType, Integer> parentTypes = new HashMap<>();

    public BaseType(final String id) { this.id = id; }

    public BaseType(final String id, final IType[] parentTypes) {
        this(id);
        addParentType(this, 1);
        for (final var pt: parentTypes) { pt.addParentTypes(this); }
    }

    @Override final public void addParentType(final IType type, final int weight) {
        parentTypes.compute(type, (k, w) -> (w == null) ? weight : weight + w);
    }

    @Override final public void addParentTypes(final IType childType) {
        for (final var pe: parentTypes.entrySet()) {
            childType.addParentType(pe.getKey(), pe.getValue());
        }
    }

    @Override public String dump(VM vm, IValue value) { return value.cast(this).toString(); }

    @Override public boolean eq(IValue left, IValue right) {
        return left.cast(this).equals(right.cast(this));
    }

    @Override final public String id() { return id; }
    @Override final public boolean is(IValue left, IValue right) { return left.cast(this) == right.cast(this); }
}

