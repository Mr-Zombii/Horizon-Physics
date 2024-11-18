package me.zombii.horizon;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;
import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientPreModInitializer;
import me.zombii.horizon.mesh.MeshInstancer;
import me.zombii.horizon.threading.MeshingThread;
import me.zombii.horizon.util.ItemRegistrar;

public class HorizonClient implements ClientPreModInitializer {

    @Override
    public void onPreInit() {
        MeshingThread.init();

        Constants.MESHER_INSTANCE = new MeshInstancer();
        Constants.ITEM_REGISTRAR_INSTANCE = new ItemRegistrar();
    }

}
