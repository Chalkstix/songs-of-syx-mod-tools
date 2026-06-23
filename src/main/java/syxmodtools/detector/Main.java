package syxmodtools.detector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Console entry point. Scans a user's enabled Songs of Syx mods for
 * class-name collisions and reports them.
 *
 * Usage:
 *   java -jar mod-conflict-detector.jar --game-dir "C:\Steam\steamapps\common\Songs of Syx"
 *
 * If --game-dir is omitted and the jar is run from inside the Songs of Syx
 * install folder itself (e.g. dropped in there and double-clicked via a
 * .bat wrapper), the current directory is used automatically.
 *
 * Optional:
 *   --launcher-settings <path>   (default: %APPDATA%/songsofsyx/settings/LauncherSettings.txt)
 *   --workshop-dir <path>        (override instead of deriving it from --game-dir)
 */
public final class Main {

    private static final String SONGS_OF_SYX_APP_ID = "1162750";

    public static void main(String[] args) {
        try {
            run(args);
        } catch (UsageException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println(usageText());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        ArgMap arg = ArgMap.parse(args);

        Path launcherSettings = arg.path("launcher-settings", defaultLauncherSettingsPath());
        Path workshopDir = arg.has("workshop-dir")
                ? arg.path("workshop-dir", null)
                : deriveWorkshopDir(arg);

        if (workshopDir == null) {
            throw new UsageException("Could not determine the Workshop content folder. "
                    + "Run this from inside your Songs of Syx install folder, "
                    + "or pass --game-dir or --workshop-dir explicitly.");
        }

        System.out.println("Reading enabled mods from " + launcherSettings + " ...");
        List<String> modIds = LauncherSettingsReader.readEnabledModIds(launcherSettings);
        System.out.println("Found " + modIds.size() + " enabled mod id(s).");
        System.out.println();

        List<ModInfo> mods = new ModScanner(workshopDir).resolve(modIds);
        if (mods.size() < modIds.size()) {
            System.out.println("Note: " + (modIds.size() - mods.size())
                    + " enabled mod(s) could not be located under " + workshopDir
                    + " and were skipped.");
            System.out.println();
        }

        ClassCollisionDetector detector = new ClassCollisionDetector();
        List<Collision> collisions = detector.findCollisions(mods, (mod, classCount) ->
                System.out.println("Scanned " + mod.displayName() + " -> " + classCount + " class(es)"));

        System.out.println();
        report(mods, collisions);
    }

    private static void report(List<ModInfo> mods, List<Collision> collisions) {
        if (collisions.isEmpty()) {
            System.out.println("No class-name collisions detected among " + mods.size() + " enabled mod(s).");
            return;
        }

        System.out.println("Found " + collisions.size() + " class-name collision(s):");
        System.out.println();
        for (Collision c : collisions) {
            System.out.println("  " + c.className());
            System.out.println("    defined by (load order): " + modNames(c));
            System.out.println("    -> only one definition can load; the other mod's version of this class is silently ignored.");
        }
        System.out.println();
        System.out.println("Load order above follows your MODS list in LauncherSettings.txt. "
                + "Which mod actually wins isn't guaranteed by this tool alone -- "
                + "if a feature seems missing in-game, try changing mod load order or check with the mod authors.");
    }

    private static String modNames(Collision c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < c.mods().size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(c.mods().get(i).displayName());
        }
        return sb.toString();
    }

    private static Path defaultLauncherSettingsPath() {
        String appData = System.getenv("APPDATA");
        if (appData == null) {
            return null;
        }
        return Path.of(appData, "songsofsyx", "settings", "LauncherSettings.txt");
    }

    private static Path deriveWorkshopDir(ArgMap arg) {
        Path gameDir = arg.path("game-dir", null);
        if (gameDir == null) {
            gameDir = currentDirIfLooksLikeGameDir();
        }
        if (gameDir == null) {
            return null;
        }
        // Steam layout: <library>/steamapps/common/<game> and <library>/steamapps/workshop/content/<appid>
        Path steamapps = gameDir.getParent() != null ? gameDir.getParent().getParent() : null;
        if (steamapps == null) {
            return null;
        }
        return steamapps.resolve("workshop").resolve("content").resolve(SONGS_OF_SYX_APP_ID);
    }

    /** If the jar was placed and run inside the actual game folder, use that as --game-dir. */
    private static Path currentDirIfLooksLikeGameDir() {
        Path cwd = Path.of("").toAbsolutePath();
        return looksLikeGameDir(cwd) ? cwd : null;
    }

    /** A Songs of Syx install directory always contains the main game jar. */
    static boolean looksLikeGameDir(Path dir) {
        return Files.isRegularFile(dir.resolve("SongsOfSyx.jar"));
    }

    private static String usageText() {
        return """
                Usage:
                  java -jar mod-conflict-detector.jar --game-dir "<path to Songs of Syx install>"

                  Or run with no arguments from inside the Songs of Syx install folder itself
                  (the one containing SongsOfSyx.jar) -- it will be detected automatically.

                Optional:
                  --launcher-settings <path>   (default: %APPDATA%/songsofsyx/settings/LauncherSettings.txt)
                  --workshop-dir <path>        (override instead of deriving it from --game-dir)
                """;
    }

    private static final class UsageException extends RuntimeException {
        UsageException(String message) {
            super(message);
        }
    }
}
