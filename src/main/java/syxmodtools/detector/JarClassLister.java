package syxmodtools.detector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Lists the fully-qualified class names contained in a jar file. */
public final class JarClassLister {

    private JarClassLister() {
    }

    /** Returns class names (dot-separated, no ".class" suffix) found in the jar. */
    public static Set<String> listClasses(Path jar) throws IOException {
        Set<String> classes = new LinkedHashSet<>();
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            var entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !name.endsWith(".class") || name.startsWith("META-INF/")) {
                    continue;
                }
                String className = name.substring(0, name.length() - ".class".length())
                        .replace('/', '.');
                classes.add(className);
            }
        }
        return classes;
    }
}
