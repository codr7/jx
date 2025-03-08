package codr7.eli.libs.core.types;

import codr7.eli.BaseType;
import codr7.eli.IValue;
import codr7.eli.VM;
import codr7.eli.libs.core.traits.CmpTrait;

public final class SymType extends BaseType<String> implements CmpTrait {
    public SymType(final String id) {
        super(id);
    }

    @Override
    public int cmp(final IValue lhs, final IValue rhs) {
        return lhs.cast(this).compareTo(rhs.cast(this));
    }

    @Override
    public String dump(final VM vm, final IValue value) {
        return "'" + value.cast(this);
    }

    @Override
    public boolean toBit(final VM vm, final IValue value) {
        return !value.cast(this).equals("_");
    }

    @Override
    public String toString(final VM vm, final IValue value) {
        return value.cast(this);
    }
}