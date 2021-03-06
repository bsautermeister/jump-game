package de.bsautermeister.jump.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.sprites.GameObjectState;
import de.bsautermeister.jump.sprites.Player;

public class DrunkenGuy extends Enemy {
    private static final float MOVE_SPEED = 1.5f;
    private static final float HIDDEN_TIME = 2f;
    private static final float WAIT_TIME = 1.5f;

    public enum State {
        HIDDEN, MOVE_UP, WAITING, MOVE_DOWN, KILLED
    }

    private GameObjectState<State> state;

    private final Animation<TextureRegion> animation;

    private float hiddenTargetY;
    private float waitingTargetY;
    private final static float hiddenOffsetY = Cfg.BLOCK_SIZE / 4f / Cfg.PPM;

    private boolean blocked;
    private float peekTime;

    public DrunkenGuy(GameCallbacks callbacks, World world, TextureAtlas atlas,
                      float posX, float posY) {
        super(callbacks, world, posX, posY - hiddenOffsetY, Cfg.BLOCK_SIZE_PPM, (int)(1.5f * Cfg.BLOCK_SIZE) / Cfg.PPM);
        animation = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.DRUNKEN_GUY), Animation.PlayMode.LOOP);
        state = new GameObjectState<>(State.HIDDEN);
        hiddenTargetY = getBody().getPosition().y;
        waitingTargetY = getBody().getPosition().y + getHeight() + hiddenOffsetY - 1 / Cfg.PPM;
        setRegion(animation.getKeyFrame(state.timer()));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        peekTime += delta;

        if (!isActive()) {
            return;
        }

        state.update(delta);
        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - Cfg.BLOCK_SIZE * 0.75f / Cfg.PPM);
        setRegion(animation.getKeyFrame(peekTime));

        if (state.is(State.KILLED)) {
            if (getBody().getPosition().y > hiddenTargetY) {
                getBody().setLinearVelocity(0, 3 * -MOVE_SPEED);
            } else {
                getBody().setLinearVelocity(Vector2.Zero);
            }
        } else if (state.is(State.HIDDEN) && state.timer() > HIDDEN_TIME && !blocked) {
            getBody().setLinearVelocity(0, MOVE_SPEED);
            peekTime = 0f;
            state.set(State.MOVE_UP);
        } else if (state.is(State.MOVE_UP) && getBody().getPosition().y >= waitingTargetY) {
            getBody().setLinearVelocity(Vector2.Zero);
            state.set(State.WAITING);
        } else if (state.is(State.WAITING) && state.timer() > WAIT_TIME) {
            getBody().setLinearVelocity(0, -MOVE_SPEED);
            state.set(State.MOVE_DOWN);
        } else if (state.is(State.MOVE_DOWN) && getBody().getPosition().y <= hiddenTargetY) {
            getBody().setLinearVelocity(Vector2.Zero);
            state.set(State.HIDDEN);
        }
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape bodyShape = new PolygonShape();
        Vector2[] bodyVertices = new Vector2[6];
        bodyVertices[0] = new Vector2(-7f, 6f).scl(1 / Cfg.PPM);
        bodyVertices[1] = new Vector2(-3f, 12f).scl(1 / Cfg.PPM);
        bodyVertices[2] = new Vector2(3f, 12f).scl(1 / Cfg.PPM);
        bodyVertices[3] = new Vector2(7f, 6f).scl(1 / Cfg.PPM);
        bodyVertices[4] = new Vector2(7f, -12f).scl(1 / Cfg.PPM);
        bodyVertices[5] = new Vector2(-7f, -12f).scl(1 / Cfg.PPM);
        bodyShape.set(bodyVertices);
        fixtureDef.shape = bodyShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.PLAYER
                | Bits.BULLET
                | Bits.ENEMY;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        bodyShape.dispose();

        // top sensor
        EdgeShape topSensorShape = new EdgeShape();
        fixtureDef.shape = topSensorShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.PLAYER | Bits.BULLET;
        fixtureDef.isSensor = true;
        topSensorShape.set(new Vector2(-Cfg.BLOCK_SIZE_PPM, 14f / Cfg.PPM + hiddenOffsetY),
                new Vector2(Cfg.BLOCK_SIZE_PPM, 14f / Cfg.PPM + hiddenOffsetY));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_TOP));
        topSensorShape.dispose();
        return body;
    }

    @Override
    public void onHeadHit(Player player) {
        // NOOP
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (enemy instanceof Hedgehog) {
            Hedgehog hedgehog = (Hedgehog) enemy;
            if (hedgehog.getState() == Hedgehog.State.ROLLING) {
                kill(0f);
                return;
            }
        }
    }

    @Override
    public void kill(float pushFactor) {
        super.kill(pushFactor);
        state.set(State.KILLED);
    }

    @Override
    public boolean renderInForeground() {
        return false;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
        out.writeFloat(hiddenTargetY);
        out.writeFloat(waitingTargetY);
        out.writeBoolean(blocked);
        out.writeFloat(peekTime);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        hiddenTargetY = in.readFloat();
        waitingTargetY = in.readFloat();
        blocked = in.readBoolean();
        peekTime = in.readFloat();
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
