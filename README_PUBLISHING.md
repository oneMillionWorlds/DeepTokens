# Publishing to Maven Central (via Sonatype Central Publisher Portal)

This project uses Sonatype Central's modern Publisher Portal and API (USER_MANAGED flow). Legacy OSSRH staging is no longer used in CI.

The short version:
- Gradle stages your publication locally and zips it to build/central-bundle.zip.
- A helper script uploads the bundle to Sonatype Central with a Bearer token.
- You complete the release in the Central Portal UI once validation passes.

See also: PublishingUsingPortalApi.txt in this repo for official API details and extra examples.

## 1) Signing

Artifacts published to Maven Central must be signed. Configure your GPG details in your personal Gradle properties file (typically at:
Windows: C:\\Users\\{user}\\.gradle\\gradle.properties):

    signing.keyId=keyId
    signing.password=password
    signing.secretKeyRingFile=C:/Users/{user}/AppData/Roaming/gnupg/pubring.kbx

Notes:
- keyId is the last 8 characters of your long key id.
- If you need a legacy secret ring file, export one with:

      gpg --export-secret-keys -o secring.gpg

  You can then set signing.secretKeyRingFile to the absolute path of that secring.gpg.

In CI, the workflow decodes a provided secring.gpg and points Gradle to it automatically.

## 2) Central Publisher Portal credentials

OSSRH is being sunset in 2025; Sonatype Central's Publisher Portal is used instead. Credentials are the same username/password as your OSSRH/Central account.

Set Central credentials either in gradle.properties or as environment variables.

- gradle.properties entries:

      centralUsername=your-username
      centralPassword=your-password

- or environment variables:

      CENTRAL_USERNAME=your-username
      CENTRAL_PASSWORD=your-password

The CI workflow maps the existing OSSRH secrets to these properties automatically.

## 3) How the new flow works

1. Gradle publishes the mavenJava publication into a local directory (no network): build/central-portal-staging/.
2. Gradle zips that directory into build/central-bundle.zip.
3. CI (or you locally) uploads the zip to Sonatype Central's Publisher API with publishingType=USER_MANAGED.
4. After validation passes (state VALIDATED), publish the deployment in the Central Portal UI.

## 4) Publish from your machine (manual)

Prerequisites:
- GPG signing configured (see section 1).
- Central credentials configured (see section 2).
- curl installed for the helper script.

Steps:

- Stage and zip the bundle:

      ./gradlew prepareCentralBundle

  This creates: build/central-bundle.zip

- Upload the bundle with the helper script (provide credentials via env vars):

      CENTRAL_USERNAME=your-username CENTRAL_PASSWORD=your-password \
      .github/scripts/upload_central.sh "build/central-bundle.zip"

On success, the script prints a Deployment ID. Visit:
https://central.sonatype.com/publishing/deployments

to monitor the deployment and click Publish when it reaches VALIDATED.

You can still publish to your local Maven for dependency testing:

    ./gradlew publishToMavenLocal

## 5) Publish in CI (GitHub Actions)

The workflow at .github/workflows/publish.yml has been updated to:
- Build the project
- Load signing keys from secrets
- Provide Central credentials
- Run `./gradlew prepareCentralBundle`
- Run `.github/scripts/upload_central.sh "build/central-bundle.zip"`

No direct uploads to OSSRH occur anymore in CI.

Required GitHub Secrets:
- SIGNING_SECRET_KEY_RING_FILE — base64 of your secring.gpg
- SIGNING_KEY_ID — the short key id
- SIGNING_PASSWORD — your key passphrase
- OSSRH_USERNAME, OSSRH_PASSWORD — your Sonatype Central/OSSRH credentials

## 6) Legacy OSSRH notes

The previous OSSRH (SonaType) repository configuration remains in build.gradle for local/manual experiments only, but the automated pipeline no longer uses it. Prefer the Central Portal going forward.

## 7) Manual testing of validated bundles

Central supports consuming validated (but not yet published) artifacts via a special repository that requires an Authorization header.

In Gradle, use the built-in HTTP header authentication types:
- org.gradle.api.credentials.HttpHeaderCredentials
- org.gradle.authentication.http.HttpHeaderAuthentication

Example configuration that reads the header name/value from gradle.properties:

```
repositories {
    maven {
        name = "centralManualTesting"
        url = uri("https://central.sonatype.com/api/v1/publisher/deployments/download/")
        credentials(org.gradle.api.credentials.HttpHeaderCredentials) {
            name = findProperty('centralManualTestingAuthHeaderName') as String
            value = findProperty('centralManualTestingAuthHeaderValue') as String
        }
        authentication {
            header(org.gradle.authentication.http.HttpHeaderAuthentication)
        }
    }
    mavenCentral()
}
```

And in your gradle.properties:

```
# Use a Bearer token made from base64(username:password).
centralManualTestingAuthHeaderName=Authorization
centralManualTestingAuthHeaderValue=Bearer <base64-username-colon-password>
```

Notes:
- This repository exposes only VALIDATED (not yet published) files; once you publish, resolve from Maven Central as usual.
- The exact same header works for both single-deployment and multi-deployment endpoints (see PublishingUsingPortalApi.txt for full details).

## 8) Useful references

- Central Publisher API docs: see PublishingUsingPortalApi.txt in this repo.
- CI workflow: .github/workflows/publish.yml
- Upload helper script: .github/scripts/upload_central.sh
- Gradle publication tasks defined in: build.gradle (prepareCentralBundle, zipCentralPortalBundle, publishToCentralPortalStaging)