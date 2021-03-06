package de.bsautermeister.jump.screens.game;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import de.bsautermeister.jump.assets.AssetDescriptors;

public class GameSoundEffects implements Disposable {

    private static final long VOICE_DEDUPLICATION_GRACE_PERIOD = 500L;
    private long lastVoiceStart;

    public Sound bumpSound;
    public Sound beerSpawnSound;
    public Sound coinSpawnSound;
    public Sound eatFoodSound;
    public Sound coinSound;
    public Sound breakBlockSound;
    public Sound stompSound;
    public Sound[] complainSounds;
    private Sound[] swearingSounds;
    private Sound[] drownSounds;
    private Sound[] shoutSounds;
    private Sound[] beerSounds;
    private Sound[] ozapftSounds;
    private Sound[] boostSounds;
    private Sound[] needBeerSounds;
    private Sound[] spotBeerSounds;
    private Sound[] startSounds;
    private Sound[] victorySounds;
    private Sound jumpSound;
    public Sound landingSound;
    public Sound kickedSound;
    public Sound splashSound;
    public Sound fireSound;
    public Sound drinkingSound;
    public Sound successSound;
    public Sound burpSound;
    public Sound ravenSound;
    public Sound frogSound;
    public Sound whineSound;

    public GameSoundEffects(AssetManager assetManager) {
        bumpSound = assetManager.get(AssetDescriptors.Sounds.BUMP);
        beerSpawnSound = assetManager.get(AssetDescriptors.Sounds.BEER_SPAWN);
        coinSpawnSound = assetManager.get(AssetDescriptors.Sounds.COIN_SPAWN);
        eatFoodSound = assetManager.get(AssetDescriptors.Sounds.EAT_FOOD);
        coinSound = assetManager.get(AssetDescriptors.Sounds.COIN);
        breakBlockSound = assetManager.get(AssetDescriptors.Sounds.BREAK_BLOCK);
        stompSound = assetManager.get(AssetDescriptors.Sounds.STOMP);
        swearingSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.SWEARING1),
                assetManager.get(AssetDescriptors.Sounds.SWEARING2),
                assetManager.get(AssetDescriptors.Sounds.SWEARING3),
                assetManager.get(AssetDescriptors.Sounds.SWEARING4),
                assetManager.get(AssetDescriptors.Sounds.SWEARING5),
                assetManager.get(AssetDescriptors.Sounds.SWEARING6),
                assetManager.get(AssetDescriptors.Sounds.SWEARING7),
                assetManager.get(AssetDescriptors.Sounds.SWEARING8),
                assetManager.get(AssetDescriptors.Sounds.SWEARING9),
                assetManager.get(AssetDescriptors.Sounds.SWEARING10)
        };
        drownSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.DROWN1),
                assetManager.get(AssetDescriptors.Sounds.DROWN2),
                assetManager.get(AssetDescriptors.Sounds.DROWN3),
                assetManager.get(AssetDescriptors.Sounds.DROWN4),
                assetManager.get(AssetDescriptors.Sounds.DROWN5)
        };
        shoutSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.SHOUT1),
                assetManager.get(AssetDescriptors.Sounds.SHOUT2),
                assetManager.get(AssetDescriptors.Sounds.SHOUT3),
                assetManager.get(AssetDescriptors.Sounds.SHOUT4),
                assetManager.get(AssetDescriptors.Sounds.SHOUT5)
        };
        beerSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.BEER1),
                assetManager.get(AssetDescriptors.Sounds.BEER2),
                assetManager.get(AssetDescriptors.Sounds.BEER3),
                assetManager.get(AssetDescriptors.Sounds.BEER4),
                assetManager.get(AssetDescriptors.Sounds.BEER5)
        };
        ozapftSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.OZAPFT1),
                assetManager.get(AssetDescriptors.Sounds.OZAPFT2)
        };
        boostSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.BOOST1),
                assetManager.get(AssetDescriptors.Sounds.BOOST2),
                assetManager.get(AssetDescriptors.Sounds.BOOST3),
                assetManager.get(AssetDescriptors.Sounds.BOOST4),
                assetManager.get(AssetDescriptors.Sounds.BOOST5)
        };
        needBeerSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.NEED_BEER1),
                assetManager.get(AssetDescriptors.Sounds.NEED_BEER2),
                assetManager.get(AssetDescriptors.Sounds.NEED_BEER3)
        };
        spotBeerSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.SPOT_BEER1),
                assetManager.get(AssetDescriptors.Sounds.SPOT_BEER2),
                assetManager.get(AssetDescriptors.Sounds.SPOT_BEER3)
        };
        startSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.START1),
                assetManager.get(AssetDescriptors.Sounds.START2),
                assetManager.get(AssetDescriptors.Sounds.START3),
                assetManager.get(AssetDescriptors.Sounds.START4),
                assetManager.get(AssetDescriptors.Sounds.START5)
        };
        victorySounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.VICTORY1),
                assetManager.get(AssetDescriptors.Sounds.VICTORY2),
                assetManager.get(AssetDescriptors.Sounds.VICTORY3),
                assetManager.get(AssetDescriptors.Sounds.VICTORY4),
                assetManager.get(AssetDescriptors.Sounds.VICTORY5)
        };
        complainSounds = new Sound[] {
                assetManager.get(AssetDescriptors.Sounds.COMPLAIN1),
                assetManager.get(AssetDescriptors.Sounds.COMPLAIN2),
                assetManager.get(AssetDescriptors.Sounds.COMPLAIN3),
                assetManager.get(AssetDescriptors.Sounds.COMPLAIN4),
                assetManager.get(AssetDescriptors.Sounds.COMPLAIN5)
        };
        jumpSound = assetManager.get(AssetDescriptors.Sounds.JUMP);
        landingSound = assetManager.get(AssetDescriptors.Sounds.LANDING);
        kickedSound = assetManager.get(AssetDescriptors.Sounds.KICKED);
        splashSound = assetManager.get(AssetDescriptors.Sounds.SPLASH);
        fireSound = assetManager.get(AssetDescriptors.Sounds.FIRE);
        drinkingSound = assetManager.get(AssetDescriptors.Sounds.DRINKING);
        successSound = assetManager.get(AssetDescriptors.Sounds.SUCCESS);
        burpSound = assetManager.get(AssetDescriptors.Sounds.BURP);
        ravenSound = assetManager.get(AssetDescriptors.Sounds.RAVEN);
        frogSound = assetManager.get(AssetDescriptors.Sounds.FROG);
        whineSound = assetManager.get(AssetDescriptors.Sounds.WHINE);
    }

    public Sound randomSwearingSound() {
        deduplicateVoice();
        return swearingSounds[MathUtils.random(swearingSounds.length - 1)];
    }

    public Sound randomDrownSound() {
        deduplicateVoice();
        return drownSounds[MathUtils.random(drownSounds.length - 1)];
    }

    public Sound randomShoutSound() {
        deduplicateVoice();
        return shoutSounds[MathUtils.random(shoutSounds.length - 1)];
    }

    public Sound randomBeerSound() {
        deduplicateVoice();
        return beerSounds[MathUtils.random(beerSounds.length - 1)];
    }

    public Sound randomOzapftSound() {
        deduplicateVoice();
        return ozapftSounds[MathUtils.random(ozapftSounds.length - 1)];
    }

    public void playRandomBoostSound() {
        if (canPlayVoice()) {
            boostSounds[MathUtils.random(boostSounds.length - 1)].play();
            deduplicateVoice();
        }
    }

    public void playRandomNeedBeerSound() {
        if (canPlayVoice()) {
            needBeerSounds[MathUtils.random(needBeerSounds.length - 1)].play();
            deduplicateVoice();
        }
    }

    public void playRandomSpotBeerSound() {
        if (canPlayVoice()) {
            spotBeerSounds[MathUtils.random(spotBeerSounds.length - 1)].play();
            deduplicateVoice();
        }
    }

    public void playRandomStartSound() {
        if (canPlayVoice()) {
            startSounds[MathUtils.random(startSounds.length - 1)].play();
            deduplicateVoice();
        }
    }

    public Sound randomVictorySound() {
        deduplicateVoice();
        return victorySounds[MathUtils.random(victorySounds.length - 1)];
    }

    public Sound randomComplainSound() {
        deduplicateVoice();
        return complainSounds[MathUtils.random(complainSounds.length - 1)];
    }

    public void playRandomBurpSound(float volume) {
        float randomVolume = MathUtils.random(0.9f * volume, 1.0f * volume);
        float randomPitch = MathUtils.random(0.85f, 1.15f);
        burpSound.play(randomVolume, randomPitch, 1.0f);
    }

    public void playRandomJumpSound(float volume) {
        float randomPitch = MathUtils.random(0.9f, 1.0f);
        jumpSound.play(volume, randomPitch, 1.0f);
    }

    private boolean canPlayVoice() {
        return lastVoiceStart + VOICE_DEDUPLICATION_GRACE_PERIOD < System.currentTimeMillis();
    }

    private void deduplicateVoice() {
        lastVoiceStart = System.currentTimeMillis();
    }

    @Override
    public void dispose() {
        // disposing sound effects has weird side effects:
        // - Effect stop playing the next time
        // - GdxRuntimeException: Unable to allocate audio buffers.
    }
}
