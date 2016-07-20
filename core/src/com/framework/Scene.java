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
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;
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
    World world;
    TiledMap map;

    HashMap<Integer,Collider> colliders;

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
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float viewportX = Float.parseFloat (properties.get ("viewportX", width+"", String.class));
        float viewportY = Float.parseFloat (properties.get ("viewportY", height+"", String.class));
        String[] gravity = properties.get ("gravity", "0 0", String.class).split("\\s");
        float gravityX = Float.parseFloat (gravity[0]);
        float gravityY = Float.parseFloat (gravity[1]);

        world = new World (new Vector2 (gravityX, gravityY), true);
        gameCamera = new OrthographicCamera (viewportX, viewportY);
        guiCamera = new OrthographicCamera (Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new FitViewport (viewportX, viewportY, gameCamera);

        engine = new Engine ();
        engine.addEntityListener (this);

        colliders = new HashMap<Integer, Collider> ();

        sceneLoaded = false;
        build ();
        engine.addSystem (new UpdateSystem (world));
        engine.addSystem (new RenderSystem (gameCamera, world, map));
        engine.addSystem (new LightSystem ());
        engine.addSystem (new GUISystem (guiCamera));

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
            Vector2 position = new Vector2 ();
            Vector2 center = new Vector2 ();
            PolygonShape shape = null;

            if (obj instanceof CircleMapObject) {
                Circle circle = ((CircleMapObject) obj).getCircle();
                center.x = circle.x;
                center.y = circle.y;
                position = center;

                shape = new PolygonShape ();
                shape.setRadius (circle.radius);

            } else if (obj instanceof RectangleMapObject) {
                Rectangle rectangle = ((RectangleMapObject) obj).getRectangle();
                rectangle.getCenter (center);
                position.x = rectangle.getWidth () / 2;
                position.y = rectangle.getHeight() / 2;

                shape = new PolygonShape ();
                shape.setAsBox (rectangle.getWidth()/2, rectangle.getHeight()/2);

            } else if (obj instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) obj).getPolygon();
                Rectangle bounds = polygon.getBoundingRectangle ();
                bounds.getCenter (center);

                float[] vertices = polygon.getVertices ();
                float ax = 0; float ay = 0;
                for (int i=0; i<vertices.length;) {
                    ax += vertices[i++];
                    ay += vertices[i++];
                }
                ax /= vertices.length/2;
                ay /= vertices.length/2;

                float w = tilesLayer.getTileWidth ()/2;
                float h = tilesLayer.getTileHeight ()/2;

                position.x = w - (Math.copySign (w, ax) - ax);
                position.y = h - (Math.copySign (h, ay) - ay);

                // TODO apply transform to polygon and use that to get center
                //center.x = bounds.getX() + position.x;
                //center.y = bounds.getY() + position.y;
                //center.x = polygon.ge;
                //center.y = polygon.getY();
                //System.out.println(center);

                for (int i=0; i<vertices.length;) {
                    vertices[i++] -= ax;
                    vertices[i++] -= ay;
                }

                shape = new PolygonShape ();
                shape.set (vertices);
            }

            int x = (int)(center.x / tilesLayer.getTileWidth ());
            int y = (int)(center.y / tilesLayer.getTileHeight ());
            System.out.println(x+", "+y);
            System.out.println(center);

            TiledMapTileLayer.Cell cell = tilesLayer.getCell (x, y);

            Collider collider = new Collider ();
            collider.position = position;
            collider.shape = shape;
            colliders.put(cell.getTile().getId(), collider);
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
                String type = properties.get ("type", "", String.class);

                int hashedType = type.hashCode();
                String imageSrc = properties.get ("image", "", String.class);
                String scriptSrc = properties.get ("script", "", String.class);

                addGameObject (entity, name, type, scriptSrc);
                addTransform (entity, rectangle);

                // TODO load ui widgets
                if (hashedType == TYPE_SPRITE) {
                    addSprite (entity, imageSrc);
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
            if (mapLayer instanceof TiledMapTileLayer) {
                // TODO add tile colliders
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) mapLayer;
                for (int i=0; i<tileLayer.getWidth(); i++) {
                    for (int j=0; j<tileLayer.getHeight(); j++) {
                        TiledMapTileLayer.Cell cell = tileLayer.getCell(i, j);
                        if (cell != null) {
                            Collider collider = colliders.get(cell.getTile().getId());
                            if (collider != null) {
                                // Create and add body to world
                                // Now create a BodyDefinition.  This defines the physics objects type and position in the simulation
                                BodyDef bodyDef = new BodyDef ();
                                bodyDef.type = BodyDef.BodyType.StaticBody;
                                // We are going to use 1 to 1 dimensions.  Meaning 1 in physics engine is 1 pixel
                                // Set our body to the same position as our sprite
                                bodyDef.position.set (collider.position.x+i*16, collider.position.y+j*16);

                                // Create a body in the world using our definition
                                Body body = world.createBody (bodyDef);

                                // Now define the dimensions of the physics shape
                                //PolygonShape shape = new PolygonShape();
                                // We are a box, so this makes sense, no?
                                // Basically set the physics polygon to a box with the same dimensions as our sprite
                                //shape.setAsBox(sprite.getWidth()/2, sprite.getHeight()/2);

                                // FixtureDef is a confusing expression for physical properties
                                // Basically this is where you, in addition to defining the shape of the body
                                // you also define it's properties like density, restitution and others we will see shortly
                                // If you are wondering, density and area are used to calculate over all mass
                                FixtureDef fixtureDef = new FixtureDef ();
                                fixtureDef.shape = collider.shape;
                                fixtureDef.density = 1f;

                                Fixture fixture = body.createFixture (fixtureDef);

                                // Shape is the only disposable of the lot, so get rid of it
                                //shape.dispose();
                            }
                        }
                    }
                }

            } else if (mapLayer instanceof TiledMapImageLayer) {
                // TODO add image colliders?

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

        if (!scriptSrc.isEmpty ()) {
            String[] scripts = scriptSrc.split("\\s");
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
                        object = constructor.newInstance(sceneManager, entity);
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
                    gameObject.scripts.add(script);

                } catch (ReflectionException e) {
                    String script = scr.isEmpty() ? "<empty>" : scr;
                    System.err.println("Failed to load [" + name + "] script: " + script);
                    e.printStackTrace();
                }
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

        String[] images = imageSrc.split("\\s");
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
