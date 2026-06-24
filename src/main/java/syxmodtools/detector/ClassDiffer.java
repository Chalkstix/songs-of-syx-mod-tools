package syxmodtools.detector;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.util.*;

/**
 * Compares two versions of the same class (as raw bytecode) and reports
 * which method signatures differ between them.
 */
public final class ClassDiffer {

    private ClassDiffer() {
    }

    /**
     * Returns the set of method signatures (name + descriptor, e.g.
     * {@code "doThing(Ljava/lang/String;)V"}) that differ between
     * {@code original} and {@code modified}.  Includes methods that were
     * added to or removed from {@code modified} relative to {@code original}.
     */
    public static Set<String> changedMethods(byte[] original, byte[] modified) {
        Map<String, String> origBodies = methodBodies(original);
        Map<String, String> modBodies  = methodBodies(modified);

        Set<String> changed = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : modBodies.entrySet()) {
            if (!entry.getValue().equals(origBodies.get(entry.getKey()))) {
                changed.add(entry.getKey());
            }
        }
        for (String sig : origBodies.keySet()) {
            if (!modBodies.containsKey(sig)) {
                changed.add(sig);
            }
        }
        return Collections.unmodifiableSet(changed);
    }

    private static Map<String, String> methodBodies(byte[] classBytes) {
        ClassNode cn = new ClassNode();
        new ClassReader(classBytes).accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        Map<String, String> bodies = new LinkedHashMap<>();
        for (MethodNode m : cn.methods) {
            bodies.put(m.name + m.desc, serialize(m));
        }
        return bodies;
    }

    private static String serialize(MethodNode m) {
        Map<LabelNode, Integer> lblIdx = new IdentityHashMap<>();
        int n = 0;
        for (AbstractInsnNode insn : m.instructions) {
            if (insn instanceof LabelNode l) lblIdx.put(l, n++);
        }
        StringBuilder sb = new StringBuilder();
        for (AbstractInsnNode insn : m.instructions) {
            appendInsn(sb, insn, lblIdx);
        }
        return sb.toString();
    }

    private static void appendInsn(StringBuilder sb, AbstractInsnNode insn,
                                   Map<LabelNode, Integer> lblIdx) {
        if (insn instanceof LabelNode l) {
            sb.append('L').append(lblIdx.get(l)).append('\n');
        } else if (insn instanceof LineNumberNode) {
            // skip — debug info, not semantically meaningful
        } else if (insn instanceof FrameNode) {
            // skip — stack map frames, not part of the method logic
        } else if (insn instanceof InsnNode i) {
            sb.append(i.getOpcode()).append('\n');
        } else if (insn instanceof IntInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.operand).append('\n');
        } else if (insn instanceof VarInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.var).append('\n');
        } else if (insn instanceof TypeInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.desc).append('\n');
        } else if (insn instanceof FieldInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.owner).append('.')
              .append(i.name).append(' ').append(i.desc).append('\n');
        } else if (insn instanceof MethodInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.owner).append('.')
              .append(i.name).append(i.desc).append('\n');
        } else if (insn instanceof InvokeDynamicInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.name).append(i.desc).append('\n');
        } else if (insn instanceof JumpInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(lblIdx.getOrDefault(i.label, -1)).append('\n');
        } else if (insn instanceof LdcInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.cst).append('\n');
        } else if (insn instanceof IincInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.var).append(' ').append(i.incr).append('\n');
        } else if (insn instanceof TableSwitchInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.min).append('-').append(i.max)
              .append(" def:").append(lblIdx.getOrDefault(i.dflt, -1));
            for (LabelNode lbl : i.labels) sb.append(' ').append(lblIdx.getOrDefault(lbl, -1));
            sb.append('\n');
        } else if (insn instanceof LookupSwitchInsnNode i) {
            sb.append(i.getOpcode()).append(" def:").append(lblIdx.getOrDefault(i.dflt, -1));
            for (int k = 0; k < i.keys.size(); k++) {
                sb.append(' ').append(i.keys.get(k)).append(':')
                  .append(lblIdx.getOrDefault(i.labels.get(k), -1));
            }
            sb.append('\n');
        } else if (insn instanceof MultiANewArrayInsnNode i) {
            sb.append(i.getOpcode()).append(' ').append(i.desc).append(' ').append(i.dims).append('\n');
        } else {
            sb.append('?').append(insn.getOpcode()).append('\n');
        }
    }
}
