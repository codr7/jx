package codr7.jx.ops;

import codr7.jx.Loc;
import codr7.jx.Op;
import codr7.jx.VM;

import java.util.Set;

public record Dec(int rTarget, int rDelta, Loc loc) implements Op {
    public String dump(final VM vm) {
        return "Dec rTarget: " + rTarget + " (" + vm.registers.get(rTarget).dump(vm) + ") " +
                "rDelta: " + rDelta + " (" + vm.registers.get(rDelta).dump(vm) + ")";
    }

    public void io(final VM vm, final Set<Integer> read, final Set<Integer> write) {
        read.add(rTarget);
        write.add(rTarget);
        if (rDelta != -1) { read.add(rDelta); }
    }
}
