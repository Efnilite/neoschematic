package dev.efnilite.neoschematic.test;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Starting tests...");

        var listener = new SummaryGeneratingListener();
        var request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("dev.efnilite.neoschematic.test"))
                .filters(includeClassNamePatterns(".*Test"))
                .build();
        var launcher = LauncherFactory.create();
        launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        var summary = listener.getSummary();

        var failed = summary.getFailures().stream()
                .map(failure ->
                        Map.entry(failure.getTestIdentifier().getDisplayName(),
                                Arrays.stream(failure.getException().getStackTrace())
                                        .map(StackTraceElement::toString)
                                        .toList()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        var results = new TestResults(
                summary.getTestsStartedCount(),
                summary.getTestsSucceededCount(),
                summary.getTestsFailedCount(),
                List.of(),
                failed
        );

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("test-results.json"))) {
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
}
