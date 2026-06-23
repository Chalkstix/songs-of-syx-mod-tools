package syxmodtools.detector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads the human-readable mod name out of a mod's _Info.txt, if present. */
public final class ModMetadataReader {

    private static final Pattern NAME_FIELD = Pattern.compile("NAME:\\s*\"([^\"]*)\"");

    private ModMetadataReader() {
    }

    /** Returns the mod's declared name, or null if _Info.txt is missing or has no NAME field. */
    public static String readName(Path modDir) {
        Path infoFile = modDir.resolve("_Info.txt");
        if (!Files.isRegularFile(infoFile)) {
            return null;
        }
        try {
            for (String line : Files.readAllLines(infoFile)) {
                Matcher m = NAME_FIELD.matcher(line);
                if (m.find()) {
                    return m.group(1);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
