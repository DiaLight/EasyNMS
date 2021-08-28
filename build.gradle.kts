buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")
    }
}

plugins {
    java
    id("dialight.easynms")
}

group = "dialight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    implementation(files("./run/cache/patched_1.17.1.jar"))

}

easynms {
    customData {
        outputDir.set(layout.buildDirectory.file("mysite"));
        websiteUrl.set("https://gradle.org")
        vcsUrl.set("https://github.com/gradle/gradle-site-plugin")
    }
    minecraftVersion.set("1.17.1")
//    minecraftVersion.set("1.16.4")
    remappedMinecraftServer.set(file("run/cache/patched_1.17.1.jar"))
}
