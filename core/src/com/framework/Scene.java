package com.framework;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.framework.components.CGameObject;
import com.framework.components.CMaterial;
import com.framework.components.CTexture;
import com.framework.components.CTransform;
import com.framework.systems.GUIRenderSystem;
import com.framework.systems.LightRenderSystem;
import com.framework.systems.ScriptRenderSystem;
import com.framework.systems.ScriptUpdateSystem;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by conor on 16/07/16.
 */
public abstract class Scene implements EntityListener
{
    SceneManager sceneManager;
    Viewport viewport;
    OrthographicCamera guiCamera;
    Engine engine;

    World world;
    Box2DDebugRenderer debugRenderer;

    TiledMap map;
    TiledMapRenderer renderer;

    HashMap<Integer,Shape2D> colliders;

    boolean assetsLoaded = false;

    //static final int TYPE_ENTITY = "entity".hashCode();
    static final int TYPE_SPRITE = "sprite".hashCode();
    static final int TYPE_BUTTON = "button".hashCode();
    static final int TYPE_TOGGLE = "toggle".hashCode();

    public Scene (SceneManager sceneManager, Viewport viewport, float unitScale)
    {
        this.sceneManager = sceneManager;
        this.viewport = viewport;
        guiCamera = new OrthographicCamera (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        engine = new Engine ();
        engine.addEntityListener (this);
        engine.addSystem (new ScriptUpdateSystem());
        engine.addSystem (new ScriptRenderSystem((OrthographicCamera) viewport.getCamera()));
        //engine.addSystem (new LightRenderSystem());
        //engine.addSystem (new GUIRenderSystem(guiCamera));

        world = new World (new Vector2 (0, 0), true);
        debugRenderer = new Box2DDebugRenderer (true, true, true, true, true, true);

        renderer = new OrthogonalTiledMapRenderer (map, unitScale);

        colliders = new HashMap<Integer, Shape2D> ();

        assetsLoaded = false;
        build ();
        sceneManager.getAssetManager().finishLoading();
        assetsLoaded = true;

        onPostLoad();
    }

    public abstract void build ();

    protected void loadMap (String filename)
    {
        TiledMap tiledMap = new TmxMapLoader ().load (filename);

        for (MapLayer mapLayer : tiledMap.getLayers()) {
            if (mapLayer instanceof TiledMapTileLayer || mapLayer instanceof TiledMapImageLayer) {
                // TODO add tile colliders
            } else {
                for (MapObject obj : mapLayer.getObjects()) {
                    // TODO add entity objects
                }
            }
        }
    }

    protected void loadColliders (String filename)
    {
        TiledMap tiledMap = new TmxMapLoader ().load (filename);
        //TiledMapTileSets tileSets = tiledMap.getTileSets();
        MapLayers mapLayers = tiledMap.getLayers();

        MapLayer colliderLayer = mapLayers.get ("colliders");
        TiledMapTileLayer tilesLayer = (TiledMapTileLayer) mapLayers.get ("tiles");

        for (MapObject obj : colliderLayer.getObjects()) {
            Vector2 center = new Vector2 ();
            Shape2D shape = null;

            if (obj instanceof CircleMapObject) {
                Circle circle = ((CircleMapObject) obj).getCircle();
                center.x = circle.x;
                center.y = circle.y;
                shape = circle;

            } else if (obj instanceof RectangleMapObject) {
                Rectangle rectangle = ((RectangleMapObject) obj).getRectangle();
                rectangle.getCenter (center);
                shape = rectangle;

            } else if (obj instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) obj).getPolygon();
                polygon.getBoundingRectangle().getCenter (center);
                shape = polygon;
            }

            int x = (int)(center.x / tilesLayer.getTileWidth ());
            int y = (int)(center.y / tilesLayer.getTileHeight ());

            TiledMapTileLayer.Cell cell = tilesLayer.getCell (x, y);
            colliders.put(cell.getTile().getId(), shape);
        }
    }

    protected void loadUI (String filename)
    {
        TiledMap tiledMap = new TmxMapLoader ().load (filename);
        MapLayers mapLayers = tiledMap.getLayers();
        MapLayer uiLayer = mapLayers.get ("ui");

        for (MapObject obj : uiLayer.getObjects()) {
            if (obj instanceof RectangleMapObject) {
                Entity entity = new Entity ();

                Rectangle rectangle = ((RectangleMapObject) obj).getRectangle ();
                MapProperties properties = obj.getProperties();
                String name = obj.getName ();
                String type = (String) properties.get ("type");

                int hashedType = type.hashCode();
                String image = (String) properties.get ("image");
                String script = (String) properties.get ("script");

                addGameObject (entity, name, type, script);
                addTransform (entity, rectangle);

                // TODO load ui widgets
                if (hashedType == TYPE_SPRITE) {
                    addSprite (entity, image);
                }
                else if (hashedType == TYPE_BUTTON) {

                }
                else if (hashedType == TYPE_TOGGLE) {
                    String off = (String) properties.get ("off");
                    String on = (String) properties.get ("on");
                    //Boolean state = (Boolean) properties.get ("state");
                }

                engine.addEntity (entity);
            }
        }
    }

    private void addGameObject (Entity entity, String name, String tag, String scriptSrc)
    {
        CGameObject gameObject = new CGameObject();
        gameObject.name = name;
        gameObject.tag = tag;

        String[] scripts = new String[0];
        if (scriptSrc != null) {
            scripts = scriptSrc.split("\\s");
        }

        for (String scr : scripts) {
            try {
                Class scrClass = ClassReflection.forName(scr);
                Constructor constructor = null;
                try {
                    constructor = scrClass.getConstructor(SceneManager.class, Entity.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                assert (constructor != null);

                Object object = null;
                try {
                    object = constructor.newInstance (sceneManager, entity);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                assert (object != null);
                assert (object instanceof Script);

                Script script = (Script) object;
                gameObject.scripts.add (script);

            } catch (ReflectionException e) {
                e.printStackTrace();
            }
        }

        entity.add (gameObject);
    }

    private void addTransform (Entity entity, Rectangle bounds)
    {
        CTransform transform = new CTransform();
        transform.bounds = bounds;
        bounds.getCenter (transform.position);
        entity.add (transform);
    }

    private void addSprite (Entity entity, String imageSrc)
    {
        CMaterial material = new CMaterial();

        String[] images = new String[0];
        if (imageSrc != null) {
            images = imageSrc.split("\\s");
        }

        for (String img : images) {
            sceneManager.getAssetManager().load (img, Texture.class);
            material.images.add (img);
        }

        entity.add (material);
    }

    private void onPostLoad ()
    {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(CMaterial.class).get());
        for (Entity entity : entities) {
            CMaterial material = Mappers.MATERIAL.get(entity);

            for (String img : material.images) {
                Texture texture = sceneManager.getAssetManager().get(img, Texture.class);
                entity.add (new CTexture (texture));
            }
        }

        for (Entity entity : engine.getEntities ()) {
            CGameObject gameObject = entity.getComponent (CGameObject.class);
            for (Script script : gameObject.scripts) {
                script.start ();
            }
        }
    }

    public void update ()
    {
        engine.update (Gdx.graphics.getDeltaTime ());
        //debugRenderer.render(world, viewport.getCamera ().combined);
    }

    public void dispose ()
    {

    }

    @Override
    public void entityAdded (Entity entity)
    {
        CGameObject gameObject = entity.getComponent(CGameObject.class);
        for (Script script : gameObject.scripts) {
            script.create ();

            if (assetsLoaded) {
                script.start ();
            }
        }
    }

    @Override
    public void entityRemoved (Entity entity)
    {
        CGameObject gameObject = entity.getComponent(CGameObject.class);
        for (Script script : gameObject.scripts) {
            script.destroy ();
        }
    }
}
