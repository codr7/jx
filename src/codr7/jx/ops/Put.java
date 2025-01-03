package codr7.jx.ops;

import codr7.jx.*;

import java.util.Set;

public record Put(int rTarget, IValue value, Loc loc) implements Op {
    @Override public String dump(final VM vm) {
        return "PUT rTarget: " + rTarget + " (" + vm.registers.get(rTarget).dump(vm) + ") " +
                "value: " + value.dump(vm);
    }

    @Override public void io(final VM vm, final Set<Integer> read, final Set<Integer> write) {
        write.add(rTarget);
    }
}
