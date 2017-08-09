package com.framework;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.framework.map.SceneMapLoader;

import java.util.HashMap;

import static com.framework.Constants.PixelToMeters;

/**
 * Created by conor on 09/08/17.
 */

public class ColliderLoader {
    public static class ColliderWrapper {
        public Shape shape;
        public Vector2 offset;
    }

    public HashMap<Integer, ColliderWrapper> colliders;

    public ColliderLoader() {
        colliders = new HashMap<>();
    }

    public static void loadColliders(MapProperties properties, ColliderLoader loader) {
        String filename = properties.get("collider", "", String.class);

        if (!filename.isEmpty()) {
            TiledMap colliderMap = new SceneMapLoader().load(filename);
            MapProperties mapProperties = colliderMap.getProperties();
            MapLayers mapLayers = colliderMap.getLayers();

            int tilewidth = mapProperties.get("tilewidth", 0, Integer.class);
            int tileheight = mapProperties.get("tileheight", 0, Integer.class);

            MapLayer colliderLayer = mapLayers.get("colliders");

            System.out.println(filename);

            for (MapObject obj : colliderLayer.getObjects()) {
                MapProperties layerProp = obj.getProperties();
                float rotation = layerProp.get("rotation", 0f, Float.class);

                ColliderWrapper wrapper = correctShape(obj, tilewidth, tileheight, rotation);

                int tileId = obj.getProperties().get("tileId", 0, Integer.class);
                loader.colliders.put(tileId, wrapper);
            }
        }
    }

    public static ColliderWrapper correctShape(MapObject obj, int tilewidth, int tileheight, float rotation) {
        ColliderWrapper wrapper = new ColliderWrapper();
        wrapper.offset = new Vector2();
        wrapper.shape = null;

        if (obj instanceof CircleMapObject) {
            Circle circle = ((CircleMapObject) obj).getCircle();
            wrapper.offset.x = circle.x * PixelToMeters;
            wrapper.offset.y = circle.y * PixelToMeters;

            wrapper.shape = new CircleShape();
            wrapper.shape.setRadius(circle.radius);

        } else if (obj instanceof EllipseMapObject) {
            Ellipse ellipse = ((EllipseMapObject) obj).getEllipse();
            Vector2 center = new Vector2();
            center.x = (ellipse.x + ellipse.width / 2 - tilewidth / 2) * PixelToMeters;
            center.y = (ellipse.y + ellipse.height / 2 - tileheight / 2) * PixelToMeters;
            wrapper.offset.x = (tilewidth / 2) * PixelToMeters;
            wrapper.offset.y = (tileheight / 2) * PixelToMeters;

            wrapper.shape = new ChainShape();
            Vector2[] vertices = new Vector2[32];
            for (int i = 0; i < 32; i++) {
                float angle = ((MathUtils.PI2) / 32) * i;
                float x, y;

                x = (ellipse.width / 2 * MathUtils.cos(angle)) * PixelToMeters + center.x;
                y = (ellipse.height / 2 * MathUtils.sin(angle)) * PixelToMeters + center.y;
                vertices[i] = new Vector2(x, y);
            }
            ((ChainShape) wrapper.shape).createChain(vertices);

        } else if (obj instanceof RectangleMapObject) {
            Rectangle rectangle = ((RectangleMapObject) obj).getRectangle();
            Vector2 center = new Vector2();
            center.x = (rectangle.x + rectangle.getWidth() / 2 - tilewidth / 2) * PixelToMeters;
            center.y = (rectangle.y + rectangle.getHeight() / 2 - tileheight / 2) * PixelToMeters;
            wrapper.offset.x = (tilewidth / 2) * PixelToMeters;
            wrapper.offset.y = (tileheight / 2) * PixelToMeters;
            float width = (rectangle.getWidth() / 2) * PixelToMeters;
            float height = (rectangle.getHeight() / 2) * PixelToMeters;

            wrapper.shape = new PolygonShape();
            ((PolygonShape) wrapper.shape).setAsBox(width, height, center, -rotation);

        } else if (obj instanceof PolygonMapObject) {
            Polygon polygon = ((PolygonMapObject) obj).getPolygon();
            polygon.setRotation(-rotation);

            float[] tmp = polygon.getTransformedVertices();
            Vector2[] vertices = new Vector2[tmp.length / 2];
            int v = 0;
            for (int i = 0; i < tmp.length; i += 2) {
                float x = (tmp[i] - polygon.getX()) * PixelToMeters;
                float y = (tmp[i + 1] - polygon.getY()) * PixelToMeters;
                vertices[v++] = new Vector2(x, y);
            }

            Vector2 average = new Vector2();
            for (Vector2 i : vertices) average.add(i);
            average.scl(1f / vertices.length);

            if (average.x < 0 && average.y > 0) {
                for (Vector2 i : vertices) i.x += tilewidth * PixelToMeters;
            } else if (average.x > 0 && average.y < 0) {
                for (Vector2 i : vertices) i.y += tileheight * PixelToMeters;
            } else if (average.x < 0 && average.y < 0) {
                for (Vector2 i : vertices) {
                    i.x += tilewidth * PixelToMeters;
                    i.y += tileheight * PixelToMeters;
                }
            }

            wrapper.offset.x = (polygon.getX() - (int) (polygon.getX() / tilewidth) * tilewidth) * PixelToMeters;
            wrapper.offset.y = (polygon.getY() - (int) (polygon.getY() / tileheight) * tileheight) * PixelToMeters;

            wrapper.shape = new PolygonShape();
            ((PolygonShape) wrapper.shape).set(vertices);
        }

        return wrapper;
    }
}
