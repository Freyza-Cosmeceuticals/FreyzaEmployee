# Freyza Employee App

Simple Details entry app using Clean Architecture in Jetpack Compose

## Building

Depending on the type(s) of build to be done, create and populate `secret.dev.properties`, `secret.preview.properties`, and `secert.production.properties` with the example content from `secret.example`.
Also, populate `key.properties` from `key.example` with the release signing key, if doing a release build.

Bump the versionCode/versionName in `app/build.gradle.kts` (will automate this from git tag)

Finally build using android studio (will add commands later, probably `./gradlew prodReleaseBuild` or something).

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
