package syxmodtools.detector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/** Builds tiny throwaway jars for tests. Entry contents are irrelevant -- only names matter. */
final class TestJars {

    private TestJars() {
    }

    static Path createJar(Path dir, String fileName, String... classEntryNames) throws IOException {
        Path jarPath = dir.resolve(fileName);
        try (OutputStream out = Files.newOutputStream(jarPath);
             JarOutputStream jarOut = new JarOutputStream(out)) {
            for (String entryName : classEntryNames) {
                jarOut.putNextEntry(new JarEntry(entryName));
                jarOut.closeEntry();
            }
        }
        return jarPath;
    }
}
