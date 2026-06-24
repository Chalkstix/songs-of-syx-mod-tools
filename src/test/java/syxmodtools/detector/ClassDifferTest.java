package syxmodtools.detector;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.objectweb.asm.Opcodes.*;

class ClassDifferTest {

    @Test
    void noChangesWhenBytecodeIsIdentical() {
        byte[] cls = classWithMethods("getValue", 1, "getLabel", 42);
        assertTrue(ClassDiffer.changedMethods(cls, cls).isEmpty());
    }

    @Test
    void detectsChangedMethod() {
        byte[] original = classWithMethods("getValue", 1, "getLabel", 42);
        byte[] modified = classWithMethods("getValue", 2, "getLabel", 42);

        Set<String> changed = ClassDiffer.changedMethods(original, modified);

        assertEquals(Set.of("getValue()I"), changed);
    }

    @Test
    void detectsAddedMethod() {
        byte[] original = classWithMethods("getValue", 1);
        byte[] modified = classWithMethods("getValue", 1, "getLabel", 42);

        Set<String> changed = ClassDiffer.changedMethods(original, modified);

        assertEquals(Set.of("getLabel()I"), changed);
    }

    @Test
    void detectsRemovedMethod() {
        byte[] original = classWithMethods("getValue", 1, "getLabel", 42);
        byte[] modified = classWithMethods("getValue", 1);

        Set<String> changed = ClassDiffer.changedMethods(original, modified);

        assertEquals(Set.of("getLabel()I"), changed);
    }

    @Test
    void threeWayDiff_noOverlap() {
        byte[] original = classWithMethods("foo", 1, "bar", 1);
        byte[] modA     = classWithMethods("foo", 2, "bar", 1);
        byte[] modB     = classWithMethods("foo", 1, "bar", 2);

        ThreeWayClassDiff diff = ThreeWayClassDiff.of("pkg.MyClass",
                ClassDiffer.changedMethods(original, modA),
                ClassDiffer.changedMethods(original, modB));

        assertEquals(Set.of("foo()I"), diff.onlyInA());
        assertEquals(Set.of("bar()I"), diff.onlyInB());
        assertTrue(diff.inBoth().isEmpty());
        assertTrue(diff.canAutoMerge());
        assertFalse(diff.hasConflict());
    }

    @Test
    void threeWayDiff_overlappingChange_isConflict() {
        byte[] original = classWithMethods("foo", 1, "bar", 1);
        byte[] modA     = classWithMethods("foo", 2, "bar", 1);
        byte[] modB     = classWithMethods("foo", 3, "bar", 1);

        ThreeWayClassDiff diff = ThreeWayClassDiff.of("pkg.MyClass",
                ClassDiffer.changedMethods(original, modA),
                ClassDiffer.changedMethods(original, modB));

        assertEquals(Set.of("foo()I"), diff.inBoth());
        assertTrue(diff.onlyInA().isEmpty());
        assertTrue(diff.onlyInB().isEmpty());
        assertTrue(diff.hasConflict());
        assertFalse(diff.canAutoMerge());
    }

    /**
     * Generates bytecode for a class with one no-arg int-returning method per
     * (name, returnValue) pair.  Each method just does BIPUSH returnValue; IRETURN.
     */
    private static byte[] classWithMethods(Object... nameValuePairs) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V17, ACC_PUBLIC, "pkg/TestClass", null, "java/lang/Object", null);
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            String name = (String) nameValuePairs[i];
            int value   = (int)   nameValuePairs[i + 1];
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, "()I", null, null);
            mv.visitCode();
            mv.visitIntInsn(BIPUSH, value);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        cw.visitEnd();
        return cw.toByteArray();
    }
}
