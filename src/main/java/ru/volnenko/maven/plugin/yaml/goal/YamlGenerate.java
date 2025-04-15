package ru.volnenko.maven.plugin.yaml.goal;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
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

    @Override
    @SneakyThrows
    public void execute() throws MojoExecutionException, MojoFailureException {
        @NonNull final File buildPath = new File(project.getBuild().getDirectory());
        buildPath.mkdirs();

        @NonNull final String sourceNameJSON = project.getBuild().getFinalName() + "." + project.getPackaging();
        @NonNull final File build = new File(project.getBuild().getDirectory(), sourceNameJSON);

        if (!files.isEmpty()) {
            @NonNull final RootParser rootParser = new RootParser();
            @NonNull final String yaml = rootParser.files(files).json();
            Files.write(build.toPath(), yaml.getBytes(StandardCharsets.UTF_8));
        } else {
            @NonNull final YAMLMapper mapper = new YAMLMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(build, Collections.emptyMap());
        }
    }

}
