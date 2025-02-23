# eli-java

## Introduction
This project implements the [eli](https://github.com/codr7/eli) language in Java.

## REPL
Launching the `.jar`-file without arguments starts a REPL.

```
$ java -jar eli.jar
eli v3

 1 (say 'hello)
 2
hello
```

## Performance

```
$ java -jar eli.jar ../eli/benchmarks/run.eli
```
```
fact PT0.852045564S
fib PT0.761190763S
```