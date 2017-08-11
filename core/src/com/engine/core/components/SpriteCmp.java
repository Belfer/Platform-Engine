package com.engine.core.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by conor on 11/08/17.
 */
public class SpriteCmp implements Component {
    public Vector2 origin = new Vector2();
    public Rectangle region = new Rectangle();

    public static final ComponentMapper<SpriteCmp>
            Mapper = ComponentMapper.getFor(SpriteCmp.class);
}
