package net.me.parkour.obstacles;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public abstract class Obstacle extends ContactListener {
    public ModelInstance obstacleInstance;
    public btRigidBody body;
    public Model obstacle;
    public btCollisionShape shape;
    public Vector3 position = new Vector3();
    static ModelBuilder builder = new ModelBuilder();
    public final Vector3 spawnPosition = new Vector3();

    public Obstacle() {}

    public void update() {
        obstacleInstance.transform.set(body.getWorldTransform());
        obstacleInstance.transform.getTranslation(position);
    }

    public void render(ModelBatch batch, Environment environment) {
        batch.render(obstacleInstance, environment);
    }

    public void dispose() {
        body.dispose();
        obstacle.dispose();
        shape.dispose();
    }
    public void registerBodies(btDynamicsWorld dynamicsWorld){
        dynamicsWorld.addRigidBody(body);
    }
}

