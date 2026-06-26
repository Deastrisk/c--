import java.util.HashMap;
import java.util.Map;

public class Environment {

    Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this(null);
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    private Environment ancestor(int hops) {
        Environment env = this;
        for (int i = 0; i < hops; i++) {
            env = env.enclosing;
        }
        return env;
    }

    public Object lookup(String name, int hops) {
        return ancestor(hops).values.get(name);
    }

    public void assign(String name, Object value, int hops) {
        ancestor(hops).values.put(name, value);
    }

    public Object getGlobal(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }

        if (enclosing != null) {
            return enclosing.getGlobal(name);
        }

        throw new RuntimeException("Used undefined variable '" + name + "'.");
    }

    public void assignGlobal(String name, Object value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assignGlobal(name, value);
            return;
        }

        throw new RuntimeException("Assigned to undefined variable '" + name + "'.");
    }
}
