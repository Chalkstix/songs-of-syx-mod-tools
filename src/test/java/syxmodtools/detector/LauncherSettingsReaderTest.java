package syxmodtools.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LauncherSettingsReaderTest {

    @Test
    void readsModIdsInOrder(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("LauncherSettings.txt");
        Files.writeString(file, """
                JVM: 0,
                DEBUG: 0,
                MODS: [
                	"3331182511",
                	"3699453096",
                	"3044071344",
                ],
                JVM_ARGS2: [
                	"-Xms512m",
                ],
                """);

        List<String> ids = LauncherSettingsReader.readEnabledModIds(file);

        assertEquals(List.of("3331182511", "3699453096", "3044071344"), ids);
    }

    @Test
    void returnsEmptyListWhenModsBlockIsEmpty(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("LauncherSettings.txt");
        Files.writeString(file, """
                MODS: [
                ],
                JVM_ARGS2: [
                	"-Xms512m",
                ],
                """);

        List<String> ids = LauncherSettingsReader.readEnabledModIds(file);

        assertEquals(List.of(), ids);
    }
}
