package syxmodtools.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void summarizesConflictCountPerModGroup() {
        ModInfo moreOptions = new ModInfo("3044071344", "More Options", List.of());
        ModInfo betterBlueprints = new ModInfo("3699453096", "Better Blueprints", List.of());
        List<Collision> collisions = List.of(
                new Collision("a.SavedPrints", List.of(betterBlueprints, moreOptions)),
                new Collision("a.SavedPrints$SavedPrint", List.of(betterBlueprints, moreOptions)),
                new Collision("view.UISavedPrints", List.of(betterBlueprints, moreOptions)));

        Map<List<ModInfo>, Integer> summary = Main.summarizeByModGroup(collisions);

        assertEquals(1, summary.size());
        assertEquals(3, summary.get(List.of(betterBlueprints, moreOptions)));
    }
}
