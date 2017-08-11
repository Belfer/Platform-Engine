package com.engine.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.engine.core.IEntityFactory;
import com.engine.core.IScene;
import com.engine.core.SceneManager;
import com.engine.core.components.GameObjectCmp;
import com.engine.core.components.MaterialCmp;
import com.engine.core.systems.GUISystem;
import com.engine.core.systems.LightSystem;
import com.engine.core.systems.RenderSystem;
import com.engine.core.systems.UpdateSystem;

import java.util.HashMap;

import static com.engine.core.Constants.PixelToMeters;
import static com.engine.tiled.TiledUtil.buildCollider;

/**
 * Created by conor on 16/07/16.
 */
public class TiledScene implements IScene, EntityListener {
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
    protected MapProperties properties;

    protected boolean sceneLoaded = false;

    private HashMap<Integer, TiledCollider> colliders;

    @Override
    public void init(String filename, SceneManager sceneManager, Class<?> entityFactoryClass) {
        this.sceneManager = sceneManager;
        stage = new Stage();
        colliders = new HashMap<>();

        engine = new Engine();
        engine.addEntityListener(this);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMultiplexer);

        map = new TmxMapLoader().load(filename);
        properties = map.getProperties();

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

        entityFactory = newEntityFactory(entityFactoryClass);
        entityFactory.init(sceneManager, inputMultiplexer, world);
    }

    private IEntityFactory newEntityFactory(Class<?> entityFactoryClass) {
        Object object = null;
        try {
            object = entityFactoryClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assert (object instanceof IEntityFactory);
        return (IEntityFactory) object;
    }

    @Override
    public void build() {
        sceneLoaded = false;

        loadSystems();
        loadColliders();
        loadMap();
        loadAssets();

        sceneManager.getAssetManager().finishLoading();
        sceneLoaded = true;
    }

    private void loadSystems() {
        engine.addSystem(new UpdateSystem(world));
        engine.addSystem(new RenderSystem(sceneManager.getAssetManager(), gameCamera, world, map));
        engine.addSystem(new LightSystem());
        engine.addSystem(new GUISystem(guiCamera, stage));
    }

    private void loadColliders() {
        String filename = properties.get("collider", "", String.class);

        if (!filename.isEmpty()) {
            TiledMap colliderMap = new TmxMapPatchLoader().load(filename);
            MapProperties mapProperties = colliderMap.getProperties();
            MapLayers mapLayers = colliderMap.getLayers();

            int tilewidth = mapProperties.get("tilewidth", 0, Integer.class);
            int tileheight = mapProperties.get("tileheight", 0, Integer.class);

            MapLayer colliderLayer = mapLayers.get("colliders");

            for (MapObject obj : colliderLayer.getObjects()) {
                MapProperties layerProp = obj.getProperties();
                float rotation = layerProp.get("rotation", 0f, Float.class);

                TiledCollider wrapper = buildCollider(obj, tilewidth, tileheight, rotation);

                int tileId = obj.getProperties().get("tileId", 0, Integer.class);
                colliders.put(tileId, wrapper);
            }
        }
    }

    private void loadMap() {
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                // Load tile colliders
                loadTileColliders((TiledMapTileLayer) layer);

            } else if (layer instanceof TiledMapImageLayer) {
                // Load image data
                loadImageLayer((TiledMapImageLayer) layer);

            } else {
                // Load game entities
                loadEntities(layer);
            }
        }
    }

    private void loadTileColliders(TiledMapTileLayer layer) {
        for (int i = 0; i < layer.getWidth(); i++) {
            for (int j = 0; j < layer.getHeight(); j++) {

                TiledMapTileLayer.Cell cell = layer.getCell(i, j);
                if (cell != null) {
                    TiledCollider collider = colliders.get(cell.getTile().getId());

                    if (collider != null) {
                        if (collider.shape != null) {
                            BodyDef bodyDef = new BodyDef();
                            bodyDef.type = BodyDef.BodyType.StaticBody;

                            int tilewidth = 16;
                            int tileheight = 16;
                            float x = (i * tilewidth * PixelToMeters) + collider.origin.x;
                            float y = (j * tileheight * PixelToMeters) + collider.origin.y;
                            bodyDef.position.set(x, y);

                            Body body = world.createBody(bodyDef);

                            FixtureDef fixtureDef = new FixtureDef();
                            fixtureDef.shape = collider.shape;
                            fixtureDef.density = 1f;
                            fixtureDef.friction = 0.5f;
                            fixtureDef.restitution = 0f;
                            body.createFixture(fixtureDef);
                        }
                    }
                }
            }
        }
    }

    private void loadImageLayer(TiledMapImageLayer layer) {
        // TODO add image colliderLoader?
    }

    private void loadEntities(MapLayer layer) {
        for (MapObject obj : layer.getObjects()) {
            TiledEntityWrapper wrapper = new TiledEntityWrapper();

            wrapper.bounds = ((RectangleMapObject) obj).getRectangle();
            wrapper.properties = obj.getProperties();
            wrapper.name = obj.getName();
            wrapper.type = (String) wrapper.properties.get("type");

            entityFactory.buildEntity(wrapper);
            engine.addEntity(wrapper.entity);
        }
    }

    private void loadAssets() {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(MaterialCmp.class).get());
        for (Entity entity : entities) {
            MaterialCmp materialCmp = MaterialCmp.Mapper.get(entity);
            if (materialCmp != null && !materialCmp.texture.isEmpty()) {
                sceneManager.getAssetManager().load(materialCmp.texture, Texture.class);
            }
        }
    }

    @Override
    public void start() {
        for (Entity entity : engine.getEntities()) {
            GameObjectCmp gameObjectCmp = entity.getComponent(GameObjectCmp.class);
            assert (gameObjectCmp != null);

            if (gameObjectCmp.script != null) {
                gameObjectCmp.script.start();
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
        GameObjectCmp gameObjectCmp = entity.getComponent(GameObjectCmp.class);
        assert (gameObjectCmp != null);

        if (gameObjectCmp.script != null) {
            gameObjectCmp.script.create();
            if (sceneLoaded) {
                gameObjectCmp.script.start();
            }
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
        GameObjectCmp gameObjectCmp = entity.getComponent(GameObjectCmp.class);
        assert (gameObjectCmp != null);

        if (gameObjectCmp.script != null) {
            gameObjectCmp.script.destroy();
        }
    }

    @Override
    public OrthographicCamera getGameCamera() {
        return gameCamera;
    }

    @Override
    public OrthographicCamera getGUICamera() {
        return guiCamera;
    }
}
