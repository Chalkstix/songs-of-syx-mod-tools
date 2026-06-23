package syxmodtools.detector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Builds a class-name -&gt; defining-mods map across a set of mods and
 * reports any class name defined by more than one of them.
 */
public final class ClassCollisionDetector {

    /**
     * Scans every mod's jars and returns collisions, in the order classes
     * were first encountered.
     *
     * @param onModScanned called with (mod, classCount) as each mod finishes scanning,
     *                      so a caller can show live progress. May be null.
     */
    public List<Collision> findCollisions(List<ModInfo> mods, BiConsumer<ModInfo, Integer> onModScanned)
            throws IOException {
        Map<String, List<ModInfo>> classToMods = new LinkedHashMap<>();

        for (ModInfo mod : mods) {
            var classesInMod = new java.util.LinkedHashSet<String>();
            for (Path jar : mod.jars()) {
                classesInMod.addAll(JarClassLister.listClasses(jar));
            }
            for (String className : classesInMod) {
                classToMods.computeIfAbsent(className, k -> new ArrayList<>()).add(mod);
            }
            if (onModScanned != null) {
                onModScanned.accept(mod, classesInMod.size());
            }
        }

        List<Collision> collisions = new ArrayList<>();
        for (var entry : classToMods.entrySet()) {
            if (entry.getValue().size() > 1) {
                collisions.add(new Collision(entry.getKey(), entry.getValue()));
            }
        }
        return collisions;
    }
}
