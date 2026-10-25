# Changelog

## 1.0.0 — First complete offline release

- Rebuilt the room-and-furniture generator around deterministic seeds and checked navigation cells.
- Added grid-aligned food and AI targets to prevent flies getting stuck when a target fell inside a blocked navigation cell.
- Added collision-aware BFS routes, automatic recovery targets, and tests across 200 seeds and 600 cross-room routes.
- Polished the Canvas renderer with distinct rooms, floor materials, room-specific furniture, windows, window light pools, animated wings, wing veins, six legs, compound eyes, antennae and skin-specific markings.
- Added 12 cosmetic skins, an expanded four-column achievement view, individual apiary profiles, rename/recall/release controls and capped offline income.
- Fixed first-flight achievement timing, post-escape mode switching, saved-position recovery, back-button navigation and apiary timing while menus are open.
- Added version metadata, app icon, build workflow and a no-Android-SDK procedural-world test.

### Deliberate limits

This version is a 2D offline release. Multiplayer, multi-floor houses, audio, trained neural-network weights and the real FlyWire connectome are not included. The `Real Brain` mode is a labelled educational sensory/motor model, not a claim that connectome data has been integrated.
