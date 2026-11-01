# Development

RinLib adapts Minecraft APIs whose spelling or behavior differs between supported versions.
The authoritative target/source table is [support/profiles.json](../support/profiles.json);
consumer evidence is in [targets.md](targets.md).

Use a full JDK 21 for the default 1.21.1 build:

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew :common:test :fabric:build :neoforge:build
python3 -m unittest discover -s tests -p 'test_*.py'
python3 tools/check_adapter_api.py
```

`common` contains the existing pure Java classes and tests. It replaces the old portable
module as a testable source module; it is not repackaged into metadata-only Minecraft artifacts.

Build one additional target directly, without a matrix builder or receipt JSON:

```sh
./gradlew -p profiles -Pprofile=1.20.1 -Ploader=fabric build
profiles/wrapper-forge/gradlew -p profiles -Pprofile=1.20.1 -Ploader=forge build
```

For 26.x, launch Gradle with a full JDK 25. Forge uses the checked-in Gradle 8 wrapper;
1.12.2 uses `profiles/wrapper-legacy/gradlew` with JDK 8. Other targets use the root wrapper.
Pass `-Porg.gradle.java.installations.paths=...` when a required target JDK is not discovered.
Outputs go to `build/profiles/<minecraft>-<loader>`; the default root build uses
`fabric/build` and `neoforge/build`.

New stable adapters live in `adapters/<minecraft>/dev/rinchan/rinlib/api` and must expose
identical public signatures. Select directories in the table rather than introducing
Minecraft version branches into Gradle. ID arguments are strings. Retained compatibility
entrypoints delegate to the new package; do not implement a second copy of behavior there.

See [migration-2.0.md](migration-2.0.md) for current API coverage and the retained
mod-specific code. Dedicated-server probes remain opt-in; see [runtime-tests](../runtime-tests/README.md).

On this Mac, Forge 1.21.1 needed an already-cached `lwjgl-freetype-3.3.3-natives-macos-patch.jar`
that Maven Central does not serve. The local build copied the cached module into
`build/environment/maven`, verified the cache directory SHA-1 and the source/copy SHA-256,
and used `build/environment/local-lwjgl.gradle` as an init script. This is a machine-local
build prerequisite, not a dependency substitution committed to the repository.
