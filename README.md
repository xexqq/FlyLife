# FlyLife — prototype 0.6

An offline-first Android Canvas prototype for the FlyLife concept. It is still a prototype, but the 0.1–0.6 foundation now lives in one project and one persistent fly/world progression.

## Included in 0.6
- **Seeded house generation:** home / large home / mansion footprints, six room types, interior door gaps, exterior windows, furniture and food. The same seed recreates the same layout.
- **Fly simulation:** movement, collision checks, hunger, energy, food seeking, room visitation memory, exploration, escape goal and progression.
- **Room-aware route planning:** a breadth-first search over the orthogonal room graph selects intermediate room-centre waypoints. It is a lightweight 2D navigation model, not a full physics/pathfinding engine.
- **Six modes:** Escape, Free Life, Training, Fly Lab, Evolution and Real Brain (explicitly experimental placeholder). Training lets the player set a destination; Fly Lab supports manual targets.
- **Persistent progression:** coins, level, needs, house seed, exploration bits, food/escape/training totals, cosmetics and achievements survive app restarts.
- **Achievements and cosmetics:** unlockable milestones and fly body-colour options.
- **Apiary:** save up to 12 fly profiles with colour, skill, XP, behaviour trait and curiosity/stamina; select, recall or release a profile. Apiary flies generate small periodic and timestamp-based offline coin income.
- **Mobile-first UI:** landscape Canvas, no third-party runtime dependencies, no network permission required.

## Honest limitations
- This is not yet a finished commercial game. Furniture placement and collision are intentionally lightweight and should be play-tested on device.
- The “Neural Fly” idea is currently represented by a small rule-based behaviour policy plus exploration variation; it is **not** a trained neural network.
- “Real Brain / FlyWire” is a labelled research placeholder. No connectome data or biological neural simulation is included.
- The current prototype uses one floor; multi-floor buildings, richer individual memories, advanced furniture constraints, sound/art polish and LAN multiplayer remain later work (0.7+).
- Apiary offline income is a simple game-economy mechanic, not a background service.

## Build
GitHub Actions workflow `.github/workflows/android.yml` builds a debug APK on pushes to `main`/`master` and publishes it as the `FlyLife-debug-apk` artifact. Open the repository's Actions tab, select the latest successful run and download the artifact.

## Roadmap
- 0.1: basic room, fly and movement/avoidance.
- 0.2: reproducible procedural house.
- 0.3: room graph, basic memory, training targets and behaviour policy.
- 0.4: robust saved progression and multiple fly profiles.
- 0.5: achievements, cosmetics and settings.
- 0.6: apiary, saved/recallable flies and offline income.
- 0.7+: LAN multiplayer, multi-floor generation, richer AI and presentation.
