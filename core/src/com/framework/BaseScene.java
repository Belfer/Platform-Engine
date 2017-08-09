package com.framework;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.framework.components.ButtonCmp;
import com.framework.components.GameObjectCmp;
import com.framework.components.MaterialCmp;
import com.framework.components.SpriteCmp;
import com.framework.systems.GUISystem;
import com.framework.systems.LightSystem;
import com.framework.systems.RenderSystem;
import com.framework.systems.UpdateSystem;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.framework.ColliderLoader.loadColliders;
import static com.framework.Constants.PixelToMeters;
import static com.framework.MapLoader.loadMap;
import static com.framework.UILoader.loadUI;

/**
 * Created by conor on 16/07/16.
 */
public class BaseScene implements IScene, EntityListener {
    protected SceneManager sceneManager;
    protected Viewport viewport;
    protected OrthographicCamera gameCamera;
    protected OrthographicCamera guiCamera;

    protected IEntityFactory entityFactory;
    protected InputMultiplexer inputMultiplexer;
    protected Engine engine;
    protected World world;
    protected Stage stage;
    protected TiledMap map;

    protected ColliderLoader colliderLoader;
    protected UILoader uiLoader;
    protected MapLoader mapLoader;

    protected boolean sceneLoaded = false;

    public BaseScene(SceneManager sceneManager, TiledMap map, Class<?> entityFactoryClass) {
        this.sceneManager = sceneManager;
        this.map = map;

        init(map.getProperties());
        entityFactory = getEntityFactory(entityFactoryClass);

        engine.addEntityListener(this);

        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void init(MapProperties properties) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float viewportX = Float.parseFloat(properties.get("viewportX", width + "", String.class));
        float viewportY = Float.parseFloat(properties.get("viewportY", height + "", String.class));
        //String[] gravity = properties.get ("gravity", "0 0", String.class).split("\\s");
        float gravityX = 0;//Float.parseFloat (gravity[0]);
        float gravityY = -100f;//Float.parseFloat (gravity[1]);

        world = new World(new Vector2(gravityX, gravityY), true);
        gameCamera = new OrthographicCamera(viewportX, viewportY);
        guiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new FitViewport(viewportX, viewportY, gameCamera);
        inputMultiplexer = new InputMultiplexer();
        stage = new Stage();
        engine = new Engine();
        colliderLoader = new ColliderLoader();
    }

    private IEntityFactory getEntityFactory(Class<?> entityFactoryClass) {
        Object object = null;
        Constructor constructor = null;
        try {
            constructor = entityFactoryClass.getDeclaredConstructor(SceneManager.class, InputMultiplexer.class, World.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            object = constructor.newInstance(sceneManager, inputMultiplexer, world);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assert (object instanceof IEntityFactory);
        return (IEntityFactory) object;
    }

    public void build() {
        sceneLoaded = false;

        MapProperties properties = map.getProperties();

        loadColliders(properties, colliderLoader);
        loadUI(properties, engine, entityFactory);
        loadMap(engine, entityFactory, map, world, colliderLoader);
        loadSystems();

        sceneManager.getAssetManager().finishLoading();
        sceneLoaded = true;

        onPostLoad();
    }

    protected void loadSystems() {
        engine.addSystem(new UpdateSystem(world));
        engine.addSystem(new RenderSystem(gameCamera, world, map, PixelToMeters));
        engine.addSystem(new LightSystem());
        engine.addSystem(new GUISystem(guiCamera, stage));
    }

    @SuppressWarnings("unchecked")
    private void onPostLoad() {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(MaterialCmp.class).get());
        for (Entity entity : entities) {
            MaterialCmp material = Mappers.MATERIAL.get(entity);

            for (String img : material.images) {
                Texture texture = sceneManager.getAssetManager().get(img, Texture.class);

                SpriteCmp sprite = entity.getComponent(SpriteCmp.class);
                if (sprite != null) {
                    sprite.sprite.setTexture(texture);
                }

                ButtonCmp button = entity.getComponent(ButtonCmp.class);
                if (button != null) {
                    //button.button.setStyle();
                }
            }
        }
    }

    @Override
    public void start() {
        for (Entity entity : engine.getEntities()) {
            GameObjectCmp gameObject = entity.getComponent(GameObjectCmp.class);
            for (BaseScript script : gameObject.scripts) {
                script.start();
            }
        }
    }

    @Override
    public void render() {
        engine.update(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void dispose() {
    }

    @Override
    public void entityAdded(Entity entity) {
        GameObjectCmp gameObject = entity.getComponent(GameObjectCmp.class);
        for (BaseScript script : gameObject.scripts) {
            script.create();

            if (sceneLoaded) {
                script.start();
            }
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
        GameObjectCmp gameObject = entity.getComponent(GameObjectCmp.class);
        for (BaseScript script : gameObject.scripts) {
            script.destroy();
        }
    }

    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    public OrthographicCamera getGUICamera() {
        return guiCamera;
    }
}
