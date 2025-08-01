import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.23"
    antlr
    application
}

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation(kotlin("stdlib"))

    //implementation("com.strumenta.kolasu:kolasu-core:1.5.0-RC5")

    antlr("org.antlr:antlr4:4.13.1") // 使用最新的稳定版本

    // 如果你还需要在运行时使用 ANTLR 库，也指定相同版本
    implementation("org.antlr:antlr4-runtime:4.13.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

sourceSets {
    main {
        java {
            // 显式指定哪些是 Java 文件
            srcDir("src/main/kotlin")
            include("**/*.java")
        }
        kotlin {
            // 显式指定哪些是 Kotlin 文件
            srcDir("src/main/kotlin")
            include("**/*.kt")
        }
    }
}

tasks.compileTestKotlin {
    dependsOn("generateTestGrammarSource")
}

tasks.named<AntlrTask>("generateGrammarSource") {
    description = "Generate ANTLR parser and lexer"
    group = "Code Generation"

    source = fileTree("src/main/antlr").include("*.g4") as FileTree
    arguments = listOf("-visitor", "-package", "parser","-no-listener", "-Dlanguage=Java")
    outputDirectory = file("src/main/kotlin/parser")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }

    // ✅ 添加这一行：排除重复文件
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(files(sourceSets.main.get().output.files))
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("generateGrammarSource")

}

tasks.test {
    useJUnitPlatform()
}