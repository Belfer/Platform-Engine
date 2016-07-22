package com.framework.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.framework.Mappers;
import com.framework.Script;
import com.framework.components.CGameObject;
import com.framework.components.CTransform;

/**
 * Created by conor on 16/07/16.
 */
public class UpdateSystem extends EntitySystem {
    private ImmutableArray<Entity> gameObjectEntities;

    static final float WORLD_STEP = 1f/120f;
    static final int POS_IT = 10;
    static final int VEL_IT = 10;
    World world;

    public UpdateSystem (World world)
    {
        this.world = world;
        world.setContactListener (new EntityContactListener());
    }

    public void addedToEngine(Engine engine) {
        gameObjectEntities = engine.getEntitiesFor(Family.all(CGameObject.class, CTransform.class).get());
    }

    public void update(float deltaTime) {
        for (Entity entity : gameObjectEntities) {
            CGameObject gameObject = Mappers.GAMEOBJECT.get(entity);

            for (Script script : gameObject.scripts) {
                script.update (Gdx.graphics.getDeltaTime ());
            }
        }

        world.step (WORLD_STEP, POS_IT, VEL_IT);
    }

    private class EntityContactListener implements ContactListener
    {
        @Override
        public void beginContact(Contact contact) {
            Fixture fixtureA = contact.getFixtureA ();
            Fixture fixtureB = contact.getFixtureB ();
            Entity entityA = (Entity) fixtureA.getBody().getUserData();
            Entity entityB = (Entity) fixtureB.getBody().getUserData();

            if (entityA != null) {
                CGameObject gameObjectA = Mappers.GAMEOBJECT.get(entityA);
                for (Script script : gameObjectA.scripts) {
                    script.beginContact (contact, entityB);
                }
            }

            if (entityB != null) {
                CGameObject gameObjectB = Mappers.GAMEOBJECT.get(entityB);
                for (Script script : gameObjectB.scripts) {
                    script.beginContact (contact, entityA);
                }
            }
        }

        @Override
        public void endContact(Contact contact) {
            Fixture fixtureA = contact.getFixtureA ();
            Fixture fixtureB = contact.getFixtureB ();
            Entity entityA = (Entity) fixtureA.getBody().getUserData();
            Entity entityB = (Entity) fixtureB.getBody().getUserData();

            if (entityA != null) {
                CGameObject gameObjectA = Mappers.GAMEOBJECT.get(entityA);
                for (Script script : gameObjectA.scripts) {
                    script.endContact (contact, entityB);
                }
            }

            if (entityB != null) {
                CGameObject gameObjectB = Mappers.GAMEOBJECT.get(entityB);
                for (Script script : gameObjectB.scripts) {
                    script.endContact (contact, entityA);
                }
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            Fixture fixtureA = contact.getFixtureA ();
            Fixture fixtureB = contact.getFixtureB ();
            Entity entityA = (Entity) fixtureA.getBody().getUserData();
            Entity entityB = (Entity) fixtureB.getBody().getUserData();

            if (entityA != null) {
                CGameObject gameObjectA = Mappers.GAMEOBJECT.get(entityA);
                for (Script script : gameObjectA.scripts) {
                    script.preSolve (contact, oldManifold, entityB);
                }
            }

            if (entityB != null) {
                CGameObject gameObjectB = Mappers.GAMEOBJECT.get(entityB);
                for (Script script : gameObjectB.scripts) {
                    script.preSolve (contact, oldManifold, entityA);
                }
            }
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
            Fixture fixtureA = contact.getFixtureA ();
            Fixture fixtureB = contact.getFixtureB ();
            Entity entityA = (Entity) fixtureA.getBody().getUserData();
            Entity entityB = (Entity) fixtureB.getBody().getUserData();

            if (entityA != null) {
                CGameObject gameObjectA = Mappers.GAMEOBJECT.get(entityA);
                for (Script script : gameObjectA.scripts) {
                    script.postSolve (contact, impulse, entityB);
                }
            }

            if (entityB != null) {
                CGameObject gameObjectB = Mappers.GAMEOBJECT.get(entityB);
                for (Script script : gameObjectB.scripts) {
                    script.postSolve (contact, impulse, entityA);
                }
            }
        }
    }
}