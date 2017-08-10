package com.engine.tiled;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
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
import com.engine.core.Constants;

/**
 * Created by conor on 09/08/17.
 */
public class TiledCollider {
    public static class ColliderWrapper {
        public Shape shape;
        public Vector2 origin;
    }

    public static ColliderWrapper correctShape(MapObject obj, int tilewidth, int tileheight, float rotation) {
        ColliderWrapper wrapper = new ColliderWrapper();
        wrapper.origin = new Vector2();
        wrapper.shape = null;

        if (obj instanceof CircleMapObject) {
            Circle circle = ((CircleMapObject) obj).getCircle();
            wrapper.origin.x = circle.x * Constants.PixelToMeters;
            wrapper.origin.y = circle.y * Constants.PixelToMeters;

            wrapper.shape = new CircleShape();
            wrapper.shape.setRadius(circle.radius);

        } else if (obj instanceof EllipseMapObject) {
            Ellipse ellipse = ((EllipseMapObject) obj).getEllipse();
            Vector2 center = new Vector2();
            center.x = (ellipse.x + ellipse.width / 2 - tilewidth / 2) * Constants.PixelToMeters;
            center.y = (ellipse.y + ellipse.height / 2 - tileheight / 2) * Constants.PixelToMeters;
            wrapper.origin.x = (tilewidth / 2) * Constants.PixelToMeters;
            wrapper.origin.y = (tileheight / 2) * Constants.PixelToMeters;

            wrapper.shape = new ChainShape();
            Vector2[] vertices = new Vector2[32];
            for (int i = 0; i < 32; i++) {
                float angle = ((MathUtils.PI2) / 32) * i;
                float x, y;

                x = (ellipse.width / 2 * MathUtils.cos(angle)) * Constants.PixelToMeters + center.x;
                y = (ellipse.height / 2 * MathUtils.sin(angle)) * Constants.PixelToMeters + center.y;
                vertices[i] = new Vector2(x, y);
            }
            ((ChainShape) wrapper.shape).createChain(vertices);

        } else if (obj instanceof RectangleMapObject) {
            Rectangle rectangle = ((RectangleMapObject) obj).getRectangle();
            Vector2 center = new Vector2();
            center.x = (rectangle.x + rectangle.getWidth() / 2 - tilewidth / 2) * Constants.PixelToMeters;
            center.y = (rectangle.y + rectangle.getHeight() / 2 - tileheight / 2) * Constants.PixelToMeters;
            wrapper.origin.x = (tilewidth / 2) * Constants.PixelToMeters;
            wrapper.origin.y = (tileheight / 2) * Constants.PixelToMeters;
            float width = (rectangle.getWidth() / 2) * Constants.PixelToMeters;
            float height = (rectangle.getHeight() / 2) * Constants.PixelToMeters;

            wrapper.shape = new PolygonShape();
            ((PolygonShape) wrapper.shape).setAsBox(width, height, center, -rotation);

        } else if (obj instanceof PolygonMapObject) {
            Polygon polygon = ((PolygonMapObject) obj).getPolygon();
            polygon.setRotation(-rotation);

            float[] tmp = polygon.getTransformedVertices();
            Vector2[] vertices = new Vector2[tmp.length / 2];
            int v = 0;
            for (int i = 0; i < tmp.length; i += 2) {
                float x = (tmp[i] - polygon.getX()) * Constants.PixelToMeters;
                float y = (tmp[i + 1] - polygon.getY()) * Constants.PixelToMeters;
                vertices[v++] = new Vector2(x, y);
            }

            Vector2 average = new Vector2();
            for (Vector2 i : vertices) average.add(i);
            average.scl(1f / vertices.length);

            if (average.x < 0 && average.y > 0) {
                for (Vector2 i : vertices) i.x += tilewidth * Constants.PixelToMeters;
            } else if (average.x > 0 && average.y < 0) {
                for (Vector2 i : vertices) i.y += tileheight * Constants.PixelToMeters;
            } else if (average.x < 0 && average.y < 0) {
                for (Vector2 i : vertices) {
                    i.x += tilewidth * Constants.PixelToMeters;
                    i.y += tileheight * Constants.PixelToMeters;
                }
            }

            wrapper.origin.x = (polygon.getX() - (int) (polygon.getX() / tilewidth) * tilewidth) * Constants.PixelToMeters;
            wrapper.origin.y = (polygon.getY() - (int) (polygon.getY() / tileheight) * tileheight) * Constants.PixelToMeters;

            wrapper.shape = new PolygonShape();
            ((PolygonShape) wrapper.shape).set(vertices);
        }

        return wrapper;
    }
}
