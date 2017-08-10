package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.framework.core.IScript;

import java.util.ArrayList;

/**
 * Created by conor on 16/07/16.
 */
public class GameObjectCmp implements Component {
    public String name = "";
    public String tag = "";
    public ArrayList<IScript> scripts = new ArrayList<>();

    public static final ComponentMapper<GameObjectCmp>
            Mapper = ComponentMapper.getFor(GameObjectCmp.class);
}
