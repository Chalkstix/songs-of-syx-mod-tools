package syxmodtools.detector;

import java.nio.file.Path;
import java.util.List;

/**
 * A single enabled mod: its Steam Workshop id, display name (if known),
 * and the compiled jars that make up its code.
 */
public record ModInfo(String workshopId, String name, List<Path> jars) {

    public String displayName() {
        return (name == null || name.isBlank()) ? workshopId : name;
    }
}
