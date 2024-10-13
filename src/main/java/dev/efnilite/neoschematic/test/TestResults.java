package dev.efnilite.neoschematic.test;

import java.util.List;
import java.util.Map;

public record TestResults(
        long done,
        long passed,
        long failed,
        List<String> passes,
        Map<String, List<String>> failures
) {

}
