package net.pevori.queencats.entity.client;

import net.minecraft.resources.ResourceLocation;
import net.pevori.queencats.QueenCats;
import net.pevori.queencats.entity.custom.PrincessCatEntity;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class PrincessCatModel extends AnimatedGeoModel<PrincessCatEntity> {
    @Override
    public ResourceLocation getModelResource(PrincessCatEntity object) {
        return new ResourceLocation(QueenCats.MOD_ID, "geo/humanoid_cat_children.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PrincessCatEntity object) {
        return QueenCatRenderer.LOCATION_BY_VARIANT.get(object.getVariant());
    }

    @Override
    public ResourceLocation getAnimationResource(PrincessCatEntity animatable) {
        return new ResourceLocation(QueenCats.MOD_ID, "animations/humanoid_cat.animation.json");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setLivingAnimations(PrincessCatEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");

        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        if (head != null) {
            head.setRotationX(extraData.headPitch * ((float) Math.PI / 180F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 180F));
        }
    }
}
