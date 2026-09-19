# Existing behavior fixes

This patch keeps the existing packages, components, commands, move definitions, and progression formulas. It does not implement the unfinished races, fighting styles, fruits, or world content.

- Administrative Haki, Doriki, race, and style subcommands now check OP level directly. Status commands remain public.
- Mob kills award Doriki once. The obsolete 10,000-point cap text was removed; progression remains uncapped.
- Ability cooldowns advance once per server tick. Haki regenerates with combat mode disabled.
- Each player's ability component owns its own Gatling state. Canceling bypasses the running move's cooldown without charging energy again. Death, disconnect, respawn, water, losing the fruit, unequipping the move, and leaving combat stop the attack.
- Gomu animation packets carry the attacker's UUID. Observers animate the attacker rather than their own character. Packet codecs register on both client and dedicated server.
- Existing ability energy costs and fruit-water restrictions are enforced on the server. The Haki bar also shows for fruit users so their energy is visible.
- The aura uses the existing 4,000 Haoshoku XP threshold, charges its declared activation cost, and respects cooldowns after being unequipped or interrupted by water. Revoked powers shut down. Cooldown feedback no longer accesses client HUD classes from server code.
- Armament activation animations wait for server approval and do not play when disabling the power or failing its activation checks.
- Missing or empty fruit/style save tags retain the `none` default.
- The mod metadata declares its existing library dependencies so Fabric can report missing dependencies instead of failing later with missing classes.

## Verification

Run `gradlew.bat build` on Windows with Java 21 or newer suitable for the configured Gradle version. Tests use Fabric Loader JUnit so Minecraft access transformations are applied.

Eight regression tests cover administrative command permissions, all four Gomu animation packet round trips, save defaults, per-player transient ability ownership, cooldown timing across both server tick paths, out-of-combat regeneration, aura progression/revocation, and revoking active Haki.

The local build and tests pass using Java 21. A full interactive Minecraft or multiplayer playtest has not been performed. Before using a shared world, check two simultaneous Gatling users, manual cancellation, disconnect/death during an attack, remote animations, and an ordinary player's inability to execute administrative commands.

Output: `build/libs/redline-1.0.0.jar`. This keeps the repository's current version. Because the Gomu packet format changed, clients and servers must all use the updated build together. The source JAR is not the playable mod.
