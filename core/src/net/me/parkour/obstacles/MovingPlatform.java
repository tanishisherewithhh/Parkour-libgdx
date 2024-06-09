package net.me.parkour.obstacles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import net.me.parkour.Parkour;


public class MovingPlatform extends Obstacle{
    final Vector3 endPos;
    public float speed;
    boolean movingToEnd = true;
    boolean carryObjects = true;

    public MovingPlatform(Vector3 startPos,Vector3 endPos, float width, float height, float depth, float mass,float speed){
        this.endPos = endPos;
        this.speed = speed;
        this.spawnPosition.set(startPos);
        obstacle = builder.createBox(width,height,depth,new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        obstacleInstance = new ModelInstance(obstacle);
        shape = new btBoxShape(new Vector3(width/2,height/2,depth/2));

        body = Parkour.createRigidBody(shape, mass,startPos);
        obstacleInstance.transform.set(body.getWorldTransform());
        this.body.setCollisionFlags(this.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
    }
    public void move() {
        Vector3 direction = movingToEnd ? new Vector3(endPos).sub(spawnPosition).nor() : new Vector3(spawnPosition).sub(endPos).nor();
        body.translate(direction.scl(speed * Gdx.graphics.getDeltaTime()));

        if(movingToEnd && body.getWorldTransform().getTranslation(new Vector3()).dst(endPos) < 5f) {
            movingToEnd = false;
        } else if(!movingToEnd && body.getWorldTransform().getTranslation(new Vector3()).dst(spawnPosition) < 5f) {
            movingToEnd = true;
        }
    }

    public MovingPlatform setCarryObjects(boolean carryObjects) {
        this.carryObjects = carryObjects;
        return this;
    }

    @Override
    public void update() {
        move();
        super.update();
    }

}
