package com.framework.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.framework.Mappers;
import com.framework.components.CMaterial;
import com.framework.components.CTexture;
import com.framework.components.CTransform;

/**
 * Created by conor on 17/07/16.
 */
public class GUIRenderSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    private OrthographicCamera guiCamera;
    private SpriteBatch batch;

    public GUIRenderSystem(OrthographicCamera guiCamera) {
        this.guiCamera = guiCamera;
        batch = new SpriteBatch();
    }

    @Override
    public void addedToEngine (Engine engine) {
        entities = engine.getEntitiesFor(Family.all(CTransform.class, CMaterial.class, CTexture.class).get());
    }

    @Override
    public void removedFromEngine (Engine engine) { }

    @Override
    public void update (float deltaTime) {
        CTransform transform;
        CMaterial material;
        CTexture texture;

        guiCamera.update();

        batch.begin();
        //batch.setProjectionMatrix(guiCamera.combined);

        for (Entity entity : entities) {

            transform = Mappers.TRANSFORM.get(entity);
            material = Mappers.MATERIAL.get(entity);
            texture = Mappers.TEXTURE.get(entity);

            batch.draw(texture.getTexture(), transform.position.x, transform.position.y);

            //for(TextureRegion region : material.regions) {
            //    batch.draw(region, transform.position.x, transform.position.y);
            //}
        }

        batch.end();
    }
}
