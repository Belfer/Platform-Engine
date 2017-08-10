package com.framework.core;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by conor on 21/07/16.
 */
public interface IEntityFactory {
    boolean buildEntity(Entity entity, String name, String type, Rectangle bounds, MapProperties properties);

    void addGameObject(Entity entity, String name, String tag, String scriptSrc);

    void addTransform(Entity entity, Rectangle bounds);

    void addCollider(Entity entity, Rectangle bounds, MapObjects colliders, int tilewidth, int tileheight);

    void addSprite(Entity entity, String imageSrc);

    void addButton(Entity entity, Rectangle bounds, String imageSrc);
}
