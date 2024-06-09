package net.me.parkour;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class Player extends ContactListener implements InputProcessor {
    public btRigidBody body;
    public ModelInstance instance,XYZInstance;
    PerspectiveCamera cam;
    public Model playerModel, XYZModel;
    float speed = 20f;
    float jumpPower = 18f;
    boolean isJumping = false;
    Vector3 position;
    btCollisionShape playerShape;
    public btCollisionObject playerObject;
    float scroll = 10f;
    Vector3 spawnPos = new Vector3(0F,6F,0F);
    boolean isOnGround = true;
    float angleX = 0.0f;
    float angleY = 0.0f;
    float previousX = 0, previousY = 0;
    float jumpDelay = 0; // Add this as a field in your class
    float maxJumpDelay = 0.4f; // Maximum delay between jumps, adjust as needed
    boolean isCamFree = false, showXYZ = false;
    int jumpCount = 0; // Add this as a field in your class
    int maxJumpCount = 2; // Maximum number of jumps allowed before landing
    public Player(PerspectiveCamera cam) {
        this.cam = cam;
        position = new Vector3();
        playerObject = new btCollisionObject();
        // Create the player's model instance
        ModelBuilder modelBuilder = new ModelBuilder();
        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(Color.GRAY));
        material.set(ColorAttribute.createSpecular(Color.DARK_GRAY));
        material.set(FloatAttribute.createShininess(6f));

        playerModel = modelBuilder.createCapsule(2f, 10f, 16, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        XYZModel = modelBuilder.createXYZCoordinates(5f,new Material(),VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        instance = new ModelInstance(playerModel);
        XYZInstance = new ModelInstance(XYZModel);

        // Create the player's rigid body
        playerShape = new btCapsuleShape(2f, 6f);
        body = Parkour.createRigidBody(playerShape,1f,spawnPos);
        body.setDamping(0.8f,0.8f);

        playerObject.setCollisionShape(playerShape);
        playerObject.setWorldTransform(instance.transform);
        this.body.setCollisionFlags(this.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        this.body.setActivationState(Collision.DISABLE_DEACTIVATION);
    }
    @Override
    public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0, btCollisionObject colObj1, int partId1, int index1) {
        // Check if one of the collision objects is the player
        isOnGround = colObj0 == body || colObj1 == body;
        return true;
    }

    public void update() {
        instance.transform.set(body.getWorldTransform());
        instance.transform.getTranslation(position);
        XYZInstance.transform.setTranslation(position.x,position.y + 5.1f,position.z);
        if (jumpDelay > 0) {
            jumpDelay -= Gdx.graphics.getDeltaTime();
        }
        if (isOnGround) {
            landed();
        }
        updateCamera();
        handleInput();
        body.setAngularFactor(new Vector3(0, 1f, 0));
    }
    public void render(ModelBatch batch,Environment environment){
        batch.render(instance,environment);
        if(showXYZ) {
            batch.render(XYZInstance);
        }
    }

    private void handleInput() {
        Vector3 force = new Vector3();
        Vector3 camDirection = new Vector3(cam.direction).nor();
        Vector3 camRight = new Vector3(cam.direction).crs(cam.up).nor();

        float forceScale = isJumping ? 0.5f : 1.0f; // Reduce force when jumping

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            force.add(camDirection.scl(speed * forceScale));
        }else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            force.add(camDirection.scl(-speed * forceScale));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            force.add(camRight.scl(-speed * forceScale));
        }else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            force.add(camRight.scl(speed * forceScale));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && jumpCount < maxJumpCount && jumpDelay <= 0.0f) {
            float actualJumpPower = jumpPower;

            for(int i = 1; i<=jumpCount; i++) {
                actualJumpPower *= 2f;
            }

            body.applyCentralImpulse(new Vector3(0, actualJumpPower, 0));
            isJumping = true;
            isOnGround = false;
            jumpCount++;
            jumpDelay = maxJumpDelay;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)){
            isCamFree = !isCamFree;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.X)){
            showXYZ = !showXYZ;
        }
        body.applyCentralForce(force);
    }

    private void updateCamera() {
        //Follow the player
        if(!isCamFree) {
            // Check for left or right arrow key press
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                angleX -= 0.5f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                angleX += 0.5f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                angleY -= 0.5f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                angleY += 0.5f;
            }

            // Calculate the new camera position
            float cameraX = position.x + scroll * MathUtils.cosDeg(90 - angleX);
            float cameraY = position.y + scroll * MathUtils.sinDeg(angleY);
            float cameraZ = position.z + scroll * MathUtils.sinDeg(90 - angleX);

            // Ensure the camera is above the ground level
            float groundLevel = 1f; // Adjust this value based on your ground level
            if (cameraY < groundLevel) {
                cameraY = groundLevel;
            }

            cam.position.set(cameraX, cameraY, cameraZ);
            cam.lookAt(position.x, position.y, position.z);
            cam.up.set(0, 1, 0); // Keep the camera upright
            cam.update();
        }
    }

    public void landed() {
        isJumping = false;
        isOnGround = true;
        jumpCount = 0; // Reset the jump count when the player lands
    }

    public void dispose() {
        body.dispose();
        playerModel.dispose();
        playerShape.dispose();
        XYZModel.dispose();
    }

    @Override
    public boolean keyDown(int i) {
           handleInput();
        return true;
    }

    @Override
    public boolean keyUp(int i) {
         handleInput();
        return true;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        return true;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Calculate the difference in mouse position
        float deltaX = (previousX - (float)screenX)/(float)Gdx.graphics.getWidth();
        float deltaY = (previousY - (float) screenY)/(float)Gdx.graphics.getHeight();
        previousX = (float) screenX;
        previousY = (float) screenY;

        // Adjust the angles based on the mouse movement
        angleX += deltaX * 360.0F;
        angleY += deltaY * 360.0F;

        // Clamp the vertical angle to prevent the camera from flipping over
        angleY = MathUtils.clamp(angleY, -89f, 89f); // Adjust these values as needed

        return true;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float v, float v1) {
        scroll += v1;
        scroll = MathUtils.clamp(scroll,0.1f , 50f);
        return true;
    }
}
