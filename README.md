# DeepTokens
A jmonkey library to make 2D images kind of 3D

![An image of a thickened sun and cloud image](readmeResources/cloudAndSun.png)

## Getting started

This library is available on maven central 

As a maven dependency:
    
    <dependency>
        <groupId>com.onemillionworlds</groupId>
        <artifactId>deeptokens</artifactId>
        <version>X.X.X</version>
    </dependency>

As a gradle dependency:

    implementation "com.onemillionworlds:deeptokens:X.X.X"

See https://mvnrepository.com/artifact/com.onemillionworlds/deeptokens for the latest version.

Create a DeepTokenBuilder and specify its width and depth (height is implicit from the image shape)

    DeepTokenBuilder deepTokenBuilder = new DeepTokenBuilder(1, 0.1f);

Load an image

    BufferedImage tokenImage = ImageIO.read(TestApplication.class.getResourceAsStream("/pathToImage.png"));

Request this be turned into a deep token

    Geometry deepToken = deepTokenBuilder.bufferedImageToLitGeometry(tokenImage);

Or just get the mesh and texture and put it together yourself (if you want to use an unusual material)

    Mesh mesh = deepTokenBuilder.bufferedImageToMesh(tokenImage);
    Texture texture = deepTokenBuilder.imageToTexture(tokenImage);

(Note that the imageToTexture adds a margin on the image in case a simplification of the edge means the shape "slips off the edge")

## Attribution and licensing

This project is licensed under the BSD-3 license, meaning it can be used in a commercial project free of charge with no need to provide attribution. That said if you want to mention that DeepTokens is used in your project that would be very welcome.

# Maintainer notes

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

## Publishing

See README_PUBLISHISHING.md

### Testing staging builds

To test a staging build the repository must be in "closed" state, then add the following to the consuming build.gradle

    maven {
        url "https://s01.oss.sonatype.org/content/repositories/comonemillionworlds-XXXX"
    }

Where XXXX is replaced by the specific repository number being used for this version (see https://s01.oss.sonatype.org/#stagingRepositories
when logged in).

