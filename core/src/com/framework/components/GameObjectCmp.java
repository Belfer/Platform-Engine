package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.framework.BaseScript;

import java.util.ArrayList;

/**
 * Created by conor on 16/07/16.
 */
public class GameObjectCmp implements Component {
    public String name = "";
    public String tag = "";
    public ArrayList<BaseScript> scripts = new ArrayList<BaseScript>();
}
