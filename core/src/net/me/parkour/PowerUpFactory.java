package net.me.parkour;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class PowerUpFactory {
    private Array<PowerUp> powerUps;
    Player player;

    public PowerUpFactory(Player player) {
        this.player = player;
        powerUps = new Array<>();
    }

    public PowerUp createPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
        return powerUp;
    }

    public void update() {
        Iterator<PowerUp> iterator = powerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            if (powerUp.isCollected) {
                iterator.remove();
            } else {
                powerUp.update(player);
            }
        }
    }

    public void render(ModelBatch batch, Environment environment) {
        for (PowerUp powerUp : powerUps) {
            powerUp.render(batch, environment);
        }
    }

    public void dispose() {
        for (PowerUp powerUp : powerUps) {
            powerUp.onPlayerContact();
        }
        powerUps.clear();
    }
}
