import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Paths;

public class SyxAgentPoC {

    private static PrintWriter log;

    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            log = new PrintWriter(new FileWriter(
                Paths.get(System.getProperty("user.home"), "syx_agent_poc.log").toFile(), true), true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        log.println("=== SyxAgentPoC premain() running. JVM started OK with -javaagent. ===");
        log.println("Working dir: " + System.getProperty("user.dir"));

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                     ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (className != null &&
                    (className.contains("SavedPrints") || className.contains("UISavedPrints"))) {
                    log.println("INTERCEPTED CLASS LOAD: " + className
                        + " | size=" + classfileBuffer.length + " bytes"
                        + " | loader=" + loader);
                }
                return null; // don't modify, just observe for this PoC
            }
        });

        log.println("Transformer registered. Watching for SavedPrints / UISavedPrints class loads...");
    }
}
