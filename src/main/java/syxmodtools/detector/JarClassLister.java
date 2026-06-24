package syxmodtools.detector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
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

    /**
     * Returns the raw bytecode of a single class from a jar, or empty if the
     * class is not present.  {@code className} uses dot-separated notation,
     * e.g. {@code "settlement.room.main.copy.SavedPrints"}.
     */
    public static Optional<byte[]> readClass(Path jar, String className) throws IOException {
        String entryName = className.replace('.', '/') + ".class";
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            JarEntry entry = jarFile.getJarEntry(entryName);
            if (entry == null) return Optional.empty();
            try (InputStream in = jarFile.getInputStream(entry)) {
                return Optional.of(in.readAllBytes());
            }
        }
    }
}
