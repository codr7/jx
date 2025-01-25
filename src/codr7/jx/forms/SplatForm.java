package codr7.jx.forms;

import codr7.jx.*;
import codr7.jx.libs.CoreLib;
import codr7.jx.libs.core.traits.SeqTrait;
import codr7.jx.ops.CreateIter;
import codr7.jx.ops.Trace;

public class SplatForm extends BaseForm {
    public final IForm target;

    public SplatForm(final IForm target, final Loc loc) {
        super(loc);
        this.target = target;
    }

    @Override public String argId(final VM vm, final Loc loc) {
        return target.argId(vm, loc) + '*';
    }

    @Override
    public void emit(final VM vm, final int rResult) {
        final var v = value(vm);

        if (v == null) {
            target.emit(vm, rResult);
            vm.emit(new CreateIter(rResult, loc()));
        } else {
            v.emit(vm, rResult, loc());
        }
    }

    @Override
    public boolean eq(final IForm other) {
        if (other instanceof SplatForm f) {
            return target.eq(f.target);
        }

        return false;
    }

    @Override
    public String dump(final VM vm) {
        return target.dump(vm) + '*';
    }

    @Override
    public IValue rawValue(final VM vm) {
        final var v = target.value(vm);

        if (v != null && v.type() instanceof SeqTrait st) {
            final var it = st.iter(vm, v, loc());
            return new Value<>(CoreLib.iterType, it);
        }

        return null;
    }
}