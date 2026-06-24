package syxmodtools.detector;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Result of a three-way class comparison: original game class vs mod A vs mod B.
 *
 * <ul>
 *   <li>{@link #onlyInA()} — methods changed only by mod A; safe to take A's version</li>
 *   <li>{@link #onlyInB()} — methods changed only by mod B; safe to take B's version</li>
 *   <li>{@link #inBoth()} — methods changed by both mods; requires manual resolution</li>
 * </ul>
 */
public record ThreeWayClassDiff(
        String className,
        Set<String> onlyInA,
        Set<String> onlyInB,
        Set<String> inBoth
) {

    public static ThreeWayClassDiff of(String className,
                                       Set<String> changedByA,
                                       Set<String> changedByB) {
        Set<String> both = new LinkedHashSet<>(changedByA);
        both.retainAll(changedByB);

        Set<String> onlyA = new LinkedHashSet<>(changedByA);
        onlyA.removeAll(both);

        Set<String> onlyB = new LinkedHashSet<>(changedByB);
        onlyB.removeAll(both);

        return new ThreeWayClassDiff(
                className,
                Collections.unmodifiableSet(onlyA),
                Collections.unmodifiableSet(onlyB),
                Collections.unmodifiableSet(both));
    }

    /** True if both mods changed at least one of the same methods. */
    public boolean hasConflict() {
        return !inBoth.isEmpty();
    }

    /** True when the two mods' changes do not overlap and can be merged automatically. */
    public boolean canAutoMerge() {
        return inBoth.isEmpty();
    }
}
