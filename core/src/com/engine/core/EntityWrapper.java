package com.engine.core;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by conor on 10/08/17.
 */
public class EntityWrapper {
    public Entity entity = new Entity();
    public String name = "";
    public String type = "";
    public Rectangle bounds = new Rectangle();
}
