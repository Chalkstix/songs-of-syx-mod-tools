package syxmodtools.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JarClassListerTest {

    @Test
    void listsClassesAndSkipsMetaInfAndDirectories(@TempDir Path tempDir) throws IOException {
        Path jar = TestJars.createJar(tempDir, "test.jar",
                "a/B.class",
                "a/B$1.class",
                "META-INF/MANIFEST.MF",
                "a/");

        Set<String> classes = JarClassLister.listClasses(jar);

        assertEquals(Set.of("a.B", "a.B$1"), classes);
    }
}
