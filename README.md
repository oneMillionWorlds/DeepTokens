# DeepTokens
A jmonkey library to make 2D images kind of 3D


## Attribution and licensing

This project is licensed under the BSD-3 license, meaning it can be used in a commercial project free of charge with no need to provide attribution. That said if you want to mention that DeepTokens is used in your project that would be very welcome.

## Coding standard

Standard Java coding conventions (try to match existing style).

British English; "Centre", "Colour" etc

## Signing

To sign jars for maven central appropriate details will need to be in C:\Users\{user}\.gradle\gradle.properties. Will need

    signing.keyId=keyId
    signing.password=password
    signing.secretKeyRingFile=C:/Users/{user}/AppData/Roaming/gnupg/pubring.kbx
    
    ossrhUsername=your-jira-id
    ossrhPassword=your-jira-password

Note that the keyId is just the last 8 characters of the long id, and the secretRing must be explicitly exported `gpg --export-secret-keys -o secring.gpg`

## Nexus

Project is provisioned on https://s01.oss.sonatype.org/

Deploy to sonatype via pipeline by:
- Running in gitlab the publish job
- Go to https://s01.oss.sonatype.org/ and log in
- Go to the staging repository and select the repository
- If all looks well "close" the repository and then Release it
- Tag the release


Deploy to sonatype manually by:
- Editing the build.gradle file to have a non snapshot version
- Running `./gradlew publishMavenJavaPublicationToSonaTypeRepository` (see https://docs.gradle.org/current/userguide/publishing_maven.html)
- Go to https://s01.oss.sonatype.org/ and log in as user oneMillionWorlds
- Go to the staging repository and select the repository
- If all looks well "close" the repository and then Release it
- Tag the release

### Testing staging builds

To test a staging build the repository must be in "closed" state, then add the following to the consuming build.gradle

    maven {
        url "https://s01.oss.sonatype.org/content/repositories/comonemillionworlds-XXXX"
    }

Where XXXX is replaced by the specific repository number being used for this version (see https://s01.oss.sonatype.org/#stagingRepositories
when logged in).

