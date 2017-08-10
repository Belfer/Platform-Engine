package com.platut;

import com.framework.core.BaseScene;
import com.framework.core.SceneManager;

public class PlatutGame extends SceneManager {
    @Override
    public void create() {
        setScene("levels/level1.tmx", BaseScene.class, EntityFactory.class);
    }
}
