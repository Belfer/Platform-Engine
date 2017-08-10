package com.engine.tiled;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;

/**
 * Created by conor on 21/07/16.
 */
public class TmxMapPatchLoader extends TmxMapLoader {

    /**
     * Loads the specified tileset data, adding it to the collection of the specified tiled, given the XML element, the tmxFile and
     * an {@link ImageResolver} used to retrieve the tileset Textures.
     * <p>
     * <p>
     * Default tileset's property keys that are loaded by default are:
     * </p>
     * <p>
     * <ul>
     * <li><em>firstgid</em>, (int, defaults to 1) the first valid global id used for tile numbering</li>
     * <li><em>imagesource</em>, (String, defaults to empty string) the tileset source image filename</li>
     * <li><em>imagewidth</em>, (int, defaults to 0) the tileset source image width</li>
     * <li><em>imageheight</em>, (int, defaults to 0) the tileset source image height</li>
     * <li><em>tilewidth</em>, (int, defaults to 0) the tile width</li>
     * <li><em>tileheight</em>, (int, defaults to 0) the tile height</li>
     * <li><em>margin</em>, (int, defaults to 0) the tileset margin</li>
     * <li><em>spacing</em>, (int, defaults to 0) the tileset spacing</li>
     * </ul>
     * <p>
     * <p>
     * The values are extracted from the specified Tmx file, if a value can't be found then the default is used.
     * </p>
     *
     * @param map           the Map whose tilesets collection will be populated
     * @param element       the XML element identifying the tileset to load
     * @param tmxFile       the Filehandle of the tmx file
     * @param imageResolver the {@link ImageResolver}
     */
    protected void loadTileSet(TiledMap map, XmlReader.Element element, FileHandle tmxFile, ImageResolver imageResolver) {
        if (element.getName().equals("tileset")) {
            String name = element.get("name", null);
            int firstgid = element.getIntAttribute("firstgid", 1);
            int tilewidth = element.getIntAttribute("tilewidth", 0);
            int tileheight = element.getIntAttribute("tileheight", 0);
            int spacing = element.getIntAttribute("spacing", 0);
            int margin = element.getIntAttribute("margin", 0);
            String source = element.getAttribute("source", null);

            int offsetX = 0;
            int offsetY = 0;

            String imageSource = "";
            int imageWidth = 0, imageHeight = 0;

            FileHandle image = null;
            if (source != null) {
                FileHandle tsx = getRelativeFileHandle(tmxFile, source);
                try {
                    element = xml.parse(tsx);
                    name = element.get("name", null);
                    tilewidth = element.getIntAttribute("tilewidth", 0);
                    tileheight = element.getIntAttribute("tileheight", 0);
                    spacing = element.getIntAttribute("spacing", 0);
                    margin = element.getIntAttribute("margin", 0);
                    XmlReader.Element offset = element.getChildByName("tileoffset");
                    if (offset != null) {
                        offsetX = offset.getIntAttribute("x", 0);
                        offsetY = offset.getIntAttribute("y", 0);
                    }
                    XmlReader.Element imageElement = element.getChildByName("image");
                    if (imageElement != null) {
                        imageSource = imageElement.getAttribute("source");
                        imageWidth = imageElement.getIntAttribute("width", 0);
                        imageHeight = imageElement.getIntAttribute("height", 0);
                        image = getRelativeFileHandle(tsx, imageSource);
                    }
                } catch (IOException e) {
                    throw new GdxRuntimeException("Error parsing external tileset.");
                }
            } else {
                XmlReader.Element offset = element.getChildByName("tileoffset");
                if (offset != null) {
                    offsetX = offset.getIntAttribute("x", 0);
                    offsetY = offset.getIntAttribute("y", 0);
                }
                XmlReader.Element imageElement = element.getChildByName("image");
                if (imageElement != null) {
                    imageSource = imageElement.getAttribute("source");
                    imageWidth = imageElement.getIntAttribute("width", 0);
                    imageHeight = imageElement.getIntAttribute("height", 0);
                    image = getRelativeFileHandle(tmxFile, imageSource);
                }
            }

            TiledMapTileSet tileset = new TiledMapTileSet();
            tileset.setName(name);
            tileset.getProperties().put("firstgid", firstgid);

            if (image != null) {
                TextureRegion texture = imageResolver.getImage(image.path());

                MapProperties props = tileset.getProperties();
                props.put("imagesource", imageSource);
                props.put("imagewidth", imageWidth);
                props.put("imageheight", imageHeight);
                props.put("tilewidth", tilewidth);
                props.put("tileheight", tileheight);
                props.put("margin", margin);
                props.put("spacing", spacing);

                int stopWidth = texture.getRegionWidth() - tilewidth;
                int stopHeight = texture.getRegionHeight() - tileheight;

                int id = firstgid;

                for (int y = margin; y <= stopHeight; y += tileheight + spacing) {
                    for (int x = margin; x <= stopWidth; x += tilewidth + spacing) {
                        TextureRegion tileRegion = new TextureRegion(texture, x, y, tilewidth, tileheight);
                        TiledMapTile tile = new StaticTiledMapTile(tileRegion);
                        tile.setId(id);
                        tile.setOffsetX(offsetX);
                        tile.setOffsetY(flipY ? -offsetY : offsetY);
                        tileset.putTile(id++, tile);
                    }
                }
            } else {
                Array<XmlReader.Element> tileElements = element.getChildrenByName("tile");
                for (XmlReader.Element tileElement : tileElements) {
                    XmlReader.Element imageElement = tileElement.getChildByName("image");
                    if (imageElement != null) {
                        imageSource = imageElement.getAttribute("source");
                        imageWidth = imageElement.getIntAttribute("width", 0);
                        imageHeight = imageElement.getIntAttribute("height", 0);
                        image = getRelativeFileHandle(tmxFile, imageSource);
                    }
                    TextureRegion texture = imageResolver.getImage(image.path());
                    TiledMapTile tile = new StaticTiledMapTile(texture);
                    tile.setId(firstgid + tileElement.getIntAttribute("id"));
                    tile.setOffsetX(offsetX);
                    tile.setOffsetY(flipY ? -offsetY : offsetY);
                    tileset.putTile(tile.getId(), tile);
                }
            }
            Array<XmlReader.Element> tileElements = element.getChildrenByName("tile");

            Array<AnimatedTiledMapTile> animatedTiles = new Array<>();

            MapLayer collidersLayer = new MapLayer();
            collidersLayer.setName("colliders");
            for (XmlReader.Element tileElement : tileElements) {
                int localtid = tileElement.getIntAttribute("id", 0);
                TiledMapTile tile = tileset.getTile(firstgid + localtid);
                if (tile != null) {
                    XmlReader.Element objectgroupElement = tileElement.getChildByName("objectgroup");
                    if (objectgroupElement != null) {
                        loadObjectGroup(collidersLayer, objectgroupElement, tile.getId());
                    }

                    XmlReader.Element animationElement = tileElement.getChildByName("animation");
                    if (animationElement != null) {

                        Array<StaticTiledMapTile> staticTiles = new Array<>();
                        IntArray intervals = new IntArray();
                        for (XmlReader.Element frameElement : animationElement.getChildrenByName("frame")) {
                            staticTiles.add((StaticTiledMapTile) tileset.getTile(firstgid + frameElement.getIntAttribute("tileid")));
                            intervals.add(frameElement.getIntAttribute("duration"));
                        }

                        AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, staticTiles);
                        animatedTile.setId(tile.getId());
                        animatedTiles.add(animatedTile);
                        tile = animatedTile;
                    }

                    String terrain = tileElement.getAttribute("terrain", null);
                    if (terrain != null) {
                        tile.getProperties().put("terrain", terrain);
                    }
                    String probability = tileElement.getAttribute("probability", null);
                    if (probability != null) {
                        tile.getProperties().put("probability", probability);
                    }
                    XmlReader.Element properties = tileElement.getChildByName("properties");
                    if (properties != null) {
                        loadProperties(tile.getProperties(), properties);
                    }
                }
            }

            for (AnimatedTiledMapTile tile : animatedTiles) {
                tileset.putTile(tile.getId(), tile);
            }

            XmlReader.Element properties = element.getChildByName("properties");
            if (properties != null) {
                loadProperties(tileset.getProperties(), properties);
            }
            map.getTileSets().addTileSet(tileset);

            map.getLayers().add(collidersLayer);
        }
    }

    private void loadObjectGroup(MapLayer layer, XmlReader.Element element, int tileId) {
        if (element.getName().equals("objectgroup")) {
            XmlReader.Element properties = element.getChildByName("properties");
            if (properties != null) {
                loadProperties(layer.getProperties(), properties);
            }

            for (XmlReader.Element objectElement : element.getChildrenByName("object")) {
                loadObject(map, layer, objectElement, tileId);
            }
        }
    }

    private void loadObject(TiledMap map, MapLayer layer, XmlReader.Element element, int tileId) {
        if (element.getName().equals("object")) {
            MapObject object = null;

            float scaleX = convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
            float scaleY = convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

            float x = element.getFloatAttribute("x", 0) * scaleX;
            float y = (flipY ? (mapHeightInPixels - element.getFloatAttribute("y", 0)) : element.getFloatAttribute("y", 0)) * scaleY;

            float width = element.getFloatAttribute("width", 0) * scaleX;
            float height = element.getFloatAttribute("height", 0) * scaleY;

            if (element.getChildCount() > 0) {
                XmlReader.Element child;
                if ((child = element.getChildByName("polygon")) != null) {
                    String[] points = child.getAttribute("points").split(" ");
                    float[] vertices = new float[points.length * 2];
                    for (int i = 0; i < points.length; i++) {
                        String[] point = points[i].split(",");
                        vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
                        vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
                    }
                    Polygon polygon = new Polygon(vertices);
                    polygon.setPosition(x, y);
                    object = new PolygonMapObject(polygon);
                } else if ((child = element.getChildByName("polyline")) != null) {
                    String[] points = child.getAttribute("points").split(" ");
                    float[] vertices = new float[points.length * 2];
                    for (int i = 0; i < points.length; i++) {
                        String[] point = points[i].split(",");
                        vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
                        vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
                    }
                    Polyline polyline = new Polyline(vertices);
                    polyline.setPosition(x, y);
                    object = new PolylineMapObject(polyline);
                } else if (element.getChildByName("ellipse") != null) {
                    object = new EllipseMapObject(x, flipY ? y - height : y, width, height);
                }
            }
            if (object == null) {
                String gid;
                if ((gid = element.getAttribute("gid", null)) != null) {
                    int id = (int) Long.parseLong(gid);
                    boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
                    boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);

                    TiledMapTile tile = map.getTileSets().getTile(id & ~MASK_CLEAR);
                    TiledMapTileMapObject tiledMapTileMapObject = new TiledMapTileMapObject(tile, flipHorizontally, flipVertically);
                    TextureRegion textureRegion = tiledMapTileMapObject.getTextureRegion();
                    tiledMapTileMapObject.getProperties().put("gid", id);
                    tiledMapTileMapObject.setX(x);
                    tiledMapTileMapObject.setY(flipY ? y : y - height);
                    float objectWidth = element.getFloatAttribute("width", textureRegion.getRegionWidth());
                    float objectHeight = element.getFloatAttribute("height", textureRegion.getRegionHeight());
                    tiledMapTileMapObject.setScaleX(scaleX * (objectWidth / textureRegion.getRegionWidth()));
                    tiledMapTileMapObject.setScaleY(scaleY * (objectHeight / textureRegion.getRegionHeight()));
                    tiledMapTileMapObject.setRotation(element.getFloatAttribute("rotation", 0));
                    object = tiledMapTileMapObject;
                } else {
                    object = new RectangleMapObject(x, flipY ? y - height : y, width, height);
                }
            }
            object.setName(element.getAttribute("name", null));
            String rotation = element.getAttribute("rotation", null);
            if (rotation != null) {
                object.getProperties().put("rotation", Float.parseFloat(rotation));
            }
            String type = element.getAttribute("type", null);
            if (type != null) {
                object.getProperties().put("type", type);
            }
            int id = element.getIntAttribute("id", 0);
            if (id != 0) {
                object.getProperties().put("id", id);
            }
            object.getProperties().put("x", x);

            if (object instanceof TiledMapTileMapObject) {
                object.getProperties().put("y", y);
            } else {
                object.getProperties().put("y", (flipY ? y - height : y));
            }
            object.getProperties().put("tileId", tileId);
            object.getProperties().put("width", width);
            object.getProperties().put("height", height);
            object.setVisible(element.getIntAttribute("visible", 1) == 1);
            XmlReader.Element properties = element.getChildByName("properties");
            if (properties != null) {
                loadProperties(object.getProperties(), properties);
            }
            layer.getObjects().add(object);
        }
    }
}
