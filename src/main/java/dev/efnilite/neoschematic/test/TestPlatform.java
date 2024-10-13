package dev.efnilite.neoschematic.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TestPlatform {

    private static final String SERVER_JAR_NAME = "server.jar";

    private static String version;
    private static Path path;
    private static Path testPath;
    private static Path testResourcesPath;

    public static void main(String[] args) {
        System.out.println("Initializing test platform");

        version = args[0];
        path = Path.of(args[1]);
        testPath = Path.of(args[2]);
        testResourcesPath = Path.of(args[3]);

        System.out.println("Version: " + version);
        System.out.println("Path: " + path);
        System.out.println("Test path: " + testPath);
        System.out.println("Test resources path: " + testResourcesPath);

        try {
            createServer();
            startServer();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    private static void createServer() throws IOException, URISyntaxException {
        System.out.println("Creating server");

        Path plugins = path.resolve("plugins");
        Files.createDirectories(plugins);

        try (Stream<Path> libs = Files.list(path.getParent().resolve("libs"))) {
            libs.forEach(file -> {
                try {
                    Files.copy(file, plugins.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        String jarSource = getServerJarSource();
        try (InputStream is = new URL(jarSource).openStream()) {
            Files.copy(is, path.resolve(SERVER_JAR_NAME), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String getServerJarSource() throws IOException {
        String stringUrl = "https://api.papermc.io/v2/projects/paper/versions/%s".formatted(version);
        URL url = new URL(stringUrl);

        JsonObject jsonObject;
        try (InputStream is = url.openStream()) {
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            jsonObject = new Gson().fromJson(reader, JsonObject.class);
        }

        JsonArray jsonArray = jsonObject.get("builds").getAsJsonArray();

        int latestBuild = -1;
        for (JsonElement jsonElement : jsonArray) {
            int build = jsonElement.getAsInt();
            if (build > latestBuild) {
                latestBuild = build;
            }
        }

        if (latestBuild == -1)
            throw new IllegalStateException("No builds for this version");

        return "https://api.papermc.io/v2/projects/paper/versions/%s/builds/%d/downloads/paper-%s-%d.jar"
                .formatted(version, latestBuild, version, latestBuild);
    }

    private static void startServer() throws IOException, InterruptedException {
        List<String> args = new ArrayList<>();

        args.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");

        args.add("-ea");
        args.add("-Dcom.mojang.eula.agree=true");
        args.add("-Ddisable.watchdog=true");

        args.add("-jar");
        args.add(SERVER_JAR_NAME);
        args.add("--nogui");

        Process process = new ProcessBuilder(args)
                .directory(path.toFile())
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(process::destroy));

        int code = process.waitFor();
        if (code != 0)
            throw new IOException("Environment returned with code " + code);

        TestResults results;
        try (BufferedReader reader = Files.newBufferedReader(path.resolve("test-results.json"))) {
            results = new Gson().fromJson(reader, TestResults.class);
        } catch (IOException ex) {
            throw new IOException("Failed to read tests: " + ex);
        }

        PrintStream out = System.out;
        if (results.failed() > 0) {
            out = System.err;
        }

        out.println("========== Test report ==========");
        out.println();
        out.println("Tests done: " + results.done());
        out.println("Tests passed: " + results.passed());
        if (results.passed() > 0) {
            out.println("\t" + String.join(", ", results.passes()));
        }
        out.println("Tests failed: " + results.failed());
        out.println();

        if (results.failed() > 0) {
            for (Map.Entry<String, List<String>> entry : results.failures().entrySet()) {
                String name = entry.getKey();
                List<String> stack = entry.getValue();
                out.println(name);
                for (String s : stack) {
                    out.println("\t" + s);
                }
                out.println();
            }
        }

        out.println("========== Test report ==========");

        System.exit(results.failed() > 0 ? -1 : 0);
    }
}
