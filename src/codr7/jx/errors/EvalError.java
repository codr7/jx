package codr7.jx.errors;

import codr7.jx.Error;
import codr7.jx.Location;

public class EvalError extends Error {
    public EvalError(final String message, final Location location) { super(message, location); }
}