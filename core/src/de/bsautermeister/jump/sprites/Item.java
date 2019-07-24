package de.bsautermeister.jump.sprites;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.bsautermeister.jump.Cfg;
import de.bsautermeister.jump.GameCallbacks;
import de.bsautermeister.jump.serializer.BinarySerializable;

public abstract class Item extends Sprite implements BinarySerializable, Disposable {
    private String id;
    private GameCallbacks callbacks;
    private World world;
    protected Vector2 velocity;
    private Body body;

    private MarkedAction destroyBody;

    public Item(GameCallbacks callbacks, World world, float x, float y) {
        this.id = UUID.randomUUID().toString();
        this.callbacks = callbacks;
        this.world = world;
        setPosition(x, y);
        setBounds(getX(), getY(), 16 / Cfg.PPM, 16 / Cfg.PPM);
        body = defineBody();
        destroyBody = new MarkedAction();
    }

    public abstract Body defineBody();
    public abstract void usedBy(Mario mario);

    public void update(float delta) {
        boolean outOfBounds = getBody().getPosition().y < - Cfg.BLOCK_SIZE / Cfg.PPM;
        if (outOfBounds) {
            destroyBody.mark();
        }
    }

    public void postUpdate() {
        if (destroyBody.needsAction()) {
            dispose();
            destroyBody.done();
        }
    }

    @Override
    public void dispose() {
        if (!destroyBody.isDone()) {
            world.destroyBody(body);
        }
    }

    public void markDestroyBody() {
        destroyBody.mark();
    }

    public void reverseVelocity(boolean reverseX, boolean reverseY) {
        if (reverseX) {
            velocity.x = -velocity.x;
        }
        if (reverseY) {
            velocity.y = -velocity.y;
        }
    }

    public void bounceUp() {
        body.applyLinearImpulse(new Vector2(0, 1.5f), body.getWorldCenter(), true);
    }

    public String getId() {
        return id;
    }

    public World getWorld() {
        return world;
    }

    public Body getBody() {
        return body;
    }

    public boolean isRemovable() {
        return destroyBody.isDone();
    }

    public GameCallbacks getCallbacks() {
        return callbacks;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeFloat(body.getPosition().x);
        out.writeFloat(body.getPosition().y);
        out.writeFloat(body.getLinearVelocity().x);
        out.writeFloat(body.getLinearVelocity().y);
        out.writeFloat(velocity.x);
        out.writeFloat(velocity.y);
        destroyBody.write(out);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        id = in.readUTF();
        body.setTransform(in.readFloat(), in.readFloat(), 0);
        body.setLinearVelocity(in.readFloat(), in.readFloat());
        velocity.set(in.readFloat(), in.readFloat());
        destroyBody.read(in);
    }
}
