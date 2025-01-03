package codr7.jx.ops;

import codr7.jx.*;

import java.util.Set;

public record Next(int rIter, int rItem, Label bodyEnd, Loc loc) implements Op {
    public String dump(final VM vm) {
        return "NEXT rIter: " + rIter + " (" + vm.registers.get(rIter).dump(vm) + ") " +
                "rItem: " + rItem + " (" + ((rItem == -1) ? "n/a" : vm.registers.get(rItem).dump(vm)) + ") " +
                "bodyEnd: " + bodyEnd;
    }

    public void io(final VM vm, final Set<Integer> read, final Set<Integer> write) {
        read.add(rIter);
        write.add(rItem);
    }
}
