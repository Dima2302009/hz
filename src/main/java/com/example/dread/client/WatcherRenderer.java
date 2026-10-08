package com.example.dread.client;

import com.example.dread.DreadMod;
import com.example.dread.entity.WatcherEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WatcherRenderer extends MobEntityRenderer<WatcherEntity, BipedEntityModel<WatcherEntity>> {
    private static final Identifier TEXTURE =
            new Identifier(DreadMod.MOD_ID, "textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererFactory.Context ctx) {
        // Используем ванильную гуманоидную модель (как у зомби) со своей текстурой
        super(ctx, new BipedEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f);
        this.addFeature(new WatcherEyesFeature(this));
    }

    @Override
    public Identifier getTexture(WatcherEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(WatcherEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.15f, 1.15f, 1.15f); // выше обычного моба
    }
}
