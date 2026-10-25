# FlyLife

FlyLife is an offline-first Android fly-life simulator prototype, built incrementally toward one shared world with procedural buildings, fly behaviour, training, saved fly profiles, an apiary and experimental neural models.

## Prototype 0.2
- Seed-based procedural home layouts: home, large home and mansion footprints.
- Room types, internal wall/door gaps, exterior windows, an exit, furniture and food.
- A fly with movement, basic furniture avoidance, exploration memory, hunger/energy and food-seeking behaviour.
- Modes: Escape, Free Life, Training, Fly Lab, Evolution and Real Brain (experimental placeholder).
- Local persistence for seed, mode, needs, progression, coins and basic offline coins.
- Landscape Android Canvas game; no network/server permission required.

## Build
GitHub Actions builds a debug APK on pushes to `main`/`master` and uploads it as `FlyLife-debug-apk`. Open the repository's Actions tab, select the latest successful build, and download the artifact.

## Roadmap
- 0.3: navigation graph, room-aware memory, explicit training tasks and offline neural policy.
- 0.4: multi-floor buildings, validated furniture placement, multiple flies and robust save/load.
- 0.5: achievements, cosmetics and settings.
- 0.6: apiary with saved fly profiles and timestamp-based offline income.
- 0.7: LAN multiplayer via Wi-Fi/hotspot and seed-based world sync.
- 1.0: richer visuals/audio, larger buildings, model comparison and scoped FlyWire experiments.

Neural Fly and FlyWire are currently future/placeholder modes, not a claim that a trained neural model or complete connectome simulation already exists.
