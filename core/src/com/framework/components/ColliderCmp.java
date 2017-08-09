package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Shape;

/**
 * Created by conor on 21/07/16.
 */
public class ColliderCmp implements Component {
    public Shape shape = null;
    public Body body = null;
}
