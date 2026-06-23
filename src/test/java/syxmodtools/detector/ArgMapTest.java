package syxmodtools.detector;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArgMapTest {

    @Test
    void normalizesTrailingDotSegment() {
        // Mirrors the %~dp0. trick used in run-from-game-dir.bat to dodge
        // %~dp0's trailing backslash escaping a closing quote.
        ArgMap arg = ArgMap.parse(new String[] {"--game-dir", "C:\\Steam\\Songs of Syx\\."});

        assertEquals(Path.of("C:\\Steam\\Songs of Syx"), arg.path("game-dir", null));
    }

    @Test
    void returnsFallbackWhenFlagMissing() {
        ArgMap arg = ArgMap.parse(new String[] {});

        assertNull(arg.path("game-dir", null));
    }
}
