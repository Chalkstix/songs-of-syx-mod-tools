package syxmodtools.detector;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** Minimal parser for {@code --flag value} style command-line arguments. */
final class ArgMap {

    private final Map<String, String> values = new HashMap<>();

    private ArgMap() {
    }

    static ArgMap parse(String[] args) {
        ArgMap map = new ArgMap();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                String value = (i + 1 < args.length) ? args[++i] : "";
                map.values.put(key, value);
            }
        }
        return map;
    }

    boolean has(String key) {
        return values.containsKey(key);
    }

    Path path(String key, Path fallback) {
        String value = values.get(key);
        return (value == null) ? fallback : Path.of(value);
    }
}
