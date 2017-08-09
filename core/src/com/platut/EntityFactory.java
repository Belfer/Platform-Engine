package com.platut;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.framework.BaseEntityFactory;
import com.framework.SceneManager;

/**
 * Created by conor on 09/08/17.
 */
public class EntityFactory extends BaseEntityFactory {
    public EntityFactory(SceneManager sceneManager, InputMultiplexer inputMultiplexer, World world) {
        super(sceneManager, inputMultiplexer, world);
    }

    @Override
    public boolean buildEntity(Entity entity, String name, String type, Rectangle bounds, MapProperties properties) {
        super.buildEntity(entity, name, type, bounds, properties);
        return true;
    }
}
