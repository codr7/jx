package codr7.jx;

import codr7.jx.ops.*;

public record Op(OpCode code, Record data, Location location) {
    public String toString(final VM vm) {
        return code.name() + switch (data) {
            case Branch op -> " " + op.toString(vm);
            case Call op -> " " + op.toString(vm);
            case Copy op -> " " + op.toString(vm);
            case Left op -> " " + op.toString(vm);
            case Put op -> " " + op.toString(vm);
            case Right op -> " " + op.toString(vm);
            case SetPath op -> " " + op.toString(vm);
            case Zip op -> " " + op.toString(vm);
            case null -> "";
            default -> throw new RuntimeException("Invalid op: " + data);
        };
    }
}