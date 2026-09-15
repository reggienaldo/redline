package net.reggie.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public class ModSounds {
    public static final SoundEvent EXPLODE = registerSoundEvent("explode");

    public static final SoundEvent HAO_HAKI = registerSoundEvent("haki_release");

    public static final SoundEvent HAKI_ACTIVATE = registerSoundEvent("haki_activate");

    public static final SoundEvent KEN_HAKI_ACTIVATE = registerSoundEvent("kenbunshoku_haki_on");

    public static final SoundEvent KEN_HAKI_DEACTIVATE = registerSoundEvent("kenbunshoku_haki_off");

    public static final SoundEvent GUARD_HAKI = registerSoundEvent("guard_haki");

    public static final SoundEvent FUTURE_SIGHT_HIT = registerSoundEvent("future_sight_hit");

    public static final SoundEvent DODGE = registerSoundEvent("dodge");

    public static final SoundEvent SORU_TELEPORT = registerSoundEvent("soru");

    public static final SoundEvent GEPPO = registerSoundEvent("geppo");


    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = Identifier.of(Redline.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        Redline.LOGGER.info("Registering Mod Sounds for " + Redline.MOD_ID);
    }
}
