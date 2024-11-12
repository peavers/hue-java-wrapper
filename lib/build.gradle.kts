plugins {
  `java-library`
  `maven-publish`
  jacoco

  alias(libs.plugins.sonarqube)
  alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

dependencies {
  // Jackson
  implementation(libs.jackson.databind)
  implementation(libs.jackson.datatype.jsr310)
  implementation(libs.jackson.datatype.jdk8)

  // Logging
  implementation(libs.slf4j.api)
  implementation(libs.jmdns)

  // Lombok
  compileOnly(libs.lombok)
  annotationProcessor(libs.lombok)

  // Guice
  implementation(libs.guice)
  implementation(libs.guice.assistedinject)

  // Retrofit
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.jackson)
  implementation(libs.okhttp)

  // Tests
  testImplementation(libs.mockito)
  testImplementation(libs.mockito.inline)
  testImplementation(libs.mockito.junit.jupiter)
}

java {
  toolchain { languageVersion = JavaLanguageVersion.of(21) }
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    create<MavenPublication>("library") {
      from(components["java"])

      groupId = "space.forloop"
      artifactId = "hue-java-wrapper"
    }
  }
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/peavers/hue-java-wrapper")
      credentials {
        username = System.getenv("GITHUB_ACTOR") ?: "Peavers"
        password = System.getenv("GITHUB_TOKEN")
      }
    }
  }
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) { useJUnitJupiter("5.10.3") }
  }
}

sonar { properties { property("sonar.projectKey", "peavers_hue-java-wrapper_AZMg-M4W4NtnO15535QJ") } }

spotless {
  java {
    googleJavaFormat().aosp()
    removeUnusedImports()
    importOrder("java", "javax", "org", "com", "")
    target("src/*/java/**/*.java")
  }

  kotlinGradle {
    target("*.gradle.kts")
    ktfmt()
  }
}

tasks.build { dependsOn("spotlessApply") }

tasks.test { finalizedBy(tasks.jacocoTestReport) }

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    csv.required.set(false)
    html.required.set(true)
    html.outputLocation.set(layout.buildDirectory.dir("reports/coverage"))
  }
}

tasks.withType<Javadoc>().configureEach {
  (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}
