package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.assets.RegionNames;

public class SpinningCoin extends Sprite {
    private final Vector2 spawnPosition;
    private Animation<TextureRegion> spinningAnimation;
    private float stateTime;
    private static final float ANIMATION_TIME = 0.5f;
    private static final float ANIMATION_OFFSET_Y = 4 * Cfg.BLOCK_SIZE / Cfg.PPM;
    private final Interpolation bumpUpInterpolation = Interpolation.linear;

    public SpinningCoin(TextureAtlas atlas, Vector2 position) {
        spawnPosition = new Vector2(position.x - (Cfg.BLOCK_SIZE / 2f / Cfg.PPM),
                position.y - (Cfg.BLOCK_SIZE / 2f / Cfg.PPM));
        setBounds(0, 0, Cfg.BLOCK_SIZE / Cfg.PPM, Cfg.BLOCK_SIZE / Cfg.PPM);
        setPosition(spawnPosition.x, spawnPosition.y);
        initTextures(atlas);
        stateTime = 0;
        setRegion(spinningAnimation.getKeyFrame(stateTime));
    }

    private void initTextures(TextureAtlas atlas) {
        TextureRegion spinningCoinTexture = atlas.findRegion(RegionNames.SPINNING_COIN);
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(spinningCoinTexture, i * Cfg.BLOCK_SIZE, 0, Cfg.BLOCK_SIZE, Cfg.BLOCK_SIZE));
        }
        spinningAnimation = new Animation<TextureRegion>(0.1f, frames);
    }

    public void update(float delta) {
        stateTime += delta;

        setRegion(spinningAnimation.getKeyFrame(stateTime, true));

        float totalProgress = getProgress();

        float offset = 0f;
        if (totalProgress < 1f) {
            float animationProgress;
            if (totalProgress <= 0.5f) {
                animationProgress = totalProgress * 2;
            } else {
                animationProgress = 1.0f - (totalProgress * 2 - 1f);
            }
            offset = bumpUpInterpolation.apply(animationProgress) * ANIMATION_OFFSET_Y;
        }
        setY(spawnPosition.y + offset);
    }

    private float getProgress() {
        return stateTime / ANIMATION_TIME;
    }

    public boolean isFinished() {
        // finish before it actually reaches its starting position
        return getProgress() > 0.8f;
    }
}