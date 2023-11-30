plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.android.junit.jupiter)
    alias(libs.plugins.kover)
    id("org.jetbrains.kotlin.jvm") apply false

    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(17)
}

koverReport {
    filters {
        includes {
            packages("ai.basemind.client")
        }
    }
}

android {
    namespace = "ai.basemind.client"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        minSdk = 25

        aarMetadata {
            minCompileSdk = 25
        }
    }

    buildTypes {
        release {}
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    protobuf(files("../proto/gateway/v1"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.grpc.kotlin.stub)
    api(libs.grpc.protobuf.kotlin.lite)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.stub)
    implementation(libs.androidx.core.ktx)
    implementation(libs.grpc.okhttp)

    testImplementation(libs.grpc.testing)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.system.stubs.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.jupiter.engine)

    compileOnly(libs.annotations.api)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        create("java") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.59.1"
        }
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.59.1"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("java") {
                    option("lite")
                }
                create("grpc") {
                    option("lite")
                }
                create("grpckt") {
                    option("lite")
                }
            }
            it.builtins {
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "ai.basemind"
            artifactId = "client"
            version = "0.0.1"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("BaseMind.AI Client")
                description.set("API Client for the BaseMind.AI Gateway")
                url.set("https://basemind.ai/")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/basemind-ai/sdk-android")
                    developerConnection.set("scm:git:https://github.com/basemind-ai/sdk-android")
                    url.set("https://github.com/basemind-ai/sdk-android")
                }
                developers {
                    organization {
                        name.set("BaseMind.AI")
                        url.set("https://basemind.ai/")
                    }
                    developer {
                        name.set("BaseMind.AI")
                        email.set("support@basemind.ai")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = providers.environmentVariable("SONATYPE_USER").orNull
                password = providers.environmentVariable("SONATYPE_PASSWORD").orNull
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
