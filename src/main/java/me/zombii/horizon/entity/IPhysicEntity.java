package me.zombii.horizon.entity;

import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.math.Quaternion;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.function.Supplier;

public interface IPhysicEntity {

    @NonNull PhysicsBody getBody();
    Quaternion getEularRotation();
    @NonNull UUID getUUID();
    float getMass();

    void setEularRotation(Quaternion rot);
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
            float rot_x = deserial.readFloat("rot_x", 0);
            float rot_y = deserial.readFloat("rot_y", 0);
            float rot_z = deserial.readFloat("rot_z", 0);
            float rot_w = deserial.readFloat("rot_w", 0);

            return new Quaternion(rot_x, rot_y, rot_z, rot_w);
        }, new Quaternion(0, 0, 0, 0)));

        entity.setMass(readOrDefault(() -> {
            return deserial.readFloat("mass", 0.25f);
        }, 0.25f));
    }

    static <T extends Entity & IPhysicEntity> void write(T entity, CRBinSerializer serial) {
        serial.writeString("uuid", entity.getUUID() == null ? UUID.randomUUID().toString() : entity.getUUID().toString());

        serial.writeFloat("rot_x", entity.getEularRotation().getX());
        serial.writeFloat("rot_y", entity.getEularRotation().getY());
        serial.writeFloat("rot_z", entity.getEularRotation().getZ());
        serial.writeFloat("rot_w", entity.getEularRotation().getW());

        serial.writeFloat("mass", entity.getMass());
    }

}
