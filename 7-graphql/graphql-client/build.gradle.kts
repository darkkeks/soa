import soa.conventions.Versions.cliktVersion

plugins {
    id("soa.application")
    id("com.apollographql.apollo3") version "3.2.1"
}

application {
    mainClass.set("me.darkkeks.soa.graphql.client.MafiaGraphQLClientKt")
}

apollo {
    generateOptionalOperationVariables.set(false)

    packageName.set("me.darkkeks.soa.graphql.client.model")

    introspection {
        schemaFile.set(file("src/main/graphql/me/darkkeks/soa/graphql/client/model/schema.json"))
        endpointUrl.set("http://localhost:8080/graphql")
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/main/graphql")
        }
    }
}

dependencies {
    implementation("com.apollographql.apollo3:apollo-runtime:3.2.1")
    implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
}
