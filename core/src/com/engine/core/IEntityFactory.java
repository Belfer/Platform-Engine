package com.engine.core;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by conor on 21/07/16.
 */
public interface IEntityFactory {
    void init(SceneManager sceneManager, InputMultiplexer inputMultiplexer, World world);

    Entity buildEntity(EntityWrapper entityWrapper);
}
