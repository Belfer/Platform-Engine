package com.framework;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.framework.components.CGameObject;
import com.framework.components.CMaterial;
import com.framework.components.CSprite;
import com.framework.components.CTransform;
import com.framework.systems.GUISystem;
import com.framework.systems.LightSystem;
import com.framework.systems.RenderSystem;
import com.framework.systems.UpdateSystem;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by conor on 16/07/16.
 */
public class Scene implements EntityListener
{
    SceneManager sceneManager;
    Viewport viewport;
    OrthographicCamera gameCamera;
    OrthographicCamera guiCamera;
    Engine engine;
    TiledMap map;

    HashMap<Integer,Shape2D> colliders;

    boolean sceneLoaded = false;

    //static final int TYPE_ENTITY = "entity".hashCode();
    static final int TYPE_SPRITE = "sprite".hashCode();
    static final int TYPE_BUTTON = "button".hashCode();
    static final int TYPE_TOGGLE = "toggle".hashCode();

    public Scene (SceneManager sceneManager, String filepath)
    {
        this.sceneManager = sceneManager;
        map = new TmxMapLoader ().load (filepath);

        MapProperties properties = map.getProperties();
        float viewportX = Float.parseFloat (properties.get ("viewportX", "800f", String.class));
        float viewportY = Float.parseFloat (properties.get ("viewportY", "600f", String.class));

        gameCamera = new OrthographicCamera (viewportX, viewportY);
        guiCamera = new OrthographicCamera (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new FitViewport (viewportX, viewportY, gameCamera);

        engine = new Engine ();
        engine.addEntityListener (this);

        colliders = new HashMap<Integer, Shape2D> ();

        sceneLoaded = false;
        build ();
        engine.addSystem (new UpdateSystem(new Vector2 (0, 0)));
        engine.addSystem (new RenderSystem(gameCamera, map));
        engine.addSystem (new LightSystem());
        engine.addSystem (new GUISystem(guiCamera));

        sceneManager.getAssetManager().finishLoading();
        sceneLoaded = true;

        onPostLoad();
    }

    public void build ()
    {
        MapProperties properties = map.getProperties();
        String collider = properties.get ("collider", "", String.class);
        String ui = properties.get ("ui", "", String.class);

        loadColliders (collider);
        loadUI (ui);
        loadMap ();
    }

    protected void loadColliders (String filename)
    {
        TiledMap colliderMap = new TmxMapLoader ().load (filename);
        //TiledMapTileSets tileSets = tiledMap.getTileSets();
        MapLayers mapLayers = colliderMap.getLayers();

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
        TiledMap uiMap = new TmxMapLoader ().load (filename);
        MapLayers mapLayers = uiMap.getLayers();
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

    protected void loadMap ()
    {
        for (MapLayer mapLayer : map.getLayers()) {
            if (mapLayer instanceof TiledMapTileLayer || mapLayer instanceof TiledMapImageLayer) {
                // TODO add tile colliders
            } else {
                for (MapObject obj : mapLayer.getObjects()) {
                    // TODO add entity objects
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
                    addSprite (entity, image);

                    engine.addEntity (entity);
                }
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
        transform.position.x = bounds.x;
        transform.position.y = bounds.y;
        transform.position.z = 0;
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
                CSprite sprite = new CSprite();
                sprite.sprite = new Sprite (sceneManager.getAssetManager().get(img, Texture.class));
                entity.add (sprite);
            }
        }
    }

    public void start ()
    {
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

            if (sceneLoaded) {
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

    public OrthographicCamera getGameCamera () { return gameCamera; }
    public OrthographicCamera getGUICamera () { return guiCamera; }
}
