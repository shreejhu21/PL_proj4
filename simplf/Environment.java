package simplf;

class Environment {
    private final Environment enclosing;
    private final AssocList assocList;

    Environment() {
        this.enclosing = null;
        this.assocList = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
        this.assocList = null;
    }

    Environment(AssocList assocList, Environment enclosing) {
        this.assocList = assocList;
        this.enclosing = enclosing;
    }

    Environment define(Token varToken, String name, Object value) {
        AssocList newList = new AssocList(name, value, this.assocList);
        return new Environment(newList, this.enclosing);
    }

    void assign(Token name, Object value) {
        for (AssocList list = this.assocList; list != null; list = list.next) {
            if (list.name.equals(name.lexeme)) {
                list.value = value;
                return;
            }
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object get(Token name) {
        for (AssocList list = this.assocList; list != null; list = list.next) {
            if (list.name.equals(name.lexeme)) {
                return list.value;
            }
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
