package syxmodtools.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassCollisionDetectorTest {

    @Test
    void detectsClassDefinedByMultipleMods(@TempDir Path tempDir) throws IOException {
        // Mirrors the real conflict this tool exists to catch: More Options and
        // Better Blueprints both ship settlement/room/main/copy/SavedPrints.
        Path moreOptionsJar = TestJars.createJar(tempDir, "more-options.jar",
                "settlement/room/main/copy/SavedPrints.class",
                "settlement/room/main/copy/OnlyInMoreOptions.class");
        Path betterBlueprintsJar = TestJars.createJar(tempDir, "better-blueprints.jar",
                "settlement/room/main/copy/SavedPrints.class",
                "view/sett/ui/room/prints/UISavedPrints.class");

        ModInfo moreOptions = new ModInfo("3044071344", "More Options", List.of(moreOptionsJar));
        ModInfo betterBlueprints = new ModInfo("3699453096", "Better Blueprints", List.of(betterBlueprintsJar));

        List<Collision> collisions = new ClassCollisionDetector()
                .findCollisions(List.of(moreOptions, betterBlueprints), null);

        assertEquals(1, collisions.size());
        Collision collision = collisions.get(0);
        assertEquals("settlement.room.main.copy.SavedPrints", collision.className());
        assertEquals(List.of(moreOptions, betterBlueprints), collision.mods());
    }

    @Test
    void noCollisionsWhenModsDefineDisjointClasses(@TempDir Path tempDir) throws IOException {
        Path jarA = TestJars.createJar(tempDir, "a.jar", "pkg/A.class");
        Path jarB = TestJars.createJar(tempDir, "b.jar", "pkg/B.class");

        ModInfo modA = new ModInfo("1", "Mod A", List.of(jarA));
        ModInfo modB = new ModInfo("2", "Mod B", List.of(jarB));

        List<Collision> collisions = new ClassCollisionDetector()
                .findCollisions(List.of(modA, modB), null);

        assertTrue(collisions.isEmpty());
    }
}
