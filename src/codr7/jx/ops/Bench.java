package codr7.jx.ops;

import codr7.jx.*;

import java.util.Set;

public record Bench(Label bodyEnd, int rResult, Loc loc) implements Op {
    public String dump(final VM vm) { return "BENCH bodyEnd: " + bodyEnd + " rResult: " + rResult; }
    public void io(final VM vm, final Set<Integer> read, final Set<Integer> write) {
        write.add(rResult);
    }
}