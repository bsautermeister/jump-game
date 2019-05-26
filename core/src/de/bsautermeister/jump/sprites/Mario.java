package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
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

import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.GameConfig;
import de.bsautermeister.jump.JumpGame;
import de.bsautermeister.jump.assets.RegionNames;

public class Mario extends Sprite {

    private static final float INITAL_TTL = 300;

    private GameCallbacks callbacks;
    private World world;
    private Body body;

    private int groundContactCounter;
    private float jumpFixTimer;

    public enum State {
        STANDING, JUMPING, WALKING, DEAD
    }

    private GameObjectState<State> state;

    private boolean runningRight;

    private TextureRegion marioStand;
    private TextureRegion marioDead;
    private TextureRegion marioTurn;
    private Animation<TextureRegion> marioWalk;
    private Animation<TextureRegion> marioJump; // is actually just 1 frame

    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private TextureRegion bigMarioTurn;
    private Animation<TextureRegion> bigMarioWalk;

    private boolean isTurning;

    private static final float GROW_TIME = 1f;
    private float growingTimer;

    private boolean isBig;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;

    private boolean deadAnimationStarted = false;

    private float timeToLive;
    private int score;

    public Mario(GameCallbacks callbacks, World world, TextureAtlas atlas) {
        this.callbacks = callbacks;
        this.world = world;
        state = new GameObjectState<State>(State.STANDING);
        runningRight = true;

        setBounds(0, 0, GameConfig.BLOCK_SIZE / GameConfig.PPM, GameConfig.BLOCK_SIZE / GameConfig.PPM);

        TextureRegion littleMarioTexture = atlas.findRegion(RegionNames.LITTLE_MARIO);
        TextureRegion bigMarioTexture = atlas.findRegion(RegionNames.BIG_MARIO);

        marioStand = new TextureRegion(littleMarioTexture, 0, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE);
        bigMarioStand = new TextureRegion(bigMarioTexture, 0, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE);

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE));
        }
        marioWalk = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(bigMarioTexture, i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE));
        }
        bigMarioWalk = new Animation<TextureRegion>(0.1f, frames);

        frames.clear();
        for (int i = 5; i < 6; i++) {
            frames.add(new TextureRegion(littleMarioTexture, i * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE));
        }
        marioJump = new Animation<TextureRegion>(0.1f, frames);
        bigMarioJump = new TextureRegion(bigMarioTexture, 5 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE);

        marioDead = new TextureRegion(littleMarioTexture, 6 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE);

        marioTurn = new TextureRegion(littleMarioTexture, 4 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, GameConfig.BLOCK_SIZE);
        bigMarioTurn = new TextureRegion(bigMarioTexture, 4 * GameConfig.BLOCK_SIZE, 0, GameConfig.BLOCK_SIZE, 2 * GameConfig.BLOCK_SIZE);

        Vector2 startPostion = new Vector2(2 * GameConfig.BLOCK_SIZE / GameConfig.PPM, 2 * GameConfig.BLOCK_SIZE / GameConfig.PPM);
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

        if (isGrowing()) {
            growingTimer -= delta;
        }

        TextureRegion textureRegion = getFrame(delta);
        setRegion(textureRegion);

        // set texture bounds always at the bottom of the body
        float textureWidth = textureRegion.getRegionWidth() / GameConfig.PPM;
        float textureHeight = textureRegion.getRegionHeight() / GameConfig.PPM;
        float yOffset = 0f;
        if (isGrowing() && textureRegion.getRegionHeight() == GameConfig.BLOCK_SIZE) {
            yOffset = 7.5f / GameConfig.PPM;
        }
        setBounds(getX(), getY() - yOffset, textureWidth, textureHeight);

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

    public void control(boolean up, boolean left, boolean right) {
        state.unfreeze();
        isTurning = right && body.getLinearVelocity().x < 0 || left && body.getLinearVelocity().x > 0;

        if (up && touchesGround() && !state.is(State.JUMPING)) {
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
            state.set(State.JUMPING);
            jumpFixTimer = 0.25f;
            callbacks.jump();
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
                state.set(State.WALKING);
                state.freeze();
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

        boolean useBigTexture = isBig;
        if (isGrowing()) {
            useBigTexture = (int)(((growingTimer - (int)growingTimer)) * 8) % 2 == 0 ? isBig : false;
        }

        switch (state.current()) {
            case DEAD:
                textureRegion = marioDead;
                break;
            case JUMPING:
                if (useBigTexture) {
                    textureRegion = bigMarioJump;
                } else {
                    textureRegion = marioJump.getKeyFrame(state.timer());
                }
                break;
            case WALKING:
                if (useBigTexture) {
                    if (isTurning) {
                        textureRegion = bigMarioTurn;
                    } else {
                        textureRegion = bigMarioWalk.getKeyFrame(state.timer(), true);
                    }

                } else {
                    if (isTurning) {
                        textureRegion = marioTurn;
                    } else {
                        textureRegion = marioWalk.getKeyFrame(state.timer(), true);
                    }
                }
                break;
            case STANDING:
            default:
                textureRegion = useBigTexture ? bigMarioStand : marioStand;
                break;
        }



        if ((body.getLinearVelocity().x < 0 || !runningRight) && !textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && textureRegion.isFlipX()) {
            textureRegion.flip(true, false);
            runningRight = true;
        }

        if (isTurning && state.is(State.WALKING)) {
            textureRegion.flip(true, false);
        }

        return textureRegion;
    }

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
            growingTimer = GROW_TIME;
            timeToDefineBigMario = true;
            isBig = true;
        }
    }

    private void smaller(Enemy enemy) {
        if (isBig) {
            isBig = false;
            timeToRedefineMario = true;
            callbacks.hit(this, enemy);
        } else {
            kill();
        }
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Koopa) {
            Koopa koopa = (Koopa)enemy;
            if (koopa.getState() == Koopa.State.STANDING_SHELL) {
                koopa.kick(getX() <= enemy.getX());
                return;
            }
        }

        smaller(enemy);
    }

    private void kill() {
        if (state.is(State.DEAD))
            return;

        callbacks.gameOver();

        state.set(State.DEAD);
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

    public boolean isGrowing() {
        return growingTimer > 0;
    }
}
