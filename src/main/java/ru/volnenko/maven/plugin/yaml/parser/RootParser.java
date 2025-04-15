package ru.volnenko.maven.plugin.yaml.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.hemantsonu20.json.JsonMerge;
import lombok.NonNull;
import lombok.SneakyThrows;
import ru.volnenko.maven.plugin.yaml.exception.UnsupportedFormatException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class RootParser {

    @NonNull
    private static final ObjectMapper JSON = new ObjectMapper();

    @NonNull
    private static final ObjectMapper YAML = new YAMLMapper();

    @NonNull
    private List<String> files = Collections.emptyList();

    @NonNull
    public RootParser files(@NonNull final List<String> files) {
        this.files = files;
        return this;
    }

    @NonNull
    private ObjectMapper objectMapper(@NonNull final String file) {
        @NonNull final String name = file.toLowerCase(Locale.ROOT);
        if (name.endsWith(".json")) return JSON;
        if (name.endsWith(".yaml")) return YAML;
        if (name.endsWith(".yml")) return YAML;
        throw new UnsupportedFormatException();
    }

    @NonNull
    @SneakyThrows
    private JsonNode map(@NonNull final String file) {
        @NonNull final ObjectMapper objectMapper = objectMapper(file);
        return objectMapper.readTree(new File(file));
    }

    @NonNull
    @SneakyThrows
    public List<JsonNode> all() {
        @NonNull final List<JsonNode> result = new ArrayList<>();
        for (final String file : files) {
            if (file == null || file.isEmpty()) continue;
            result.add(map(file));
        }
        return result;
    }

    @NonNull
    public JsonNode jsonNode() {
        @NonNull final List<JsonNode> jsonNodes = all();
        @NonNull JsonNode mergedNodes = jsonNodes.get(0);
        for (int i = 1; i < jsonNodes.size(); i++) {
            mergedNodes = JsonMerge.merge(mergedNodes, jsonNodes.get(i));
        }
        return mergedNodes;
    }

    @SneakyThrows
    public String json() {
        return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode());
    }

    @SneakyThrows
    public String yaml() {
        return YAML.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode());
    }

}
