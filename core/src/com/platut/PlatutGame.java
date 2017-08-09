package com.platut;

import com.framework.BaseScene;
import com.framework.SceneManager;

public class PlatutGame extends SceneManager {
    @Override
    public void create() {
        setScene("levels/level1.tmx", BaseScene.class, EntityFactory.class);
    }
}
