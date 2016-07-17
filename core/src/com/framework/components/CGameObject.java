package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.framework.Script;

import java.util.ArrayList;

/**
 * Created by conor on 16/07/16.
 */
public class CGameObject implements Component {
    public String name = "";
    public String tag = "";
    public ArrayList<Script> scripts = new ArrayList<Script>();
}
