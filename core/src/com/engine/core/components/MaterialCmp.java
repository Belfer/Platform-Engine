package com.engine.core.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import java.util.ArrayList;

/**
 * Created by conor on 16/07/16.
 */
public class MaterialCmp implements Component {
    public ArrayList<String> images = new ArrayList<String>();

    public static final ComponentMapper<MaterialCmp>
            Mapper = ComponentMapper.getFor(MaterialCmp.class);
}
