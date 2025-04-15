package ru.volnenko.maven.plugin.yaml.goal;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import ru.volnenko.maven.plugin.yaml.parser.RootParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class YamlGenerate extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Getter
    @Setter
    @Parameter(property = "files")
    private List<String> files = new ArrayList<>();

    @Parameter(defaultValue = "${settings}", required = true, readonly = true)
    private Settings settings;

    @Override
    @SneakyThrows
    public void execute() throws MojoExecutionException, MojoFailureException {
        @NonNull final File buildPath = new File(project.getBuild().getDirectory());
        buildPath.mkdirs();

        for (final Object dependencyObject: project.getDependencyArtifacts()) {
            if (dependencyObject == null) continue;
            @NonNull final DefaultArtifact dependency = (DefaultArtifact) dependencyObject;
            if (!"compile".equalsIgnoreCase(dependency.getScope())) continue;
            if ("json".equalsIgnoreCase(dependency.getType()) || "yaml".equalsIgnoreCase(dependency.getType())) {
                @NonNull final String name = dependency.getGroupId().replace(".", "/") + "/"
                        + dependency.getArtifactId() + "/" + dependency.getVersion() + "/"
                        + dependency.getArtifactId() + "-" + dependency.getVersion() + "." + dependency.getType();
                @NonNull final File file = new File(settings.getLocalRepository(), name);
                @NonNull final String filename = file.getAbsolutePath();
                if (!file.exists()) {
                    getLog().error("Error! File `"+ filename + "` is not exists...");
                    continue;
                }
                if (!files.contains(file.getAbsolutePath())) {
                    files.add(filename);
                    getLog().info("ADDED: " + file);
                }
            }
        }

        @NonNull final String sourceNameJSON = project.getBuild().getFinalName() + "." + project.getPackaging();
        @NonNull final File build = new File(project.getBuild().getDirectory(), sourceNameJSON);

        if (!files.isEmpty()) {
            @NonNull final RootParser rootParser = new RootParser();
            @NonNull final String yaml = rootParser.files(files).yaml();
            Files.write(build.toPath(), yaml.getBytes(StandardCharsets.UTF_8));
        } else {
            @NonNull final YAMLMapper mapper = new YAMLMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(build, Collections.emptyMap());
        }
    }

}
