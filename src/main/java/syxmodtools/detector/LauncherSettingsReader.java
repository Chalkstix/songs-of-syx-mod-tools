package syxmodtools.detector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the enabled mod list (in load order) out of Songs of Syx's
 * LauncherSettings.txt. The file is not valid JSON (trailing commas,
 * unquoted keys), so this does a small targeted scan rather than using a
 * JSON parser.
 */
public final class LauncherSettingsReader {

    private static final Pattern MOD_ID = Pattern.compile("\"(\\d+)\"");

    private LauncherSettingsReader() {
    }

    /** Returns enabled mod workshop ids, in the load order the launcher will use. */
    public static List<String> readEnabledModIds(Path launcherSettingsFile) throws IOException {
        List<String> lines = Files.readAllLines(launcherSettingsFile);
        List<String> ids = new ArrayList<>();

        boolean inModsBlock = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (!inModsBlock) {
                if (trimmed.startsWith("MODS:")) {
                    inModsBlock = true;
                }
                continue;
            }
            if (trimmed.startsWith("]")) {
                break;
            }
            Matcher m = MOD_ID.matcher(trimmed);
            if (m.find()) {
                ids.add(m.group(1));
            }
        }
        return ids;
    }
}
