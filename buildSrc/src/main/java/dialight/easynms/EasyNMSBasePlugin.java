package dialight.easynms;

import dialight.easynms.mapping.io.ProguardReader;
import dialight.easynms.tasks.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.jvm.tasks.Jar;


public class EasyNMSBasePlugin implements Plugin<Project> {

    private static final String GROUP = "easynms";

    public void apply(Project project) {
        final var ext = project.getExtensions().create("easynms", EasyNMSExtension.class);
        final var downloadBuildData = project.getTasks().register("downloadBuildData", DownloadBuildData.class, task -> {
            task.setGroup(GROUP);
            if(!ext.getMinecraftVersion().isPresent()) return;
            final var buildDataDir = ext.getEasyNMSDir().resolve("BuildData");

            task.getOutputRepoDir().set(buildDataDir.toFile());
        });
        final var downloadMojangMappings = project.getTasks().register("downloadMojangMappings", DownloadMojangMappings.class, task -> {
            task.setGroup(GROUP);
            if(!ext.getMinecraftVersion().isPresent()) return;
            final var mojangMap = ext.getMappingsDir().resolve("mojang-" + ext.getMinecraftVersion().get() + ".prg");

            task.dependsOn(downloadBuildData);
            task.getInputMappingsUrl().set(downloadBuildData.get().mappingsUrl);
            task.getOutputMapping().set(mojangMap.toFile());
        });
        final var buildMappings = project.getTasks().register("buildMappings", BuildMappings.class, task -> {
            task.setGroup(GROUP);
            if(!ext.getMinecraftVersion().isPresent()) return;
            final var mappingFile = ext.getMappingsDir().resolve(ext.getMinecraftVersion().get() + ".amf");
            final var inverseMappingFile = ext.getMappingsDir().resolve(ext.getMinecraftVersion().get() + "-inversed.amf");

            task.dependsOn(downloadBuildData, downloadMojangMappings);
            task.getInputClassMapping().set(downloadBuildData.get().outputClassMappings);
            task.getInputMembersMapping().set(downloadBuildData.get().outputMemberMappings);
            task.getInputMojangMapping().set(downloadMojangMappings.get().getOutputMapping().get().getAsFile().toPath());
            task.getOutputMapping().set(mappingFile.toFile());
            task.getOutputInverseMapping().set(inverseMappingFile.toFile());
        });

        final var serverApplyMappings = project.getTasks().register("serverApplyMappings", RemapJar.class, task -> {
            task.setGroup(GROUP);
            if(!ext.getMinecraftVersion().isPresent()) return;
            final var serverRemap = ext.getRemapDir().resolve(ext.getMinecraftVersion().get() + "_server_remap.jar");

            task.dependsOn(buildMappings);
            task.getInputMapping().set(buildMappings.get().getOutputMapping());
            task.getInputJar().set(ext.getRemappedMinecraftServer().get());
            task.getOutputJar().set(serverRemap.toFile());
        });

        final var pluginApplyMappings = project.getTasks().register("pluginApplyMappings", RemapJar.class, task -> {
            task.setGroup(GROUP);
            if(!ext.getMinecraftVersion().isPresent()) return;
            final var outJar = ext.getRemapDir().resolve(ext.getMinecraftVersion().get() + "_plugin_remap.jar");

            final var jar = ((Jar) project.getTasks().getByName("jar"));
            task.dependsOn(buildMappings);
            task.getInputMapping().set(buildMappings.get().getOutputInverseMapping());
            task.getInputJar().set(jar.getArchiveFile().get().getAsFile());
            task.getOutputJar().set(outJar.toFile());
        });

//        final var invertClassMappings = project.getTasks().register("invertClassMappings", InvertClassMappings.class, task -> {
//            task.setGroup(GROUP);
//            if(!ext.getMinecraftVersion().isPresent()) return;
//            final var classMapFile = ext.getMappingsDir().resolve("spigot-" + ext.getMinecraftVersion().get() + "-cl-inversed.csrg");
//
//            task.dependsOn(downloadBuildData);
//            task.getInputClassMap().set(downloadBuildData.get().outputClassMappings);
//            task.getOutputClassMap().set(classMapFile.toFile());
//        });
//        final var invertMemberMappings = project.getTasks().register("invertMembersMappings", InvertMembersMappings.class, task -> {
//            task.setGroup(GROUP);
//            if(!ext.getMinecraftVersion().isPresent()) return;
//            final var membersMapFile = ext.getMappingsDir().resolve("spigot-" + ext.getMinecraftVersion().get() + "-members-inversed.csrg");
//
//            task.dependsOn(downloadBuildData);
//            task.getInputMemberMap().set(downloadBuildData.get().outputMemberMappings);
//            task.getOutputMemberMap().set(membersMapFile.toFile());
//        });
//        final var applyMembersMappings = project.getTasks().register("applyMembersMappings", ApplyMemberMappings.class, task -> {
//            task.setGroup(GROUP);
//            if(!ext.getMinecraftVersion().isPresent()) return;
//            final var remappedMcServer = ext.getRemappedMinecraftServer().get().toPath();
//
//            final var membersRemap = ext.getRemapDir().resolve(ext.getMinecraftVersion().get() + "_0_members_remap.jar");
//
//            task.dependsOn(downloadBuildData, invertMemberMappings);
//            task.getInputRepoDir().set(downloadBuildData.get().getOutputRepoDir());
//            task.getInputMapCommand().set(downloadBuildData.get().memberMapCommand);
//            task.getInputMemberMap().set(invertMemberMappings.get().getOutputMemberMap());
//            task.getInputJar().set(remappedMcServer.toFile());
//            task.getOutputJar().set(membersRemap.toFile());
//        });
//        final var applyClassMappings = project.getTasks().register("applyClassMappings", ApplyClassMappings.class, task -> {
//            task.setGroup(GROUP);
//            if(!ext.getMinecraftVersion().isPresent()) return;
//            final var classesRemap = ext.getRemapDir().resolve(ext.getMinecraftVersion().get() + "_1_classes_remap.jar");
//
//            task.dependsOn(downloadBuildData, invertClassMappings, applyMembersMappings);
//            task.getInputRepoDir().set(downloadBuildData.get().getOutputRepoDir());
//            task.getInputMapCommand().set(downloadBuildData.get().classMapCommand);
//            task.getInputClassMap().set(invertClassMappings.get().getOutputClassMap());
//            task.getInputJar().set(applyMembersMappings.get().getOutputJar());
//            task.getOutputJar().set(classesRemap.toFile());
//        });
//        final var invertMojangMappings = project.getTasks().register("invertMojangMappings", InvertMojangMappings.class, task -> {
//            task.setGroup(GROUP);
//            if(!ext.getMinecraftVersion().isPresent()) return;
//            final var invMojangMap = ext.getMappingsDir().resolve("mojang-" + ext.getMinecraftVersion().get() + "-inversed.prg");
//
//            task.dependsOn(downloadMojangMappings);
//            task.getInputMapping().set(downloadMojangMappings.get().getOutputMapping());
//            task.getOutputMapping().set(invMojangMap.toFile());
//        });
//        final var fixRemapDuplicated = project.getTasks().register("fixRemapDuplicates", FixRemapDuplicates.class, task -> {
//            task.setGroup(GROUP);
//            if(!ext.getMinecraftVersion().isPresent()) return;
//            final var fixedJar = ext.getRemapDir().resolve(ext.getMinecraftVersion().get() + "_2_fixed.jar");
//
//            task.dependsOn(invertMojangMappings, applyClassMappings);
//            task.getInputMapping().set(invertMojangMappings.get().getOutputMapping());
//            task.getInputJar().set(applyClassMappings.get().getOutputJar());
//            task.getOutputJar().set(fixedJar.toFile());
//        });
        final var setup = project.getTasks().register("setup", Setup.class, task -> {
            task.setGroup(GROUP);
            if(!ext.getMinecraftVersion().isPresent()) return;

            task.dependsOn(serverApplyMappings);
            task.getInputJar().set(serverApplyMappings.get().getOutputJar());
            task.getOutputJar().set(ext.getRemappedJar().toFile());
        });

        final var downloadProguard = project.getTasks().register("downloadProguard", DownloadProguard.class, task -> {
            task.setGroup(GROUP);
            if(!ext.getMinecraftVersion().isPresent()) return;
            final var proguardHome = ext.getEasyNMSDir().resolve("ProGuard");
            final var proguardJar = proguardHome.resolve("lib").resolve("proguard.jar");

            task.getProguardHome().set(proguardHome.toFile());
            task.getProguardJar().set(proguardJar.toFile());
        });
//        final var applyMojangMappings2 = project.getTasks().register("applyMojangMappings2", ProguardRemap.class, task -> {
//            task.setGroup(GROUP);
//            final var outJar = ext.getRemapDir().resolve(ext.getMinecraftVersion().get() + "_3_mojang_remap2.jar");
//
//            task.dependsOn(downloadBuildData, downloadMojangMappings, fixRemapDuplicated);
//            task.getProguardJar().set(downloadProguard.get().getProguardJar());
//            task.getInputMapping().set(downloadMojangMappings.get().getOutputMapping());
//            task.getInputJar().set(fixRemapDuplicated.get().getOutputJar());
//            task.getOutputJar().set(outJar.toFile());
//        });

//        final var pluginApplyClassMappings = project.getTasks().register("pluginApplyClassMappings", ApplyClassMappings.class, task -> {
//            task.setGroup(GROUP);
//            if(!ext.getMinecraftVersion().isPresent()) return;
//            final var classesRemap = ext.getRemapDir().resolve(ext.getMinecraftVersion().get() + "_1_classes_remap.jar");
//
//            task.dependsOn(downloadBuildData, invertClassMappings, applyMembersMappings);
//            task.getInputRepoDir().set(downloadBuildData.get().getOutputRepoDir());
//            task.getInputMapCommand().set(downloadBuildData.get().classMapCommand);
//            task.getInputClassMap().set(invertClassMappings.get().getOutputClassMap());
//            task.getInputJar().set(applyMembersMappings.get().getOutputJar());
//            task.getOutputJar().set(classesRemap.toFile());
//        });

    }

}