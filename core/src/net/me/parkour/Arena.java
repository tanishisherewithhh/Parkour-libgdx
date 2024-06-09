package net.me.parkour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class Arena {
    btCollisionShape wallShape1,wallShape2;

    //Invisible walls
    public btRigidBody northWallBody;
    public btRigidBody southWallBody;
    public btRigidBody eastWallBody;
    public btRigidBody westWallBody;

    float arenaSize = 500f;

    public Model arenaFloorModel;
    public ModelInstance floorInstance;
    public btCollisionShape floorShape;
    public btRigidBody floorBody;

    public Arena() {
        // Create the floor's rigid body and model instance
        ModelBuilder modelBuilder = new ModelBuilder();
        Texture floorTexture = new Texture(Gdx.files.internal("floor_texture.jpg"),true);
        // Set the texture wrap mode to Repeat
        floorTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);

        floorTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // Create the material, adding a TextureAttribute for the diffuse color.
        Material floorMaterial = new Material(TextureAttribute.createDiffuse(floorTexture));
        // Calculate the number of times to repeat the texture
        float uScale = arenaSize / 15f;
        float vScale = arenaSize / 15f;

        // Create the floor with the adjusted texture coordinates
        // Create the floor with the adjusted texture coordinates
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, floorMaterial);
        builder.setUVRange(0f, 0f, uScale, vScale);
        builder.box(0f, 0f, 0f, arenaSize, 1f, arenaSize);
        arenaFloorModel = modelBuilder.end();

        floorInstance = new ModelInstance(arenaFloorModel);
        floorShape = new btBoxShape(new Vector3(arenaSize/2, 0, arenaSize/2));
        floorBody = Parkour.createRigidBody(floorShape, 0f, new Vector3());
        floorInstance.transform.set(floorBody.getWorldTransform());


        // Create the walls
        wallShape1 = new btBoxShape(new Vector3(10f/2, arenaSize/2, arenaSize/2)); // 1 unit thick walls
        wallShape2 = new btBoxShape(new Vector3(arenaSize/2, arenaSize/2, 10f/2)); // 1 unit thick walls

        northWallBody = Parkour.createRigidBody(wallShape2, 0f, new Vector3(0, arenaSize/2, -(arenaSize / 2) - 10f));
        southWallBody = Parkour.createRigidBody(wallShape2, 0f, new Vector3(0, arenaSize/2,(arenaSize / 2) + 12f ));

        eastWallBody = Parkour.createRigidBody(wallShape1, 0f, new Vector3(arenaSize / 2  +7.9f, arenaSize/2, 0));
        westWallBody = Parkour.createRigidBody(wallShape1, 0f, new Vector3(-(arenaSize / 2) - 10f, arenaSize/2, 0f));
    }

    public void render(ModelBatch batch, Environment environment){
        batch.render(floorInstance,environment);
    }

    public void dispose() {
        northWallBody.dispose();
        southWallBody.dispose();
        eastWallBody.dispose();
        westWallBody.dispose();

        floorBody.dispose();
        arenaFloorModel.dispose();
        floorShape.dispose();
        wallShape1.dispose();
        wallShape2.dispose();
    }
}
