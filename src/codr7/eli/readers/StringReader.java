package codr7.eli.readers;

import codr7.eli.*;
import codr7.eli.errors.ReadError;
import codr7.eli.forms.LiteralForm;
import codr7.eli.libs.CoreLib;

import java.util.Deque;

public class StringReader implements Reader {
    public static final StringReader instance = new StringReader();

    public boolean read(final VM vm, final Input in, final Deque<IForm> out, final Loc location) {
        if (in.peek() != '"') {
            return false;
        }
        final var loc = location.dup();
        location.update(in.pop());
        final var buffer = new StringBuilder();

        for (; ; ) {
            var c = in.peek();
            if (c == 0) {
                throw new ReadError("Unexpected end of string", location);
            }
            location.update(in.pop());
            if (c == '"') {
                break;
            }
            buffer.append(c);
        }

        out.addLast(new LiteralForm(new Value<>(CoreLib.String, buffer.toString()), loc));
        return true;
    }
}