package ru.volnenko.maven.plugin.yaml.goal;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

@Mojo(name = "package", defaultPhase = LifecyclePhase.PACKAGE)
public class YamlPackage extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    @SneakyThrows
    public void execute() {
        @NonNull final File buildPath = new File(project.getBuild().getDirectory());
        buildPath.mkdirs();

        @NonNull final String sourceName = project.getBuild().getFinalName() + "." + project.getPackaging();
        @NonNull final File build = new File(project.getBuild().getDirectory(), sourceName);

        @NonNull final Artifact artifact =  project.getArtifact();
        artifact.setFile(build);
    }

}
