package net.me.parkour;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import net.me.parkour.obstacles.BlockObstacle;
import net.me.parkour.obstacles.MovingPlatform;
import net.me.parkour.obstacles.ObstacleFactory;


public class Parkour implements ApplicationListener {
	private boolean debugMode = false;

	PerspectiveCamera cam;
    btDynamicsWorld dynamicsWorld;
	ModelBatch batch;
	public Player player;
	public Arena arena;
	public AssetManager assetManager;
	public ModelInstance skySphere;
	public boolean isLoading = true;

	btDefaultCollisionConfiguration collisionConfiguration;
	btCollisionDispatcher dispatcher;
	btBroadphaseInterface broadphase;
	btSequentialImpulseConstraintSolver solver;
	// Create a new environment
	Environment environment;
	PointLight playerLight;
	private DebugDrawer debugDrawer;

	public ObstacleFactory obsfactory = new ObstacleFactory();

	PowerUp powerUp;

	@Override
	public void create() {
		assetManager = new AssetManager();
		setupEnvironment();
		Bullet.init();

		loadAssets();

		Gdx.input.setCatchKey(Input.Keys.BACK, true);
		Gdx.input.setCatchKey(Input.Keys.MENU, true);

		setupCamera();
		powerUp = new PowerUp(Color.ORANGE,new Vector3(10,5,0f));
		batch = new ModelBatch();

		createPlayer();
		createArena();

		createObstacles();
		setupDynamicsWorld();
	}
	public void loadAssets(){
		assetManager.load("skysphere.obj", Model.class);
	}
	public void createObstacles() {
		// Starting platform
		obsfactory.createObstacle(new BlockObstacle(new Vector3(0f, 1f, 0f), 10f, 2f, 10f, 0f));

		// Series of small platforms that require precise jumping
		for (int i = 1; i <= 5; i++) {
			obsfactory.createObstacle(new BlockObstacle(new Vector3(i * 10f, 1f + i * 2f, 0f), 5f, 1f, 5f, 0f));
		}

		// A 'wall' that the player needs to jump around
		obsfactory.createObstacle(new BlockObstacle(new Vector3(60f, 1f, 0f), 2f, 20f, 10f, 0f));

		// Platforms on the other side of the wall
		for (int i = 0; i < 3; i++) {
			obsfactory.createObstacle(new BlockObstacle(new Vector3(80f + i * 15f, 10f + i * 5f, 0f), 10f, 2f, 10f, 0f));
		}

		// Maze section
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if ((i + j) % 2 == 0) {
					obsfactory.createObstacle(new BlockObstacle(new Vector3(120f + i * 10f, 1f, j * 10f), 10f, 10f, 10f, 0f));
				}
			}
		}

		// Series of platforms that require jumping to reach
		for (int i = 1; i <= 3; i++) {
			obsfactory.createObstacle(new BlockObstacle(new Vector3(150f + i * 15f, 1f + i * 2f, 0f), 10f, 2f, 10f, 0f));
		}

		// Some higher platforms that require double or triple jumps
		obsfactory.createObstacle(new BlockObstacle(new Vector3(200f, 20f, -15f), 10f, 2f, 10f, 0f));
		obsfactory.createObstacle(new BlockObstacle(new Vector3(200f, 20f, 15f), 10f, 2f, 10f, 0f));


		obsfactory.createObstacle(new MovingPlatform(new Vector3(-10f, 20f, 15f), new Vector3(10,50f,15f), 10f,2f, 10f, 0f,5f));


		// Final platform
		obsfactory.createObstacle(new BlockObstacle(new Vector3(220f, 30f, 0f), 10f, 2f, 10f, 0f));
	}



	private void setupEnvironment(){
	     environment = new Environment();
        // Add a bright white ambient light
		environment.set(ColorAttribute.createAmbientLight(Color.WHITE ));
        // Add a directional light to simulate the sun
		environment.add(new DirectionalLight().set(new Color(125,100,100,255), new Vector3(-1f, -0.8f, -0.2f)));
		// Add a point light at the player's position to highlight the player
		playerLight = new PointLight().set(Color.WHITE, new Vector3(), 20f);
		environment.add(playerLight);
	}
	private void setupCamera() {
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.position.set(0f, 10f, 0f); // Position the camera above and behind the player
		cam.lookAt(0f, 0, 0f); // Look at the origin, where the player is
		cam.near = 1f;
		cam.far = 1000f;
		cam.update();
	}

	private void createPlayer() {
		player = new Player(cam);
		Gdx.input.setInputProcessor(player);
	}

	private void createArena() {
		arena = new Arena();
	}

	private void setupDynamicsWorld() {
		 collisionConfiguration = new btDefaultCollisionConfiguration();
		 dispatcher = new btCollisionDispatcher(collisionConfiguration);
		 broadphase = new btDbvtBroadphase();
		 solver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);

		//Debug Renderer
		debugDrawer = new DebugDrawer();
		dynamicsWorld.setDebugDrawer(debugDrawer);
		debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);

		dynamicsWorld.setGravity(new Vector3(0, -10f, 0));

		dynamicsWorld.addRigidBody(player.body);

		dynamicsWorld.addRigidBody(arena.floorBody);
		dynamicsWorld.addRigidBody(arena.northWallBody);
		dynamicsWorld.addRigidBody(arena.eastWallBody);
		dynamicsWorld.addRigidBody(arena.westWallBody);
		dynamicsWorld.addRigidBody(arena.southWallBody);

		obsfactory.registerRigidBodies(dynamicsWorld);
	}

	@Override
	public void resize(int i, int i1) {

	}

	@Override
	public void render() {
		if(isLoading && assetManager.update()){
			skySphere = new ModelInstance(assetManager.get("skysphere.obj",Model.class));
			skySphere.transform.scale(5,5,5);
			skySphere.transform.rotate(Vector3.X,180);
			isLoading = false;
		}
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Step the simulation
		dynamicsWorld.stepSimulation(Gdx.graphics.getDeltaTime(), 10);

		if(debugMode) {
			// Begin debug drawing
			debugDrawer.begin(cam);

			// Debug draw the world
			dynamicsWorld.debugDrawWorld();

			// End debug drawing
			debugDrawer.end();
		}

		batch.begin(cam);
		player.render(batch,environment);
		arena.render(batch,environment);
		powerUp.render(batch,environment);
		obsfactory.render(batch,environment);
		if(skySphere != null) {
			batch.render(skySphere);
		}
		batch.end();

		obsfactory.update();
		powerUp.update(player);
		player.update();
		playerLight.position.set(player.position);
		if(skySphere != null)
		skySphere.transform.setTranslation(cam.position);

		dynamicsWorld.performDiscreteCollisionDetection();

		if(Gdx.input.isKeyPressed(Input.Keys.K)){
			debugMode = !debugMode;
		}
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}
	@Override
	public void dispose() {
		player.dispose();
		dynamicsWorld.dispose();
		arena.dispose();
		batch.dispose();


		collisionConfiguration.dispose();
		dispatcher.dispose();
		broadphase.dispose();
		solver.dispose();
		obsfactory.dispose();
		assetManager.dispose();
	}

	public static btRigidBody createRigidBody(btCollisionShape shape, float mass, Vector3 initialPosition) {
		Vector3 localInertia = new Vector3(0, 0, 0);
		if (mass != 0f)
			shape.calculateLocalInertia(mass, localInertia);

		btMotionState motionState = new btDefaultMotionState(new Matrix4(initialPosition, new Quaternion(), new Vector3(1, 1, 1)));
		btRigidBody.btRigidBodyConstructionInfo constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, shape, localInertia);
		return new btRigidBody(constructionInfo);
	}
}