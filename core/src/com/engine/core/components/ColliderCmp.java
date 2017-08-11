package com.engine.core.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by conor on 21/07/16.
 */
public class ColliderCmp implements Component {
    public Body body = null;

    public static final ComponentMapper<ColliderCmp>
            Mapper = ComponentMapper.getFor(ColliderCmp.class);
}
