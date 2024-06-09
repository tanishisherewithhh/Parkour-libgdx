package net.me.parkour;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.TimeUtils;

public class PowerUp {
    public ModelInstance instance;
    public btRigidBody body;
    public boolean isCollected = false;
    Vector3 position = new Vector3();
    PointLight powerUpLight = new PointLight();
    Environment env = new Environment();
    private boolean isLightAdded = false; // Add this line


    public PowerUp(Color color, Vector3 position) {
        // Create the power-up's model instance
        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(color));
        Model powerUpModel = modelBuilder.createBox(2f, 2f, 2f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        instance = new ModelInstance(powerUpModel, position.x, position.y, position.z);
        powerUpLight.set(color, this.position, 70f);

        // Create the power-up's rigid body
        btCollisionShape powerUpShape = new btBoxShape(new Vector3(1f, 1f, 1f));
        body = Parkour.createRigidBody(powerUpShape, 0f, position); // Assuming 0 mass (static body)
        body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE); // No collision response
    }

    public void update(Player player) {
        // Make the power-up float and rotate
        if(!body.isDisposed()) {
            instance.transform.translate(0, MathUtils.sinDeg(TimeUtils.millis() / 100f) / 100f, 0);
            instance.transform.rotate(Vector3.Y, 1f);
            instance.transform.getTranslation(position);

            if(player.position.dst(position) <= 5f) {
                onPlayerContact();
            }
        }
    }

    public void render(ModelBatch batch, Environment env){
        this.env = env;
        if(!body.isDisposed()) {
            if (!isLightAdded) { // Only add the light once
                env.add(powerUpLight);
                isLightAdded = true;
            }
            batch.render(instance, env);
            powerUpLight.position.set(position);
        }
    }

    public void onPlayerContact() {
        // Dispose of the power-up when the player makes contact
        isCollected = true;
        env.remove(powerUpLight);
        body.dispose();
        instance.model.dispose();
    }
}
