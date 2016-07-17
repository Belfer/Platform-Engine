package com.platut;

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.framework.Scene;
import com.framework.SceneManager;

public class PlatutGame extends SceneManager {

	@Override
	public void create () {
		setScene (new Scene (this, new FitViewport (800/8, 480/8), 1) {
			@Override
			public void build() {
				//loadColliders ("tiles.tmx");
				loadUI ("testui.tmx");
				//loadMap ("level1.tmx");
			}
		});
	}
	
	@Override
	public void dispose () {
	}
}
