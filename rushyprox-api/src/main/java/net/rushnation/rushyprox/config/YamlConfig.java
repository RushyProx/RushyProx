package net.rushnation.rushyprox.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.ConcurrentHashMultiset;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class YamlConfig implements IConfig {

    private final Class<? extends YamlConfig> clazz = this.getClass();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();
    private final Yaml yaml = new Yaml();
    private final ConcurrentHashMap<String, Object> manualData = new ConcurrentHashMap<>();

    @Override
    public void load() throws ConfigException {
        File file = this.getFile();

        if (file == null || !file.exists()) {
            this.save();
            return;
        }

        try {
            // First we read the data from the config and save it into this map
            Map<String, LinkedHashMap<String, String>> data = this.yamlMapper.readValue(file, Map.class);

            // Now we loop through the fields of current class and set the value
            for (Field field : this.clazz.getFields()) {
                if (field.canAccess(this)) {

                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }

                    field.set(this, this.objectMapper.readValue(this.objectMapper.writeValueAsString(data.get(field.getName())), field.getType()));
                }
            }
        } catch (IOException | IllegalAccessException e) {
            throw new ConfigException(e);
        }
    }

    @Override
    public void save() throws ConfigException {
        File file = this.getFile();

        if (file == null) {
            throw new ConfigException(String.format("The class %s does not have a @ConfigFile Annotation, please add it.", this.clazz.getName()));
        }

        ConcurrentHashMultiset<String> usedKeys = ConcurrentHashMultiset.create();

        try (FileWriter fileWriter = new FileWriter(file)) {

            for (Field field : this.clazz.getFields()) {
                if (field.canAccess(this)) {
                    if (this.hasComment(field)) {
                        fileWriter.write(String.format("# %s", this.getComment(field)));
                    }

                    fileWriter.write(this.convertToYaml(field).substring(3));

                    fileWriter.write("\n");

                    usedKeys.add(field.getName());
                }
            }

            this.manualData.keySet().forEach(key -> {
                if (usedKeys.contains(key)) {
                    this.manualData.remove(key);
                } else {
                    usedKeys.add(key);
                }
            });

            if (!this.manualData.isEmpty()) {
                fileWriter.write(this.yamlMapper.writeValueAsString(this.manualData).substring(3)); // Substring 3 to remove 3 -
            }

            fileWriter.flush();

        } catch (IOException | IllegalAccessException e) {
            throw new ConfigException(e);
        }
    }

    @Override
    public void set(String path, Object value) throws ConfigException {
        File file = this.getFile();

        if (file == null || !this.getFile().exists()) {
            this.save();
            return;
        }

        if (this.manualData.containsKey(path)) {
            this.manualData.remove(path);
        }

        this.manualData.put(path, value);
    }

    @Override
    public Object get(String path) throws ConfigException {
        try {

            for (Field field : this.clazz.getFields()) {
                if (field.getName().equals(path)) {
                    if (field.canAccess(this)) {
                        return field.get(this);
                    }
                }
            }

            Map<String, LinkedHashMap<String, String>> data = this.yamlMapper.readValue(this.getFile(), Map.class);
            return this.objectMapper.readValue(this.objectMapper.writeValueAsString(data.get(path)), Object.class);
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean hasConfigFile() {
        return this.getFile().exists();
    }

    @Override
    public boolean exists(String path) {
        return this.get(path) != null;
    }

    public File getFile() {
        if (this.getClass().isAnnotationPresent(ConfigFile.class)) {
            ConfigFile configFile = this.getClass().getAnnotation(ConfigFile.class);
            return new File(configFile.destination() + configFile.fileName());
        }

        return null;
    }

    public boolean hasComment(Field field) {
        return field.isAnnotationPresent(Comment.class);
    }

    public String getComment(Field field) {
        return field.getAnnotation(Comment.class).value();
    }

    public StringBuilder getYaml(File file) throws IOException {
        return new StringBuilder(Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    public String convertToYaml(Field field) throws IllegalAccessException, IOException {
        Object object = field.get(this);

        HashMap<String, Object> dataHashMap = new HashMap<>();
        dataHashMap.put(field.getName(), object);

        // First lets convert the data hashmap to json
        JsonNode jsonNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(dataHashMap));
        // Now we will convert the json node to a yaml block
        return this.yamlMapper.writeValueAsString(jsonNode);
    }
}
