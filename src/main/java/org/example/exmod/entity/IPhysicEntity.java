package org.example.exmod.entity;

import com.badlogic.gdx.math.Vector3;
import com.jme3.bullet.objects.PhysicsBody;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.Supplier;

public interface IPhysicEntity {

    @NonNull PhysicsBody getBody();
    @NonNull Vector3 getEularRotation();
    @NonNull UUID getUUID();
    float getMass();

    void setEularRotation(Vector3 rot);
    void setUUID(UUID uuid);
    void setMass(float mass);

    static <T> T readOrDefault(Supplier<T> read, T _default) {
        try {
            return read.get();
        } catch (Exception ignore) {
            return _default;
        }
    }

    static <T extends Entity & IPhysicEntity> void read(T entity, CRBinDeserializer deserial) {
        entity.setUUID(readOrDefault(() -> {
            String uid = deserial.readString("uuid");
            return UUID.fromString(uid);
        }, UUID.randomUUID()));

        entity.setEularRotation(readOrDefault(() -> {
            float yaw = deserial.readFloat("yaw", 0);
            float pitch = deserial.readFloat("pitch", 0);
            float roll = deserial.readFloat("roll", 0);

            return new Vector3(yaw, pitch, roll);
        }, new Vector3(0, 0, 0)));

        entity.setMass(readOrDefault(() -> {
            return deserial.readFloat("mass", 0.25f);
        }, 0.25f));
    }

    static <T extends Entity & IPhysicEntity> void write(T entity, CRBinSerializer serial) {
        serial.writeString("uuid", entity.getUUID() == null ? UUID.randomUUID().toString() : entity.getUUID().toString());

        serial.writeFloat("yaw", entity.getEularRotation().x);
        serial.writeFloat("pitch", entity.getEularRotation().y);
        serial.writeFloat("roll", entity.getEularRotation().z);

        serial.writeFloat("mass", entity.getMass());
    }

}
