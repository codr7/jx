package codr7.jx.forms;

import codr7.jx.*;
import codr7.jx.errors.EmitError;
import codr7.jx.libs.core.types.CallTrait;
import codr7.jx.ops.Left;
import codr7.jx.ops.Right;

public class CallForm extends BaseForm {
    private final IForm[] body;

    public CallForm(IForm[] body, final Location location) {
        super(location);
        this.body = body;
    }

    public void emit(final VM vm, final int rResult) {
        var getLeft = false;
        var rightCount = 0;
        var tf = body[0];

        while (tf instanceof PairForm pf) {
            if (pf.left.isNil()) {
                tf = pf.right;
                rightCount++;
                continue;
            }

            if (pf.right.isNil()) {
                getLeft = true;
                tf = pf.left;
                break;
            }

            throw new EmitError("Invalid target: %s" + tf, location());
        }

        var t = tf.value(vm);
        if (t == null) { throw new EmitError("Unknown target: " + tf, location()); }
        if (t.type() instanceof CallTrait ct) { ct.emitCall(vm, t, body, rResult, location()); }
        if (getLeft) { vm.emit(Left.make(rResult, rResult, tf.location())); }
        else for (; rightCount > 0; rightCount--) { vm.emit(Right.make(rResult, rResult, tf.location())); }
    }

    public String toString(VM vm) {
        final var result = new StringBuilder();
        result.append('(');

        for (var i = 0; i < body.length; i++) {
            if (i > 0) { result.append(' '); }
            result.append(body[i].toString(vm));
        }

        result.append(')');
        return result.toString();
    }

    public IValue value(VM vm) { return null; }
}