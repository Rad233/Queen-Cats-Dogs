package net.pevori.queencats.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HumanoidDogEntity extends TamableAnimal {
    public static final String koroSan = "korone";

    protected HumanoidDogEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    public boolean isDoog(){
        String s = this.getName().getString();
        return (s != null && s.toLowerCase().contains(koroSan));
    }
}