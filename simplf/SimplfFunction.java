package simplf;

import java.util.List;

class SimplfFunction implements SimplfCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    SimplfFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    public void setClosure(Environment environment) {
        // Not used
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment functionEnv = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++) {
            Token param = declaration.params.get(i);
            Object argVal = args.get(i);
            functionEnv = functionEnv.define(param, param.lexeme, argVal);
        }

        // Now we replace environment using a block scope execution
        // Save original environment (through interpreter.globals fallback)
        Environment previous = interpreter.globals;
        interpreter.globals = functionEnv;

        try {
            for (Stmt stmt : declaration.body) {
                stmt.accept(interpreter);
            }
        } finally {
            interpreter.globals = previous;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
