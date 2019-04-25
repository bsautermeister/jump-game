package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.AssetPaths;
import de.bsautermeister.jump.assets.RegionNames;

public class Mario extends Sprite {

    public static final float INITAL_TTL = 300;

    private World world;
    private Body body;

    private int groundContactCounter;
    private float jumpFixTimer;

    public enum State {
        STANDING, JUMPING, WALKING, GROWING, DEAD
    }

    private GameObjectState<State> state;

    private boolean runningRight;

    private TextureRegion marioStand;
    private TextureRegion marioDead;
    private Animation<TextureRegion> marioWalk;
    private Animation<TextureRegion> marioJump; // is actually just 1 frame

    private TextureRegion bigMarionStand;
    private TextureRegion bigMarioJump;
    private Animation<TextureRegion> bigMarioWalk;
    private Animation<TextureRegion> growMario;

    private boolean isBig;
    private boolean runGrowAnimation;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;

    private boolean deadAnimationStarted = false;

    private float timeToLive;
    private int score;

    public Mario(World world, TextureAtlas atlas) {
        this.world = world;
        state = new GameObjectState<State>(State.STANDING);
        runningRight = true;

        setBounds(0, 0, 16 / GameConfig.PPM, 16 / GameConfig.PPM);

        TextureRegion littleMarioTexture = atlas.findRegion(RegionNames.LITTLE_MARIO);
        TextureRegion bigMarioTexture = atlas.findRegion(RegionNames.BIG_MARIO);

        marioStand = new TextureRegion(littleMarioTexture, 0, 0, 16, 16);
        bigMarionStand = new TextureRegion(bigMarioTexture, 0, 0, 16, 32);

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * 16, 0, 16, 16));
        }
        marioWalk = new Animation(0.1f, frames);

        frames.clear();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(bigMarioTexture, i * 16, 0, 16, 32));
        }
        bigMarioWalk = new Animation(0.1f, frames);

        frames.clear();
        for (int i = 5; i < 6; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * 16, 0, 16, 16));
        }
        marioJump = new Animation(0.1f, frames);
        bigMarioJump = new TextureRegion(bigMarioTexture, 5 * 16, 0, 16, 32);

        frames.clear();
        // growing animation
        TextureRegion halfSizeMario = new TextureRegion(bigMarioTexture, 15 * 16, 0, 16, 32);
        frames.add(halfSizeMario);
        frames.add(bigMarionStand);
        frames.add(halfSizeMario);
        frames.add(bigMarionStand);
        frames.add(halfSizeMario);
        frames.add(bigMarionStand);
        growMario = new Animation<TextureRegion>(0.33f, frames);

        marioDead = new TextureRegion(littleMarioTexture, 6 * 16, 0, 16, 16);

        Vector2 startPostion = new Vector2(32 / GameConfig.PPM, 32 / GameConfig.PPM);
        defineBody(startPostion);

        setRegion(marioStand);

        timeToLive = INITAL_TTL;
        score = 0;
    }

    public void update(float delta) {
        state.upate(delta);
        timeToLive -= delta;
        jumpFixTimer -= delta;

        if (isBig) {
            setPosition(body.getPosition().x - getWidth() / 2,
                    body.getPosition().y - getHeight() / 2 - 6 / GameConfig.PPM);
        } else {
            setPosition(body.getPosition().x - getWidth() / 2,
                    body.getPosition().y - getHeight() / 2);
        }

        setRegion(getFrame(delta));

        // these are called outside of the physics update loop
        if (timeToDefineBigMario) {
            defineBigBody();
        } else if (timeToRedefineMario) {
            Vector2 position = getBody().getPosition();
            world.destroyBody(getBody());

            defineBody(position);

            timeToRedefineMario = false;
        }

        // check fallen out of game
        if (getY() < 0) {
            kill();
        }

        if (state.is(State.DEAD)) {
            if (state.timer() <= 0.5f) {
                getBody().setActive(false);
            } else if (!deadAnimationStarted) {
                getBody().setActive(true);
                getBody().applyLinearImpulse(new Vector2(0, 4.5f), getBody().getWorldCenter(), true); // TODO currently this is dependent on the speed when Mario died
                deadAnimationStarted = true;
            }
        }
    }

    public void control(boolean upJustPressed, boolean up, boolean left, boolean right) {
        if (upJustPressed && touchesGround() && !state.is(State.JUMPING)) {
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
            state.set(State.JUMPING);
            jumpFixTimer = 0.25f;
            return;
        }
        if (right && body.getLinearVelocity().x <= 2) {
            body.applyLinearImpulse(new Vector2(0.1f, 0), body.getWorldCenter(), true);

        }
        if (left && body.getLinearVelocity().x >= -2) {
            body.applyLinearImpulse(new Vector2(-0.1f, 0), body.getWorldCenter(), true);
        }
        if (!left && ! right) {
            // horizontally decelerate fast, but don't stop immediately
            body.setLinearVelocity(body.getLinearVelocity().x * 0.75f, body.getLinearVelocity().y);
        }

        if (!touchesGround()) {
            if (jumpFixTimer > 0 || state.is(State.JUMPING)) {
                // keep jumping state
                return;
            } else {
                state.set(State.STANDING);
            }
        } else if (jumpFixTimer < 0) {
            if (body.getLinearVelocity().x != 0) {
                state.set(State.WALKING);
            } else {
                state.set(State.STANDING);
            }
        }
    }

    private TextureRegion getFrame(float delta) {
        TextureRegion textureRegion;
        switch (state.current()) {
            case DEAD:
                textureRegion = marioDead;
                break;
            case GROWING:
                textureRegion = growMario.getKeyFrame(state.timer());
                if (growMario.isAnimationFinished(state.timer())) {
                    runGrowAnimation = false;
                }
                break;
            case JUMPING:
                if (isBig) {
                    textureRegion = bigMarioJump;
                } else {
                    textureRegion = marioJump.getKeyFrame(state.timer());
                }
                break;
            case WALKING:
                if (isBig) {
                    textureRegion = bigMarioWalk.getKeyFrame(state.timer(), true);
                } else {
                    textureRegion = marioWalk.getKeyFrame(state.timer(), true);
                }
                break;
            case STANDING:
            default:
                textureRegion = isBig ? bigMarionStand : marioStand;
                break;
        }

        if ((body.getLinearVelocity().x < 0 || !runningRight) && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
            runningRight = true;
        }

        return textureRegion;
    }

    /*public State getState() {
        if (isDead) {
            return State.DEAD;
        } else if (runGrowAnimation) {
            return State.GROWING;
        } else if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && state.was(State.JUMPING))) {
            return State.JUMPING;
        } else if (body.getLinearVelocity().y < 0) {
            return State.FALLING;
        } else if (body.getLinearVelocity().x > 0 || body.getLinearVelocity().x < 0) {
            return State.WALKING;
        }
        return State.STANDING;
    }*/

    public State getState() {
        return state.current();
    }

    private void defineBody(Vector2 position) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / GameConfig.PPM);
        fixtureDef.filter.categoryBits = JumpGame.MARIO_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.ENEMY_BIT |
                JumpGame.ENEMY_HEAD_BIT |
                JumpGame.OBJECT_BIT |
                JumpGame.ITEM_BIT;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        // head
        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(-2 / GameConfig.PPM, 6 / GameConfig.PPM),
                new Vector2(2 / GameConfig.PPM, 6 / GameConfig.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_HEAD_BIT;
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        body.createFixture(fixtureDef).setUserData(this);

        // feet
        EdgeShape feetShape = new EdgeShape();
        feetShape.set(new Vector2(-2 / GameConfig.PPM, -6.5f / GameConfig.PPM),
                new Vector2(2 / GameConfig.PPM, -6.5f / GameConfig.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_FEET_BIT;
        fixtureDef.shape = feetShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        body.createFixture(fixtureDef).setUserData(this);
    }

    private void defineBigBody() { // TODO refactor with defineBody(), because 90% is duplicated
        Vector2 currentPosition = getBody().getPosition();
        world.destroyBody(getBody());

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(currentPosition.add(0, 10 / GameConfig.PPM));
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / GameConfig.PPM);
        fixtureDef.filter.categoryBits = JumpGame.MARIO_BIT;
        fixtureDef.filter.maskBits = JumpGame.GROUND_BIT |
                JumpGame.COIN_BIT |
                JumpGame.BRICK_BIT |
                JumpGame.ENEMY_BIT |
                JumpGame.ENEMY_HEAD_BIT |
                JumpGame.OBJECT_BIT |
                JumpGame.ITEM_BIT;

        fixtureDef.shape = shape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);
        shape.setPosition(new Vector2(0, -14 / GameConfig.PPM));
        fixture = body.createFixture(fixtureDef);
        fixture.setUserData(this);

        EdgeShape headShape = new EdgeShape();
        headShape.set(new Vector2(-2 / GameConfig.PPM, 6 / GameConfig.PPM),
                new Vector2(2 / GameConfig.PPM, 6 / GameConfig.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_HEAD_BIT;
        fixtureDef.shape = headShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        body.createFixture(fixtureDef).setUserData(this);
        timeToDefineBigMario = false;

        // feet
        EdgeShape feetShape = new EdgeShape();
        feetShape.set(new Vector2(-2 / GameConfig.PPM, -20.5f / GameConfig.PPM),
                new Vector2(2 / GameConfig.PPM, -20.5f / GameConfig.PPM));
        fixtureDef.filter.categoryBits = JumpGame.MARIO_FEET_BIT;
        fixtureDef.shape = feetShape;
        fixtureDef.isSensor = true; // does not collide in the physics simulation
        body.createFixture(fixtureDef).setUserData(this);
    }

    public Body getBody() {
        return body;
    }

    public boolean isBig() {
        return isBig;
    }

    public boolean isDead() {
        return state.is(State.DEAD);
    }

    public float getStateTimer() {
        return state.timer();
    }

    public void grow() {
        if (!isBig()) {
            runGrowAnimation = true;
            timeToDefineBigMario = true;
            isBig = true;
            setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        }
        JumpGame.assetManager.get(AssetPaths.Sounds.POWERUP, Sound.class).play();
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa)enemy;
            if (koopa.getState() == Koopa.State.STANDING_SHELL) {
                koopa.kick(getX() <= enemy.getX() ? Koopa.KICK_SPEED : -Koopa.KICK_SPEED);
            } else {
                kill();
            }
            return;
        }

        if (isBig) {
            isBig = false;
            timeToRedefineMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() / 2);
            JumpGame.assetManager.get(AssetPaths.Sounds.POWERDOWN, Sound.class).play();
        } else {
            kill();
        }
    }

    private void kill() {
        if (state.is(State.DEAD))
            return;

        state.set(State.DEAD);
        JumpGame.assetManager.get(AssetPaths.Sounds.MARIO_DIE, Sound.class).play();
        Filter filter = new Filter();
        filter.maskBits = JumpGame.NOTHING_BIT;
        for (Fixture fixture : getBody().getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }

    public void addScore(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    public float getTimeToLive() {
        return timeToLive;
    }

    public void touchGround() {
        groundContactCounter++;
    }

    public void leftGround() {
        groundContactCounter--;
    }

    public boolean touchesGround() {
        return groundContactCounter > 0;
    }
}
