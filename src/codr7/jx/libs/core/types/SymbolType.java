package codr7.jx.libs.core.types;

import codr7.jx.BaseType;
import codr7.jx.IValue;
import codr7.jx.VM;

public class SymbolType extends BaseType<String> {
    public SymbolType(final String id) {
        super(id);
    }
    @Override public String dump(final VM vm, final IValue value) { return "'" + value.cast(this); }
    @Override public boolean toBit(final VM vm, final IValue value) {
        return !value.cast(this).equals("_");
    }
    @Override public String toString(final VM vm, final IValue value) {
        return value.cast(this);
    }
}