package com.example.dread.client;

import com.example.dread.DreadMod;
import com.example.dread.entity.WatcherEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Identifier;

/** Светящиеся в темноте глаза (рендерится поверх основной текстуры). */
public class WatcherEyesFeature extends EyesFeatureRenderer<WatcherEntity, BipedEntityModel<WatcherEntity>> {
    private static final RenderLayer EYES = RenderLayer.getEyes(
            new Identifier(DreadMod.MOD_ID, "textures/entity/watcher_eyes.png"));

    public WatcherEyesFeature(FeatureRendererContext<WatcherEntity, BipedEntityModel<WatcherEntity>> ctx) {
        super(ctx);
    }

    @Override
    public RenderLayer getEyesTexture() {
        return EYES;
    }
}
