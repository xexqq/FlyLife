# Changelog

## 1.1.0 — Camera and organic flight update

- Added pinch-to-zoom, two-finger pan, one-finger pan while zoomed and a camera reset control.
- Enlarged the rendered fly and improved room depth with bevelled floor edges, contact shadows and raised furniture faces.
- Reduced flight speed and added turning inertia, gentle lateral drift and periodic heading variation to avoid ruler-straight motion.
- Changed exploration target scoring so agents are less biased toward the shortest nearby destination.
- Escape mode now explores first and only heads for the exit after discovering it.
- Real Brain now holds its food target longer, makes decisions less frequently, prioritizes food at higher hunger and restores more hunger on eating.
- Updated Android version metadata to 1.1.0.

## 1.0.0 — First complete offline release

- Deterministic seeded houses, furniture placement, collision checks and room navigation.
- Animated fly, twelve cosmetic skins, needs, experience, achievements, six modes and persistent apiary profiles.
- Offline income, save system, Android icon and GitHub Actions build workflow.

### Deliberate limits

The renderer is 2.5D-style Android Canvas, not a full 3D engine. Multiplayer, multi-floor houses, sound, trained neural-network weights and the real FlyWire connectome are not included. `Real Brain` is an educational sensory/motor model, not integrated connectome data.


## 1.2.0-3d
- Replaced the 2D Canvas screen with native OpenGL ES 2.0 rendering.
- Added perspective camera, room geometry, partition door gaps, window panels, 3D furniture, food meshes, and a low-poly animated fly.
- Added orbit, pinch zoom, pan, and world-target touch controls.
- Preserved the deterministic `WorldModel` seed generator and basic persistent needs/progress.
- Marked 2D-only collection screens and advanced collision/AI work as unfinished instead of implying they were migrated.
