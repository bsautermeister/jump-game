package de.bsautermeister.jump.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;
import de.bsautermeister.jump.managers.Drownable;
import de.bsautermeister.jump.physics.Bits;
import de.bsautermeister.jump.physics.TaggedUserData;
import de.bsautermeister.jump.screens.game.GameCallbacks;
import de.bsautermeister.jump.sprites.GameObjectState;
import de.bsautermeister.jump.sprites.Player;

public class Hedgehog extends Enemy implements Drownable {
    private static final float SPEED_VALUE = 2.5f;
    private static final float KICK_SPEED = 8f;
    private static final float ROTATION_SPEED = 540f;
    private static final float WAIT_FOR_UNROLL_TIME = 5f;
    private static final float WALL_HITS_TO_KILL = 5;

    public enum State {
        WALKING, ROLL, ROLLING, UNROLL
    }

    private GameObjectState<State> state;
    private boolean drowning;
    private float speed;

    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> rollAnimation;
    private final Animation<TextureRegion> unrollAnimation;
    private final TextureRegion rollingTexture;

    private int leftSensorWallContacts;
    private int rightSensorWallContacts;
    private int leftSensorColliderContacts;
    private int rightSensorColliderContacts;

    private boolean previousDirectionLeft;

    private int wallHitsInSingleRoll;

    public Hedgehog(GameCallbacks callbacks, World world, TextureAtlas atlas,
                    float posX, float posY, boolean rightDirection) {
        super(callbacks, world, posX, posY, Cfg.BLOCK_SIZE_PPM, Cfg.BLOCK_SIZE_PPM);
        walkAnimation = new Animation<TextureRegion>(0.05f,
                atlas.findRegions(RegionNames.HEDGEHOG_WALK), Animation.PlayMode.LOOP);
        rollAnimation = new Animation<TextureRegion>(0.1f,
                atlas.findRegions(RegionNames.HEDGEHOG_ROLL), Animation.PlayMode.NORMAL);
        unrollAnimation = new Animation<TextureRegion>(0.2f,
                atlas.findRegions(RegionNames.HEDGEHOG_ROLL), Animation.PlayMode.REVERSED);
        rollingTexture = atlas.findRegion(RegionNames.HEDGEHOG_ROLL, 2);

        state = new GameObjectState<>(State.WALKING);
        state.setStateCallback(new GameObjectState.StateCallback<State>() {
            @Override
            public void changed(State previousState, State newState) {
                if (previousState != State.ROLLING && newState != State.ROLLING) {
                    return;
                }

                Fixture leftSideSensor = getBody().getFixtureList().get(2);
                updateColliderBit(leftSideSensor, previousState, newState);

                Fixture rightSideSensor = getBody().getFixtureList().get(3);
                updateColliderBit(rightSideSensor, previousState, newState);
            }

            private void updateColliderBit(Fixture fixture, State previousState, State newState) {
                Filter filter = fixture.getFilterData();
                if (previousState == State.ROLLING) {
                    filter.maskBits |= ~Bits.COLLIDER;
                } else if (newState == State.ROLLING) {
                    filter.maskBits &= ~Bits.COLLIDER;
                }
                fixture.setFilterData(filter);
            }
        });
        speed = rightDirection ? SPEED_VALUE : -SPEED_VALUE;
        setRegion(getFrame());
        setOrigin(getWidth() / 2, getHeight() * 5.5f / 16f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        state.update(delta);
        if (!isDead() && !isDrowning()) {
            getBody().setLinearVelocity(speed, getBody().getLinearVelocity().y);

            if (state.is(State.ROLLING)) {
                rotate(delta * (speed < 0 ? ROTATION_SPEED : -ROTATION_SPEED));
            }

            if (state.is(State.ROLL) && state.timer() > WAIT_FOR_UNROLL_TIME) {
                state.set(State.UNROLL);
            }

            updateDirection();
        }

        if (!state.is(State.ROLLING)) {
            setRotation(0f);
        }

        setPosition(getBody().getPosition().x - getWidth() / 2,
                getBody().getPosition().y - 6.25f / Cfg.PPM);
        setRegion(getFrame());

        if (state.is(State.UNROLL) && state.timer() > 0.66f) {
            state.set(State.WALKING);
            speed = previousDirectionLeft ? -SPEED_VALUE : SPEED_VALUE;
        }

        if (isDrowning()) {
            getBody().setLinearVelocity(getBody().getLinearVelocity().x * 0.95f, getBody().getLinearVelocity().y * 0.33f);
        }
    }

    private void updateDirection() {
        float absSpeed = Math.abs(speed);

        boolean isRolling = state.is(State.ROLLING);
        int leftContacts = isRolling ? leftSensorWallContacts : leftSensorWallContacts + leftSensorColliderContacts;
        int rightContacts = isRolling ? rightSensorWallContacts : rightSensorWallContacts + rightSensorColliderContacts;

        if (leftContacts > 0 && rightContacts == 0) {
            speed = absSpeed;
        } else if (leftContacts == 0 && rightContacts > 0) {
            speed = -absSpeed;
        }
    }

    private TextureRegion getFrame() {
        TextureRegion textureRegion;

        switch (state.current()) {
            case ROLLING:
                textureRegion = rollingTexture;
                break;
            case ROLL:
                textureRegion = rollAnimation.getKeyFrame(state.timer());
                break;
            case UNROLL:
                textureRegion = unrollAnimation.getKeyFrame(state.timer());
                break;
            case WALKING:
            default:
                textureRegion = walkAnimation.getKeyFrame(state.timer());
                break;
        }

        boolean isLeft = isLeftDirection();

        if (!isLeft && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        } else if (isLeft && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
        }

        if (isDead() && !textureRegion.isFlipY()) {
            textureRegion.flip(false, true);
        } else if (!isDead() && textureRegion.isFlipY()) {
            textureRegion.flip(false, true);
        }

        return textureRegion;
    }

    private boolean isLeftDirection() {
        return speed < 0 || speed == 0 && previousDirectionLeft;
    }

    @Override
    protected Body defineBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX() + getWidth() / 2, getY() + getHeight() / 2);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        Body body = getWorld().createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape bodyShape = new PolygonShape();
        Vector2[] bodyVertices = new Vector2[4];
        bodyVertices[0] = new Vector2(-6.5f, 0f).scl(1 / Cfg.PPM);
        bodyVertices[1] = new Vector2(6.5f, 0f).scl(1 / Cfg.PPM);
        bodyVertices[2] = new Vector2(-2.5f, -5.5f).scl(1 / Cfg.PPM);
        bodyVertices[3] = new Vector2(2.5f, -5.5f).scl(1 / Cfg.PPM);
        bodyShape.set(bodyVertices);
        fixtureDef.shape = bodyShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.PLAYER |
                Bits.ENEMY |
                Bits.BLOCK_TOP |
                Bits.BULLET |
                Bits.ENEMY_SIDE;
        body.createFixture(fixtureDef).setUserData(this);
        bodyShape.dispose();

        EdgeShape feetShape = new EdgeShape();
        feetShape.set(-2.25f / Cfg.PPM, -6f / Cfg.PPM,
                2.25f / Cfg.PPM, -6f / Cfg.PPM);
        fixtureDef.shape = feetShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY;
        fixtureDef.filter.maskBits = Bits.GROUND |
                Bits.PLATFORM |
                Bits.ITEM_BOX |
                Bits.BRICK |
                Bits.PLAYER |
                Bits.ENEMY |
                Bits.BLOCK_TOP |
                Bits.BULLET;
        body.createFixture(fixtureDef).setUserData(this);
        feetShape.dispose();

        // head
        PolygonShape headShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-4f, 6).scl(1 / Cfg.PPM);
        vertices[1] = new Vector2(4f, 6).scl(1 / Cfg.PPM);
        vertices[2] = new Vector2(-6.5f, 0).scl(1 / Cfg.PPM);
        vertices[3] = new Vector2(6.5f, 0).scl(1 / Cfg.PPM);
        headShape.set(vertices);
        fixtureDef.shape = headShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_HEAD;
        fixtureDef.filter.maskBits = Bits.PLAYER_FEET
                | Bits.BULLET;
        body.createFixture(fixtureDef).setUserData(this);
        headShape.dispose();

        EdgeShape sideShape = new EdgeShape();
        fixtureDef.shape = sideShape;
        fixtureDef.filter.categoryBits = Bits.ENEMY_SIDE;
        fixtureDef.filter.maskBits = Bits.GROUND
                | Bits.COLLIDER
                | Bits.ITEM_BOX
                | Bits.BRICK
                | Bits.PLATFORM;
        fixtureDef.isSensor = true;
        sideShape.set(new Vector2(-7 / Cfg.PPM, -2 / Cfg.PPM),
                new Vector2(-7 / Cfg.PPM, 2 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_LEFT));
        sideShape.set(new Vector2(7 / Cfg.PPM, -2 / Cfg.PPM),
                new Vector2(7 / Cfg.PPM, 2 / Cfg.PPM));
        body.createFixture(fixtureDef).setUserData(
                new TaggedUserData<Enemy>(this, TAG_RIGHT));
        sideShape.dispose();
        return body;
    }

    @Override
    public void onHeadHit(Player player) {
        if (player.isDead() || player.isInvincible()) {
            return;
        }

        if (!state.is(State.ROLL)) {
            stomp();
        } else {
            kill(0.5f);
        }
    }

    private void stomp() {
        boolean wasWalking = state.is(State.WALKING);
        state.set(State.ROLL);

        if (!wasWalking) {
            // skip roll animation, when rolling hedgehog is stopped
            state.setTimer(1f);
        }

        previousDirectionLeft = speed <= 0;
        speed = 0;
        getCallbacks().stomp(this);
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        boolean updateDirection = false;
        if (enemy instanceof Hedgehog) {
            Hedgehog otherHedgehog = (Hedgehog) enemy;
            if (!state.is(State.ROLLING) && otherHedgehog.getState() == State.ROLLING) {
                kill(1.0f);
                return;
            }
        }

        if (state.is(State.WALKING)) {
            updateDirection = true;
        }

        if (updateDirection) {
            runAwayFrom(enemy);
            getCallbacks().hitWall(this);
        }
    }

    private void runAwayFrom(Enemy otherEnemy) {
        speed = (getBody().getPosition().x < otherEnemy.getBody().getPosition().x)
                ? -SPEED_VALUE : SPEED_VALUE;
    }

    public void beginContactWallSensor(String sideSensorTag) {
        if (TAG_LEFT.equals(sideSensorTag)) {
            leftSensorWallContacts += 1;
        } else if (TAG_RIGHT.equals(sideSensorTag)) {
            rightSensorWallContacts += 1;
        }

        if (state.is(State.ROLLING)) {
            wallHitsInSingleRoll++;

            if (wallHitsInSingleRoll >= WALL_HITS_TO_KILL) {
                kill(0.5f);
                // apply additional impulse in opposite direction, because the hedgehog otherwise
                // stopped due to the wall, which looked odd
                getBody().applyLinearImpulse(new Vector2(-speed, 0), getBody().getWorldCenter(), true);
            }
        }

        getCallbacks().hitWall(this);
    }

    public void beginContactColliderSensor(String sideSensorTag) {
        if (TAG_LEFT.equals(sideSensorTag)) {
            leftSensorColliderContacts += 1;
        } else if (TAG_RIGHT.equals(sideSensorTag)) {
            rightSensorColliderContacts += 1;
        }
    }

    public void endContactWallSensor(String sideSensorTag) {
        if (TAG_LEFT.equals(sideSensorTag)) {
            leftSensorWallContacts = Math.max(0, leftSensorWallContacts - 1);
        } else if (TAG_RIGHT.equals(sideSensorTag)) {
            rightSensorWallContacts = Math.max(0, rightSensorWallContacts -1 );
        }
    }

    public void endContactColliderSensor(String sideSensorTag) {
        if (TAG_LEFT.equals(sideSensorTag)) {
            leftSensorColliderContacts = Math.max(0, leftSensorColliderContacts - 1);
        } else if (TAG_RIGHT.equals(sideSensorTag)) {
            rightSensorColliderContacts = Math.max(0, rightSensorColliderContacts -1 );
        }
    }

    public void kick(boolean directionRight) {
        state.set(State.ROLLING);
        speed = directionRight ? KICK_SPEED : -KICK_SPEED;
        getCallbacks().kicked(this);
        wallHitsInSingleRoll = 0;
    }

    public State getState() {
        return state.current();
    }

    @Override
    public void drown() {
        drowning = true;
        getBody().setLinearVelocity(getBody().getLinearVelocity().x / 10, getBody().getLinearVelocity().y / 10);
        if (state.is(State.ROLLING)) {
            getCallbacks().playerCausedDrown(this);
        }
    }

    @Override
    public boolean isDrowning() {
        return drowning;
    }

    private final Vector2 outCenter = new Vector2();

    @Override
    public Vector2 getWorldCenter() {
        Rectangle rect = getBoundingRectangle();
        outCenter.set(rect.x + rect.width / 2, rect.y + rect.height / 2);
        return outCenter;
    }

    @Override
    public Vector2 getLinearVelocity() {
        return getBody().getLinearVelocity();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        super.write(out);
        state.write(out);
        out.writeFloat(speed);
        out.writeBoolean(previousDirectionLeft);
        out.writeInt(wallHitsInSingleRoll);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        super.read(in);
        state.read(in);
        speed = in.readFloat();
        previousDirectionLeft = in.readBoolean();
        wallHitsInSingleRoll = in.readInt();
    }
}
