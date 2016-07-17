package com.framework.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;

import java.util.Comparator;

/**
 * Created by conor on 17/07/16.
 */
public class MapRenderSystem extends SortedIteratingSystem {
    public MapRenderSystem(Family family, Comparator<Entity> comparator) {
        super(family, comparator);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

    }
}
