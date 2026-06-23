package syxmodtools.detector;

import java.util.List;

/** A class name that more than one enabled mod defines. */
public record Collision(String className, List<ModInfo> mods) {
}
