package codr7.jx.libs;

import codr7.jx.Arg;
import codr7.jx.Lib;
import codr7.jx.Value;
import codr7.jx.libs.gui.types.FrameType;

import javax.swing.*;
import java.awt.*;

public class GUI extends Lib {
    public static final FrameType frameType = new FrameType("Frame");

    public GUI() {
        super("gui");

        bind(frameType);

        bindMethod("new-frame",
                new Arg[]{new Arg("title"), new Arg("width"), new Arg("height")}, null,
                (vm, args, rResult, location) -> {
                    final var title = args[0].cast(Core.stringType);
                    final var f = new JFrame(title);
                    final var width = args[1].cast(Core.intType).intValue();
                    final var height = args[2].cast(Core.intType).intValue();
                    f.setPreferredSize(new Dimension(width, height));
                    f.setLocationRelativeTo(null);
                    vm.registers.set(rResult, new Value<>(frameType, f));
                });

        bindMethod("show", new Arg[]{new Arg("widgets*")}, null,
                (vm, args, rResult, location) -> {
                    for (final var a: args) { a.cast(frameType).setVisible(true); }
                });
    }
}