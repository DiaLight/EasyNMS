package dialight.easynms;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.util.Arrays;

public class EasyNMSPlugin implements Plugin<Project> {

    public static final boolean DEVELOP = true;

    public void apply(Project project) {
        project.getPlugins().apply(EasyNMSBasePlugin.class);

        project.getPlugins().apply(JavaPlugin.class);
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
//        main.getJava().setSrcDirs(Arrays.asList("src"));


        Configuration dataFiles = project.getConfigurations().create("dataFiles", c -> {
            c.setVisible(false);
            c.setCanBeConsumed(false);
            c.setCanBeResolved(true);
            c.setDescription("The data artifacts to be processed for this plugin.");
            c.defaultDependencies(d -> d.add(project.getDependencies().create("org.myorg:data:1.4.6")));
        });

//        project.getTasks().withType(DataProcessing.class).configureEach(
//                dataProcessing -> dataProcessing.getDataFiles().from(dataFiles));

    }

}
