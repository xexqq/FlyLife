# FlyLife 1.0 — Seeded Fly-Life Sandbox

**FlyLife 1.0.0** is an offline-first Android 2D life-simulation sandbox. One persistent fly lives in a seed-generated home; progress, skins, individual traits, achievements and the apiary are part of the same save. The release has no multiplayer or network dependency.

## Release features

### World generation
- Four deterministic house footprints: family home, large home, estate and mansion (from 6 to 20 rooms).
- Eight room types with room-specific floor materials and furniture.
- Seeded placement variation, doors, exterior windows, a front exit, food positions and small decorative details.
- The same numeric seed regenerates the same room layout, furniture positions and food placement.
- Navigation-grid validation checks that door approaches and rooms remain connected. If a procedural furniture combination blocks an aisle, the generator removes a non-essential obstacle rather than shipping a sealed room.

### Fly simulation and behaviour
- Animated procedural fly rendering: compound eyes, antennae, six legs, segmented body, translucent wing pairs, wing veins, shading and 12 skins with individual palettes and markings.
- Needs: hunger and energy. Food is consumed only on contact, replenishes needs and respawns after a cooldown. Curious flies also investigate window-lit areas.
- Room-entry memory, exploration rewards, experience and levels.
- Grid-BFS route planning with furniture collision and doorway checks. Navigation has regression tests across varied seeds and rooms.
- Six modes: Escape, Free Life, Training, Fly Lab, Evolution and Real Brain.
- Training uses touch targets. Fly Lab lets you issue manual movement goals. Evolution changes simple behavioural genes at level milestones.
- Real Brain shows a transparent sensory/motor-drive demonstration; it is **not** a real FlyWire connectome or a trained neural network.

### Progression
- Persistent coins, fly profile, needs, level, world seed, explored rooms, achievements and cosmetics.
- Twelve skins; purchase and equip them in the achievements screen.
- Thirteen achievements.
- Apiary stores up to 12 individual fly profiles, with recall, release and rename. Profiles preserve name, skin, traits, genes, level, XP and exploration memory.
- Apiary income accrues while the app is open, including menu screens. Returning from the background credits a capped offline bonus of up to 1,500 coins.
- Settings supports exact numeric seed entry, random new worlds, needs reset and save reset.

### Graphics and platform
- Landscape Android Canvas renderer, designed custom interface, detailed top-down room/furniture illustration, animation and an app icon.
- Android min SDK 23, target/compile SDK 35, version name `1.0.0`.
- No third-party runtime libraries, internet permission, login or server requirement.

## What 1.0 does not claim

This is a polished 2D foundation, not a 3D game engine. It does not contain LAN multiplayer, multi-floor houses, real connectome data, a trained machine-learning model, or sound/audio assets. `Real Brain` is deliberately labelled as a simplified educational behaviour model. Those features need separate design, data and testing rather than a misleading placeholder.

## Build the APK

The GitHub Actions workflow builds a debug APK when you push to `main` or `master` or manually run the workflow. In the repository, open **Actions → Build FlyLife Android APK → latest successful run → Artifacts → FlyLife-1.0-debug-apk**.

For a local Android build, use Java 17, Android SDK platform 35 and Gradle 8.9:

```bash
gradle assembleDebug --no-daemon
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Test the procedural engine without the Android SDK

```bash
javac -encoding UTF-8 -d /tmp/flylife-test \
  app/src/main/java/com/flylife/game/WorldModel.java \
  tools/WorldModelSmokeTest.java
java -cp /tmp/flylife-test WorldModelSmokeTest
```

The smoke test checks repeatable and extreme seeds, food placement, collision and furniture separation, tap-target correction, exit routes and 600 cross-room routes across 200 generated seeds.
