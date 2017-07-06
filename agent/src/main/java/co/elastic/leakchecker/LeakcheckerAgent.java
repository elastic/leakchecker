package co.elastic.leakchecker;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.Inflater;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class LeakcheckerAgent {

    static ScheduledExecutorService scheduledExecutorService;

    static Map<Object, Throwable> lastMap = new ConcurrentHashMap<>();

    private static void reportLeaks() {
        System.out.println("Potential leaks:");
        System.out.println("Total: " + AllocationRecorder.allocations.size());

        ConcurrentHashMap<Object, Throwable> mappy = new ConcurrentHashMap<>(AllocationRecorder.allocations);
        // diff
        System.out.println("New:");
        for (Map.Entry<Object, Throwable> entry : mappy.entrySet()) {
            if (!lastMap.containsKey(entry.getKey())) {
                entry.getValue().printStackTrace();
            }

        }
        System.out.println("---");
        lastMap = mappy;
    }

    public static void premain(String arguments, Instrumentation instrumentation) {

        loadDispatcher(instrumentation);

        System.out.println("LeakcheckerAgent.premain");
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(LeakcheckerAgent::reportLeaks, 60, 60, TimeUnit.SECONDS);

        new AgentBuilder.Default()
                .ignore(none())
                .with(AgentBuilder.RedefinitionStrategy.REDEFINITION)
                .type(isSubTypeOf(Inflater.class))
                .transform((builder, type, classLoader, module) ->
                        builder.visit(Advice
                                .to(ConstructionTemplate.class)
                                .on(isConstructor().and(takesArguments(1))))
                                .visit(Advice
                                        .to(CloseTemplate.class)
                                        .on(named("end").and(takesArguments(0)))
                                ))
                .installOn(instrumentation);
    }

    private static void loadDispatcher(Instrumentation instrumentation) {
        final ClassLoader bootstrapClassloader = ClassLoader.getSystemClassLoader().getParent();
        try {
            bootstrapClassloader.loadClass("co.elastic.leakchecker.AllocationRecorder");
            System.out.println("Loaded co.elastic.leakchecker.AllocationRecorder");
        } catch (ClassNotFoundException e) {
            System.out.println("Loading from Jar");

            try {
                final File tempDispatcherJar = File.createTempFile("leakchecker-dispatcher", ".jar");
                tempDispatcherJar.deleteOnExit();

                try (InputStream input = LeakcheckerAgent.class.getClassLoader().getResourceAsStream("leakchecker-dispatcher.jar")) {
                    if (input == null) {
                        throw new IllegalStateException("Could not find leakchecker-dispatcher.jar");
                    }

                    try (FileOutputStream fileOutputStream = new FileOutputStream(tempDispatcherJar)) {
                        byte[] buffer = new byte[1024];
                        while ((input.read(buffer)) != -1) {
                            fileOutputStream.write(buffer);
                        }
                    }
                }

                JarFile jarFile = new JarFile(tempDispatcherJar);

                instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
                bootstrapClassloader.loadClass("co.elastic.leakchecker.AllocationRecorder");
                System.out.println("Loaded co.elastic.leakchecker.AllocationRecorder from jar");

            } catch (IOException | ClassNotFoundException e2) {
                e2.printStackTrace();
            }
        }


    }

}
