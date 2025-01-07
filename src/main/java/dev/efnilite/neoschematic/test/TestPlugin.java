package dev.efnilite.neoschematic.test;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Starting tests...");

        Result result;
        try {
            result = JUnitCore.runClasses(getClasses());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        var failed = result.getFailures().stream()
                .map(failure ->
                        Map.entry(failure.getMessage(),
                                Arrays.stream(failure.getException().getStackTrace())
                                        .map(StackTraceElement::toString)
                                        .toList()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var results = new TestResults(
                result.getRunCount(),
                result.getRunCount() - result.getFailureCount(),
                result.getFailureCount(),
                List.of(),
                failed
        );

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("test-results.json"))) {
            new Gson().toJson(results, writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down server...");

        Bukkit.shutdown();
    }


    private Class<?>[] getClasses() throws IOException {
        JarFile jar = new JarFile(getJarFile());

        String basePackage = "dev/efnilite/neoschematic/";
        List<Class<?>> classes = new ArrayList<>();

        try {
            List<String> classNames = new ArrayList<>();

            for (Iterator<JarEntry> it = jar.entries().asIterator(); it.hasNext(); ) {
                JarEntry e = it.next();

                if (e.getName().startsWith(basePackage) && e.getName().endsWith("Test.class")) {
                    classNames.add(e.getName().replace('/', '.')
                            .substring(0, e.getName().length() - ".class".length()));
                }
            }

            classNames.sort(String::compareToIgnoreCase);

            for (String className : classNames) {
                try {
                    classes.add(Class.forName(className, true, this.getClass().getClassLoader()));
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } finally {
            try {
                jar.close();
            } catch (IOException ignored) {
            }
        }
        return classes.toArray(new Class<?>[0]);
    }

    private File getJarFile() {
        try {
            Method getFile = JavaPlugin.class.getDeclaredMethod("getFile");
            getFile.setAccessible(true);
            return (File) getFile.invoke(this);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }
}
