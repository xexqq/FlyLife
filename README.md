# FlyLife

FlyLife is an early Android prototype for the fly-life simulator concept.

## Prototype 0.1
- Landscape Android app, no network permission or server required.
- One room with seed-based furniture layout.
- Automatic fly movement (Simple AI), observer mode, training target mode, and a basic lab screen.
- Basic coin counter and local persistence.
- New House button changes the procedural seed.
- GitHub Actions workflow builds a debug APK and uploads it as an artifact.

## Build
Requires JDK 17, Android SDK platform 35/build-tools available, and Gradle 8.9:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Roadmap
1. Improve room-graph and furniture placement constraints.
2. Add multiple connected rooms, doors, windows and floors.
3. Add proper fly states, food-seeking and collision avoidance.
4. Add Neural Fly with offline pretraining and persistent per-fly memory.
5. Add save/load, achievements, cosmetics and economy.
6. Add the offline-income apiary with timestamp-based progress calculation.
7. Add LAN multiplayer; no dedicated online server planned.
8. Investigate a small FlyWire-inspired experimental model.

This is an initial scaffold, not the complete game described in the design document.
