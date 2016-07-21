package com.framework;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by conor on 21/07/16.
 */
public interface IEntityFactory {
    boolean buildEntity (Entity entity, String name, String type, Rectangle bounds, MapProperties properties);
}
