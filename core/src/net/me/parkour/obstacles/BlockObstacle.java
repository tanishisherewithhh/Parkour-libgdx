package net.me.parkour.obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import net.me.parkour.Parkour;

public class BlockObstacle extends Obstacle{
    public BlockObstacle(Vector3 spawnPos, float width, float height, float depth, float mass){
        this.spawnPosition.set(spawnPos);
        obstacle = builder.createBox(width,height,depth,new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        obstacleInstance = new ModelInstance(obstacle);
        shape = new btBoxShape(new Vector3(width/2,height/2,depth/2));

        body = Parkour.createRigidBody(shape, mass,spawnPos);
        obstacleInstance.transform.set(body.getWorldTransform());
    }
}
