package syxmodtools.detector;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * Prints the three-way diff of SavedPrints between the original game class,
 * More Options, and Better Blueprints.  Requires the game and mods to be
 * installed at the hard-coded paths below — not a regular automated test.
 */
@Disabled("manual exploration — run explicitly with the game installed")
class SavedPrintsDiffExploration {

    private static final String CLASS_NAME = "settlement.room.main.copy.SavedPrints";

    private static final Path GAME_JAR        = Path.of("C:/Steam/steamapps/common/Songs of Syx/SongsOfSyx.jar");
    private static final Path MORE_OPTIONS_JAR = Path.of("C:/Steam/steamapps/workshop/content/1162750/3044071344/V71/script/More Options.jar");
    private static final Path BETTER_BP_JAR    = Path.of("C:/Steam/steamapps/workshop/content/1162750/3699453096/V71/script/Better Blueprints.jar");

    @Test
    void printThreeWayDiff() throws Exception {
        Optional<byte[]> original   = JarClassLister.readClass(GAME_JAR, CLASS_NAME);
        Optional<byte[]> moreOpts   = JarClassLister.readClass(MORE_OPTIONS_JAR, CLASS_NAME);
        Optional<byte[]> betterBp   = JarClassLister.readClass(BETTER_BP_JAR, CLASS_NAME);

        System.out.println("=== SavedPrints three-way diff ===");
        System.out.println("Original in game jar:       " + original.isPresent());
        System.out.println("Present in More Options:    " + moreOpts.isPresent());
        System.out.println("Present in Better Blueprints: " + betterBp.isPresent());
        System.out.println();

        if (original.isEmpty() || moreOpts.isEmpty() || betterBp.isEmpty()) {
            System.out.println("One or more jars missing the class — cannot diff.");
            return;
        }

        Set<String> changedByA = ClassDiffer.changedMethods(original.get(), moreOpts.get());
        Set<String> changedByB = ClassDiffer.changedMethods(original.get(), betterBp.get());

        System.out.println("Methods changed by More Options vs original (" + changedByA.size() + "):");
        changedByA.forEach(m -> System.out.println("  " + m));
        System.out.println();

        System.out.println("Methods changed by Better Blueprints vs original (" + changedByB.size() + "):");
        changedByB.forEach(m -> System.out.println("  " + m));
        System.out.println();

        ThreeWayClassDiff diff = ThreeWayClassDiff.of(CLASS_NAME, changedByA, changedByB);

        System.out.println("Only in More Options (" + diff.onlyInA().size() + "):");
        diff.onlyInA().forEach(m -> System.out.println("  " + m));
        System.out.println();

        System.out.println("Only in Better Blueprints (" + diff.onlyInB().size() + "):");
        diff.onlyInB().forEach(m -> System.out.println("  " + m));
        System.out.println();

        System.out.println("In both (conflict) (" + diff.inBoth().size() + "):");
        diff.inBoth().forEach(m -> System.out.println("  " + m));
        System.out.println();

        System.out.println("Can auto-merge: " + diff.canAutoMerge());
        System.out.println("Has conflict:   " + diff.hasConflict());
    }
}
