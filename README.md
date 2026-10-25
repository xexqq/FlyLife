# FlyLife 1.2.0 — 3D transition build

This build replaces the Canvas renderer with a native Android OpenGL ES 2.0 scene. It is a real 3D render path, not a 2.5D Canvas projection.

## Included
- Procedural seed-driven house using the existing `WorldModel` generator.
- 3D room floors, walls with door gaps, windows, furniture, food, and a custom low-poly fly built from meshes.
- Orbit camera (one-finger drag), pinch zoom, two-finger pan, reset camera.
- Six modes, target selection in Training/Fly Lab, basic fly needs and food, simple exploratory behavior, saved seed and needs.
- Android OpenGL ES 2.0; no external engine dependency.

## Current limitations
This is the first 3D migration build, not a finished high-fidelity 3D release. It uses procedural low-poly meshes and a simplified AI/movement model. Some 2D-only screens (apiary management, achievement catalogue, skin shop and the detailed brain panel) have not yet been migrated into 3D UI. Collision-aware navigation in 3D and fully detailed fly animation remain next steps. The Real Brain mode is still an educational approximation, not the FlyWire connectome.

## Build
GitHub Actions runs `gradle assembleDebug --no-daemon`; the APK artifact is uploaded when the Android build succeeds.
