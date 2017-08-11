package com.engine.core.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by conor on 16/07/16.
 */
public class MaterialCmp implements Component {
    public String texture = "";
    public Color color = Color.WHITE;

    public static final ComponentMapper<MaterialCmp>
            Mapper = ComponentMapper.getFor(MaterialCmp.class);
}
