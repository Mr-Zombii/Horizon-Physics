package org.example.exmod.mesh;

import com.badlogic.gdx.utils.Queue;

public class LoneThreadRunnable implements Runnable {

    Queue<Runnable> runners = new Queue<>();

    @Override
    public void run() {
        while (!runners.isEmpty()) {
            Runnable runnable = runners.removeFirst();
            if (runnable != null)
                runnable.run();
            else
                System.out.println("Uh oh, There was a null runner");
        }
    }
}
