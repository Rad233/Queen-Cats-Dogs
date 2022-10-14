package net.pevori.queencats.sound;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pevori.queencats.QueenCats;


public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, QueenCats.MOD_ID);

    public static RegistryObject<SoundEvent> HUMANOID_CAT_AMBIENT = registerSoundEvent("humanoid_cat_ambient");
    public static RegistryObject<SoundEvent> HUMANOID_CAT_EAT = registerSoundEvent("humanoid_cat_eat");
    public static RegistryObject<SoundEvent> HUMANOID_CAT_HURT = registerSoundEvent("humanoid_cat_hurt");
    public static RegistryObject<SoundEvent> HUMANOID_CAT_DEATH = registerSoundEvent("humanoid_cat_death");

    public static RegistryObject<SoundEvent> HUMANOID_DOG_AMBIENT = registerSoundEvent("humanoid_dog_ambient");
    public static RegistryObject<SoundEvent> HUMANOID_DOG_EAT = registerSoundEvent("humanoid_dog_eat");
    public static RegistryObject<SoundEvent> HUMANOID_DOG_HURT = registerSoundEvent("humanoid_dog_hurt");
    public static RegistryObject<SoundEvent> HUMANOID_DOG_ANGRY = registerSoundEvent("humanoid_dog_angry");
    public static RegistryObject<SoundEvent> HUMANOID_DOG_DEATH = registerSoundEvent("humanoid_dog_death");


    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(QueenCats.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> new SoundEvent(id));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}