package com.framework.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.World;
import com.framework.Mappers;
import com.framework.Script;
import com.framework.components.CGameObject;
import com.framework.components.CTransform;

/**
 * Created by conor on 16/07/16.
 */
public class UpdateSystem extends EntitySystem {
    private ImmutableArray<Entity> gameObjectEntities;

    World world;

    public UpdateSystem (World world)
    {
        this.world = world;
    }

    public void addedToEngine(Engine engine) {
        gameObjectEntities = engine.getEntitiesFor(Family.all(CGameObject.class, CTransform.class).get());
    }

    public void update(float deltaTime) {
        for (Entity entity : gameObjectEntities) {
            CGameObject gameObject = Mappers.GAMEOBJECT.get(entity);

            for (Script script : gameObject.scripts) {
                script.update (Gdx.graphics.getDeltaTime ());
            }
        }
    }
}