package com.framework.core;

import com.badlogic.ashley.core.ComponentMapper;
import com.framework.components.ButtonCmp;
import com.framework.components.ColliderCmp;
import com.framework.components.GameObjectCmp;
import com.framework.components.MaterialCmp;
import com.framework.components.SpriteCmp;
import com.framework.components.TransformCmp;

/**
 * Created by conor on 16/07/16.
 */
public class Mappers {

    public static final ComponentMapper<GameObjectCmp>
            GAMEOBJECT = ComponentMapper.getFor(GameObjectCmp.class);

    public static final ComponentMapper<TransformCmp>
            TRANSFORM = ComponentMapper.getFor(TransformCmp.class);

    public static final ComponentMapper<MaterialCmp>
            MATERIAL = ComponentMapper.getFor(MaterialCmp.class);

    public static final ComponentMapper<SpriteCmp>
            SPRITE = ComponentMapper.getFor(SpriteCmp.class);

    public static final ComponentMapper<ColliderCmp>
            COLLIDER = ComponentMapper.getFor(ColliderCmp.class);

    public static final ComponentMapper<ButtonCmp>
            BUTTON = ComponentMapper.getFor(ButtonCmp.class);
}
