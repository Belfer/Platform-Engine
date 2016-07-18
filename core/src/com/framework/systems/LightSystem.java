package com.framework.systems;

import com.badlogic.ashley.core.EntitySystem;

import java.net.URL;
import java.util.ResourceBundle;

import box2dLight.RayHandler;
import javafx.fxml.Initializable;

/**
 * Created by conor on 17/07/16.
 */
public class LightSystem extends EntitySystem implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private RayHandler rayHandler;

    /*
    @Override
    public void init() {
        rayHandler = new RayHandler(Objects.world);
        rayHandler.setShadows(Values.SHADOWS);
        rayHandler.setAmbientLight(Values.AMBIENT_LIGHT);
        rayHandler.setAmbientLight(Values.AMBIENT_LIGHT_BRIGHTNESS);
    }

    @Override
    public void update(float deltaTime) {
        rayHandler.setCombinedMatrix(Objects.camera.combined);
        rayHandler.updateAndRender();
    }

    @Override
    public void addedToEngine(final Engine engine) {
        engine.addEntityListener(new EntityListener(){

            @Override
            public void entityAdded(Entity entity) {
                LightComponent lightComponent = Objects.LIGHT_MAPPER.get(entity);
                if(lightComponent == null){
                    return;
                }

                LightType lightType = lightComponent.lightType;

                if(lightType == LightType.ConeLight){
                    ConeLightComponent comp = Objects.CONE_LIGHT_MAPPER.get(entity);
                    ConeLight coneLight = new ConeLight(rayHandler, comp.numRays, comp.color, comp.maxDistance, comp.x, comp.y, comp.directionDegree, comp.coneDegree);
                    comp.coneLight = coneLight;
                }
                else if(lightType == LightType.PointLight){
                    PointLightComponent comp = Objects.POINT_LIGHT_MAPPER.get(entity);
                    PointLight pointLight = new PointLight(rayHandler, comp.numRays, comp.color, comp.maxDistance, comp.x, comp.y);
                    comp.pointLight = pointLight;
                }
                else if(lightType == LightType.DirectionalLight){
                    DirectionalLightComponent comp = Objects.DIRECTIONAL_LIGHT_MAPPER.get(entity);
                    DirectionalLight directionalLight = new DirectionalLight(rayHandler, comp.numRays, comp.color, comp.directionDegree);
                    comp.directionalLight = directionalLight;
                }
            }

            @Override
            public void entityRemoved(Entity entity) {

            }

        });
    }
    */
}
