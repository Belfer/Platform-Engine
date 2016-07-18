package com.framework.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.framework.Mappers;
import com.framework.Script;
import com.framework.components.CGameObject;
import com.framework.components.CMaterial;
import com.framework.components.CSprite;
import com.framework.components.CTransform;

/**
 * Created by conor on 17/07/16.
 */
public class GUISystem extends EntitySystem {
    ImmutableArray<Entity> entities;
    ImmutableArray<Entity> gameObjectEntities;

    OrthographicCamera camera;
    SpriteBatch batch;

    public GUISystem (OrthographicCamera camera) {
        this.camera = camera;
        batch = new SpriteBatch ();
    }

    @Override
    public void addedToEngine (Engine engine) {
        entities = engine.getEntitiesFor(Family.all(CTransform.class, CMaterial.class, CSprite.class).get());
        gameObjectEntities = engine.getEntitiesFor(Family.all(CGameObject.class, CTransform.class).get());
    }

    @Override
    public void removedFromEngine (Engine engine) { }

    @Override
    public void update (float deltaTime) {
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Entity entity : gameObjectEntities) {
            CGameObject gameObject = Mappers.GAMEOBJECT.get(entity);

            for (Script script : gameObject.scripts) {
                script.drawGUI (batch);
            }
        }

        batch.end();
    }
}
