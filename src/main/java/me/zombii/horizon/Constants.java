package me.zombii.horizon;

import me.zombii.horizon.mesh.IMeshInstancer;
import me.zombii.horizon.util.IItemRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {

    public static IMeshInstancer MESHER_INSTANCE;
    public static IItemRegistrar ITEM_REGISTRAR_INSTANCE;
    public static final String MOD_ID = "horizon";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

}
