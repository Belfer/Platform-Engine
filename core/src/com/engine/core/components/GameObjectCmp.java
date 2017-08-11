package com.engine.core.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.engine.core.IScript;

/**
 * Created by conor on 16/07/16.
 */
public class GameObjectCmp implements Component {
    public String name = "";
    public String tag = "";
    public IScript script = null;

    public static final ComponentMapper<GameObjectCmp>
            Mapper = ComponentMapper.getFor(GameObjectCmp.class);
}
