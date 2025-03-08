package codr7.eli;

import codr7.eli.errors.EvalError;
import codr7.eli.libs.*;
import codr7.eli.libs.core.traits.IterableTrait;
import codr7.eli.ops.Iter;
import codr7.eli.ops.*;
import codr7.eli.readers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public final class VM {
    public final static int VERSION = 8;

    public boolean debug = false;

    public final CoreLib coreLib = new CoreLib();
    public final Lib homeLib = new Lib("home", null);
    public Lib currentLib = homeLib;

    public final List<Reader> prefixReaders = new ArrayList<>();
    public final List<Reader> suffixReaders = new ArrayList<>();

    public final ArrayList<Op> ops = new ArrayList<>();
    public final ArrayList<IValue> registers = new ArrayList<>();
    public Path path = Paths.get("");
    public int pc = 0;
    public final int rNull;

    private final List<Call> calls = new ArrayList<>();
    private Op.Code[] opCodes = new Op.Code[0];
    private Object[] opValues = new Object[0];

    public VM() {
        prefixReaders.add(WhitespaceReader.instance);
        prefixReaders.add(CallReader.instance);
        prefixReaders.add(CharReader.instance);
        prefixReaders.add(DecimalReader.instance);
        prefixReaders.add(IntReader.instance);
        prefixReaders.add(IdReader.instance);
        prefixReaders.add(CountReader.instance);
        prefixReaders.add(ListReader.instance);
        prefixReaders.add(MapReader.instance);
        prefixReaders.add(QuoteReader.instance);
        prefixReaders.add(StringReader.instance);
        prefixReaders.add(UnquoteReader.instance);

        suffixReaders.add(PairReader.instance);
        suffixReaders.add(SplatReader.instance);

        rNull = alloc(1);

        homeLib.bind(new BitLib());
        homeLib.bind(new CoreLib());
        homeLib.bind(new GUILib());
        homeLib.bind(new IntLib());
        homeLib.bind(new IterLib());
        homeLib.bind(new ListLib());
        homeLib.bind(new SeqLib());
        homeLib.bind(new StringLib());

        initLibs();
    }

    public int alloc(final int n) {
        final var result = registers.size();

        for (var i = 0; i < n; i++) {
            registers.add(CoreLib.NIL);
        }

        return result;
    }

    public void beginCall(final Method target, final int returnPc, final int rResult, final Loc loc) {
        calls.add(new Call(target, returnPc, rResult, loc));
    }

    public void doLib(final Lib lib, final DoLibBody body) {
        final var prevLib = currentLib;
        currentLib = (lib == null) ? new Lib(prevLib.id, prevLib) : lib;

        try {
            body.call();
        } finally {
            currentLib = prevLib;
        }
    }

    public void doLibId(final String id, final DoLibBody body) {
        final var v = currentLib.find(id);
        Lib lib;

        if (v == null) {
            lib = new Lib(id, currentLib);
            currentLib.bind(id, new Value<>(CoreLib.Lib, lib));
        } else {
            lib = v.cast(CoreLib.Lib);
        }

        doLib(lib, body);
    }

    public void dumpOps(final int startPc) {
        for (var i = startPc; i < ops.size(); i++) {
            System.out.printf("% 4d %s\n", i, ops.get(i).dump(this));
        }
    }

    public int emit(final Op op) {
        final var startPc = emitPc();
        ops.add(op);
        return startPc;
    }

    public int emit(final Deque<IForm> in, final int rResult) {
        final var startPc = emitPc();

        for (final var f : in) {
            f.emit(this, rResult);
        }

        return startPc;
    }

    public int emit(final IForm[] in, final int rResult) {
        final var startPc = emitPc();

        for (final var f : in) {
            f.emit(this, rResult);
        }

        return startPc;
    }

    public int emitPc() {
        return ops.size();
    }

    public Call endCall() {
        return calls.removeLast();
    }

    public void eval(final int fromPc) {
        eval(fromPc, emitPc());
    }

    public void eval(final int fromPc, final int toPc) {
        Op prevOp;

        if (toPc == emitPc()) {
            emit(new Stop());
            prevOp = new Nop();
        } else {
            prevOp = ops.get(toPc);
            ops.set(toPc, new Stop());
            opCodes[toPc] = Op.Code.Stop;
        }

        final var prevPc = pc;
        pc = fromPc;

        try {
            eval();
        } finally {
            ops.set(toPc, prevOp);
            opCodes[toPc] = prevOp.code();
            pc = prevPc;
        }
    }

    public void eval(final String in, final int rResult, final Loc loc) {
        final var skip = new Label();
        emit(new Goto(skip));
        final var startPc = emitPc();
        emit(read(in, loc), rResult);
        skip.pc = emitPc();
        eval(startPc);
    }

    public void eval(final String in, final Loc loc) {
        eval(in, rNull, loc);
    }

    private void eval() {
        freezeOps();

        for (; ; ) {
            //System.out.println(pc + " " + ops.get(pc).dump(this));

            switch (opCodes[pc]) {
                case AddItem: {
                    final var op = (AddItem) opValues[pc];
                    final var t = registers.get(op.rTarget()).cast(CoreLib.List);
                    final var v = registers.get(op.rItem());
                    System.out.println(v.dump(this));
                    Value.expand(this, v, t, op.loc());
                    pc++;
                    break;
                }
                case Bench: {
                    final var op = (Bench) opValues[pc];
                    final var started = System.nanoTime();
                    final var startPc = pc + 1;

                    for (var i = 0; i < op.reps(); i++) {
                        eval(startPc, op.bodyEnd().pc);
                    }

                    final var elapsed = Duration.ofNanos(System.nanoTime() - started);
                    registers.set(op.rResult(), new Value<>(CoreLib.Time, elapsed));
                    pc = op.bodyEnd().pc;
                    break;
                }
                case Branch: {
                    final var op = (Branch) opValues[pc];
                    pc = registers.get(op.rCondition()).toBit(this) ? pc + 1 : op.elseStart().pc;
                    break;
                }
                case CallRegister: {
                    final var op = (CallRegister) opValues[pc];
                    var t = registers.get(op.rTarget());

                    if (t.type() == CoreLib.Binding) {
                        t = registers.get(t.cast(CoreLib.Binding).rValue());
                    }

                    pc++;
                    t.type().cast(CoreLib.Callable, op.loc()).call(this, t, op.rArguments(), op.arity(), op.rResult(), false, op.loc());
                    break;
                }
                case CallValue: {
                    final var op = (CallValue) opValues[pc];
                    var t = op.target();

                    if (t.type() == CoreLib.Binding) {
                        t = registers.get(t.cast(CoreLib.Binding).rValue());
                    }

                    pc++;
                    t.type().cast(CoreLib.Callable, op.loc()).call(this, t, op.rArgs(), op.arity(), op.rResult(), false, op.loc());
                    break;
                }
                case Check: {
                    final var op = (Check) opValues[pc];
                    final var expected = registers.get(op.rValues());
                    final var actual = registers.get(op.rValues() + 1);

                    if (!expected.eq(actual)) {
                        throw new EvalError("Check failed; expected " +
                                expected.dump(this) + ", actual: " + actual.dump(this),
                                op.loc());
                    }

                    pc++;
                    break;
                }
                case Copy: {
                    final var op = (Copy) opValues[pc];
                    registers.set(op.rTo(), registers.get(op.rFrom()));
                    pc++;
                    break;
                }
                case Goto:
                    pc = ((Label) opValues[pc]).pc;
                    break;
                case Inc: {
                    final var op = (Inc) opValues[pc];
                    final var v = registers.get(op.rTarget()).cast(CoreLib.Int);
                    registers.set(op.rTarget(), new Value<>(CoreLib.Int, v + op.delta()));
                    pc++;
                    break;
                }
                case Iter: {
                    final var rt = (Integer) opValues[pc];
                    final var t = registers.get(rt);
                    final var it = ((IterableTrait) t.type()).iter(this, t);
                    registers.set(rt, new Value<>(CoreLib.Iter, it));
                    pc++;
                    break;
                }
                case Left: {
                    final var op = (Left) opValues[pc];
                    registers.set(op.rResult(), registers.get(op.rPair()).cast(CoreLib.Pair).left());
                    pc++;
                    break;
                }
                case MakeList: {
                    registers.set((Integer) opValues[pc], new Value<>(CoreLib.List, new ArrayList<>()));
                    pc++;
                    break;
                }
                case Next: {
                    final var op = (Next) opValues[pc];
                    final var iter = registers.get(op.rIter()).cast(CoreLib.Iter);
                    pc = (iter.next(this, op.rItem(), op.loc())) ? pc + 1 : op.bodyEnd().pc;
                    break;
                }
                case Nop:
                    pc++;
                    break;
                case Put: {
                    final var op = (Put) opValues[pc];
                    registers.set(op.rTarget(), op.value().dup(this));
                    pc++;
                    break;
                }
                case Return: {
                    final var c = endCall();

                    if (c.rResult() != c.target().rResult()) {
                        registers.set(c.rResult(), registers.get(c.target().rResult()));
                    }

                    pc = c.returnPc();
                    break;
                }
                case Right: {
                    final var op = (Right) opValues[pc];
                    registers.set(op.rResult(), registers.get(op.rPair()).cast(CoreLib.Pair).right());
                    pc++;
                    break;
                }
                case SetPath:
                    path = (Path) opValues[pc];
                    pc++;
                    break;
                case Splat: {
                    final var rt = (Integer) opValues[pc];
                    final var t = registers.get(rt);
                    final var it = ((IterableTrait) t.type()).iter(this, t);
                    registers.set(rt, new Value<>(CoreLib.Splat, it));
                    pc++;
                    break;
                }
                case Stop:
                    pc++;
                    return;
                case Trace:
                    System.out.println((String) opValues[pc]);
                    pc++;
                    break;
                case Unzip: {
                    final var op = (Unzip) opValues[pc];
                    final var p = registers.get(op.rPair()).cast(CoreLib.Pair);
                    registers.set(op.rLeft(), p.left());
                    registers.set(op.rRight(), p.right());
                    pc++;
                    break;
                }
                case Zip: {
                    final var op = (Zip) opValues[pc];
                    final var l = registers.get(op.rLeft());
                    final var r = registers.get(op.rRight());
                    registers.set(op.rResult(), new Value<>(CoreLib.Pair, new Pair(l, r)));
                    pc++;
                    break;
                }
            }
        }
    }

    public void load(final Path path, final int rResult) {
        final var prevPath = this.path;
        final var p = prevPath.resolve(path);
        final var location = new Loc(p.toString());
        this.path = p.getParent();

        try {
            final Deque<IForm> out;

            try {
                out = read(Files.readString(p), location);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            emit(new SetPath(p.getParent()));
            emit(out, rResult);
            emit(new SetPath(prevPath));
        } finally {
            this.path = prevPath;
        }
    }

    public boolean read(final Input in, final Deque<IForm> out, final Loc loc) {
        final var result = prefixReaders.stream().anyMatch(r -> r.read(this, in, out, loc));
        var _ = suffixReaders.stream().anyMatch(r -> r.read(this, in, out, loc));
        return result;
    }

    public Deque<IForm> read(final String in, final Loc loc) {
        final var out = new ArrayDeque<IForm>();
        final var _in = new Input(in);
        while (read(_in, out, loc)) ;
        return out;
    }

    private void freezeOps() {
        final var n = opCodes.length;
        final var m = ops.size();

        if (n != m) {
            opCodes = Arrays.copyOf(opCodes, m);
            opValues = Arrays.copyOf(opValues, m);

            for (var i = 0; i < m; i++) {
                final var o = ops.get(i);
                opCodes[i] = o.code();

                opValues[i] = switch (o) {
                    case AddItem op -> op;
                    case Bench op -> op;
                    case Branch op -> op;
                    case CallRegister op -> op;
                    case CallValue op -> op;
                    case Check op -> op;
                    case Copy op -> op;
                    case Goto op -> op.target();
                    case Inc op -> op;
                    case Iter op -> op.rTarget();
                    case Left op -> op;
                    case MakeList op -> op.rTarget();
                    case Next op -> op;
                    case Put op -> op;
                    case Right op -> op;
                    case SetPath op -> op.path();
                    case Splat op -> op.rTarget();
                    case Trace op -> op.text();
                    case Unzip op -> op;
                    case Zip op -> op;
                    default -> null;
                };
            }
        }
    }

    private void initLibs() {
        for (final var v : homeLib.bindings.values()) {
            if (v.type() == CoreLib.Lib) {
                v.cast(CoreLib.Lib).tryInit(this);
            }
        }
    }
    public interface DoLibBody {
        void call();
    }
}
