
plugins {
    java
    idea
    `java-gradle-plugin`
}

group = "dialight"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:21.0.1")
    implementation(gradleApi())
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.12.0.202106070339-r")
    implementation("com.google.code.gson:gson:2.8.7")

    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("org.ow2.asm:asm-util:9.2")

//    implementation("org.ow2.asm:asm:9.2:sources")
//    implementation("org.ow2.asm:asm-commons:9.2:sources")
//    implementation("org.ow2.asm:asm-util:9.2:sources")

//    implementation("org.ow2.asm:asm:9.2:javadoc")
//    implementation("org.ow2.asm:asm-commons:9.2:javadoc")
//    implementation("org.ow2.asm:asm-util:9.2:javadoc")

    implementation("commons-cli:commons-cli:1.4")
    implementation("commons-io:commons-io:2.6")
    implementation("commons-collections:commons-collections:3.2.2")
}

//idea {
//    module {
//        isDownloadJavadoc = true
//        isDownloadSources = true
//    }
//}

gradlePlugin {
    plugins {
        create("EasyNMS") {
            id = "dialight.easynms"
            implementationClass = "dialight.easynms.EasyNMSPlugin"
        }
    }
}
