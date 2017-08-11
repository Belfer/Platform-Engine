package com.engine.core.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import java.util.Map;
import java.util.Vector;

/**
 * Created by conor on 11/08/17.
 */

public class AnimationCmp implements Component {
    public Vector<Map.Entry<Integer, Integer>> frames = new Vector<>();

    public static final ComponentMapper<AnimationCmp>
            Mapper = ComponentMapper.getFor(AnimationCmp.class);
}
