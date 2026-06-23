package syxmodtools.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    @Test
    void recognizesGameDirBySongsOfSyxJar(@TempDir Path dir) throws IOException {
        Files.createFile(dir.resolve("SongsOfSyx.jar"));

        assertTrue(Main.looksLikeGameDir(dir));
    }

    @Test
    void rejectsDirWithoutGameJar(@TempDir Path dir) {
        assertFalse(Main.looksLikeGameDir(dir));
    }
}
