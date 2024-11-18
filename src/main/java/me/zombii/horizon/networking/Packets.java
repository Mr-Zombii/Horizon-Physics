package me.zombii.horizon.networking;

import finalforeach.cosmicreach.networking.GamePacket;
import finalforeach.cosmicreach.networking.packets.entities.HitEntityPacket;
import finalforeach.cosmicreach.util.logging.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class Packets {

    public static void register() {
        registerPacket("packets", 8000, HitEntityPacket.class);
    }

    public static <T extends GamePacket> void registerPacket(String strId, int numId, Class<T> packetClass) {
        try {
            Constructor<T> packetConstructor = packetClass.getDeclaredConstructor();
            Supplier<T> supplier = () -> {
                try {
                    return (T) packetConstructor.newInstance();
                } catch (IllegalArgumentException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            };
            GamePacket.idsToPackets.put(numId, supplier);
            GamePacket.packetsToIntIds.put(packetClass, numId);
            GamePacket.packetNamesToIntIds.put(strId, numId);
            GamePacket.packetNamesToClasses.put(strId, packetClass);
            Logger.info("Registered packet ( id = " + numId + " ):" + strId);
        } catch (Exception var5) {
            Exception e = var5;
            throw new RuntimeException(e);
        }
    }


}
