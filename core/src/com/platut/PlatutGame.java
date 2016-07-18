package com.platut;

import com.framework.Scene;
import com.framework.SceneManager;

public class PlatutGame extends SceneManager {

	@Override
	public void create () {
		setScene (new Scene (this, "levels/level1.tmx"));
	}
}
