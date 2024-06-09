package net.me.parkour.obstacles;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Array;

public class ObstacleFactory {
    private Array<Obstacle> obstacles;

    public ObstacleFactory() {
        obstacles = new Array<>();
    }

    public Obstacle createObstacle(Obstacle obstacle) {
        obstacles.add(obstacle);
        return obstacle;
    }

    public void update() {
        for (Obstacle obstacle : obstacles) {
            obstacle.update();
        }
    }

    public void render(ModelBatch batch, Environment environment) {
        for (Obstacle obstacle : obstacles) {
            obstacle.render(batch, environment);
        }
    }
    public void registerRigidBodies(btDynamicsWorld dynamicsWorld){
        for (Obstacle obstacle : obstacles) {
            obstacle.registerBodies(dynamicsWorld);
        }
    }

    public void dispose() {
        for (Obstacle obstacle : obstacles) {
            obstacle.dispose();
        }
        obstacles.clear();
    }
}
