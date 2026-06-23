package syxmodtools.detector;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Locates the jar(s) for each enabled mod under the Steam Workshop content
 * folder, e.g. {@code <workshop>/content/1162750/<modId>/V71/script/*.jar}.
 */
public final class ModScanner {

    private final Path workshopContentDir;

    public ModScanner(Path workshopContentDir) {
        this.workshopContentDir = workshopContentDir;
    }

    /** Builds a ModInfo for each id, skipping ids whose folder/jars can't be found. */
    public List<ModInfo> resolve(List<String> modIds) throws IOException {
        List<ModInfo> mods = new ArrayList<>();
        for (String id : modIds) {
            Path modDir = workshopContentDir.resolve(id);
            if (!Files.isDirectory(modDir)) {
                continue;
            }
            String name = ModMetadataReader.readName(modDir);
            List<Path> jars = findJars(modDir);
            if (jars.isEmpty()) {
                continue;
            }
            mods.add(new ModInfo(id, name, jars));
        }
        return mods;
    }

    /** Finds every script/*.jar under any V* version folder inside a mod's directory. */
    private List<Path> findJars(Path modDir) throws IOException {
        List<Path> jars = new ArrayList<>();
        try (DirectoryStream<Path> versionDirs = Files.newDirectoryStream(modDir, "V*")) {
            for (Path versionDir : versionDirs) {
                Path scriptDir = versionDir.resolve("script");
                if (!Files.isDirectory(scriptDir)) {
                    continue;
                }
                try (DirectoryStream<Path> jarFiles = Files.newDirectoryStream(scriptDir, "*.jar")) {
                    for (Path jar : jarFiles) {
                        jars.add(jar);
                    }
                }
            }
        }
        return jars;
    }
}
