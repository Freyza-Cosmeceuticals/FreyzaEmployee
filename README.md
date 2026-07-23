# Freyza Employee App

Simple Details entry app using Clean Architecture in Jetpack Compose

## Building

Depending on the type(s) of build to be done, create and populate `secret.dev.properties`, `secret.preview.properties`, and `secert.production.properties` with the example content from `secret.example`.
Also, populate `key.properties` from `key.example` with the release signing key, if doing a release build.

## Versioning

The project follows a specific versioning scheme in `app/build.gradle.kts`:

- **`versionName`**: Follows **0-based Semantic Versioning** (e.g., `0.1.0`). The major version remains `0` during the initial development phase.
- **`versionCode`**: A monotonic incrementing integer. It **never resets**, even when the `versionName` changes significantly. It is used by the Android system to identify newer builds.

## Release Process

1.  **Feature Completion**: Ensure all features for the release are merged into the `preview` branch.
2.  **Version Bump**: Increment `versionCode` and update `versionName` in `app/build.gradle.kts`.
3.  **Changelog**: Document the changes in `CHANGELOG.md` under the new version header with the current date.
4.  **Merge**: Perform a fast-forward merge from `preview` to `main` as described in the [Development](#development) section.
5.  **Build**: Generate the release build using:
    ```bash
    ./gradlew prodReleaseBuild
    ```

## Development

The repo has 2 fixed branches - `main` and `preview`. The `main` branch is protected, and no code should be directly pushed to it, `preview` is branched off from `main`.

To develop any feature, branch off from `preview`, say `feature-1`. Work on it and open a PR `feature-1` -> `preview` first.
After completion, either _squash_ or _rebase_ (from github UI preferably) depending on the number/type of commits made.

Once in a while, after testing everything, do a fast-forward merge from `preview` -> `main` (open a PR on GitHub, but merge and push from local, since GitHub doesn'r allow ff merges).
Since `main` is never updated independently, the ff merge will always succeed.

```
git switch preview
git pull origin preview

git switch main
git pull origin main

git merge --ff-only preview
git push origin main
```

Future plans include automated testing/building from `preview` and `main` branches.
