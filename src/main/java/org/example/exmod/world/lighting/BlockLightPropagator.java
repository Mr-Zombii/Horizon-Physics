package org.example.exmod.world.lighting;

import com.badlogic.gdx.utils.Queue;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.constants.Direction;
import org.example.exmod.world.BlockPos;
import org.example.exmod.world.StructureWorld;

public class BlockLightPropagator {
    public BlockLightPropagator() {
    }

    public static void propagateBlockDarkness(StructureWorld zone, Queue<BlockPos> darkQueue) {
        Queue<BlockPos> lightQueue = new Queue<>();

        int g;
        int b;
        int parentLightLimit;
        while(darkQueue.notEmpty()) {
            BlockPos position = darkQueue.removeFirst();
            int lpacked = position.getBlockLight();
            int r = (lpacked & 3840) >> 8;
            g = (lpacked & 240) >> 4;
            b = lpacked & 15;
            parentLightLimit = lpacked;

            if (r != g || g != b || b != 0) {
                position.setBlockLight(0, 0, 0);
                position.flagTouchingChunksForRemeshing(zone, false);
            }

            BlockState curBlock = position.getBlockState();
            if (curBlock != null && curBlock.isLightEmitter()) {
                lightQueue.addLast(position);
            }

            Direction[] var10 = Direction.ALL_DIRECTIONS;

            for (Direction d : var10) {
                PooledLightLimitPosition neighbourPosObj = new PooledLightLimitPosition(null, null, 0, 0, 0);
                BlockPos neighbourPos = position.getOffsetBlockPos(neighbourPosObj, zone, d);
                if (neighbourPos != null) {
                    neighbourPosObj.lightLimit = parentLightLimit;
                    if (position instanceof PooledLightLimitPosition limitPos) {
                        parentLightLimit = limitPos.getCombinedLimit(parentLightLimit);
                    }
                    int lnpacked = neighbourPos.getBlockLight();
                    int nr = (lnpacked & 3840) >> 8;
                    int ng = (lnpacked & 240) >> 4;
                    int nb = lnpacked & 15;
                    boolean addToDarkQueue = nr != 0 && nr < r && nr < neighbourPosObj.getRedLimit();
                    addToDarkQueue |= ng != 0 && ng < g && ng < neighbourPosObj.getGreenLimit();
                    addToDarkQueue |= nb != 0 && nb < b && nb < neighbourPosObj.getBlueLimit();
                    if (addToDarkQueue) {
                        darkQueue.addLast(neighbourPos);
                    }

                    if (nr != 0 && nr >= r || ng != 0 && ng >= g || nb != 0 && nb >= b) {
                        BlockState nBlock = position.getBlockState();
                        if (nBlock == null || !nBlock.isLightEmitter()) {
                            PooledLightLimitPosition lightPosObj = new PooledLightLimitPosition(null, null, 0, 0, 0);
                            lightPosObj.set(neighbourPosObj);
                            lightQueue.addLast(lightPosObj);
                        }
                    }
                }
            }
        }

        for (BlockPos lightPos : lightQueue) {
            BlockState nblock = lightPos.getBlockState();
            if (nblock != null) {
                g = lightPos.getBlockLight();
                b = (g & 3840) >> 8;
                parentLightLimit = (g & 240) >> 4;
                b = g & 15;
                b = Math.max(b, nblock.lightLevelRed);
                parentLightLimit = Math.max(parentLightLimit, nblock.lightLevelGreen);
                b = Math.max(b, nblock.lightLevelBlue);
                lightPos.setBlockLight(b, parentLightLimit, b);
            }
        }

        if (lightQueue.notEmpty()) {
            propagateBlockLights(zone, lightQueue);
        }

    }

    public static void propagateBlockLights(StructureWorld zone, Queue<BlockPos> lightQueue) {
        while(lightQueue.notEmpty()) {
            BlockPos position = lightQueue.removeFirst();
            int lpacked = position.getBlockLight();
            int r = (lpacked & 3840) >> 8;
            int g = (lpacked & 240) >> 4;
            int b = lpacked & 15;
            BlockState blockState = position.getBlockState();
            if (blockState != null) {
                r = Math.max(r, blockState.lightLevelRed);
                g = Math.max(g, blockState.lightLevelGreen);
                b = Math.max(b, blockState.lightLevelBlue);
            }

            Direction[] var8 = Direction.ALL_DIRECTIONS;

            for (Direction d : var8) {
                BlockPos neighbourPos = position.getOffsetBlockPos(zone, d);
                if (neighbourPos != null) {
                    int lnpacked = neighbourPos.getBlockLight();
                    int nr = (lnpacked & 3840) >> 8;
                    int ng = (lnpacked & 240) >> 4;
                    int nb = lnpacked & 15;
                    BlockState block = neighbourPos.getBlockState();
                    if (block == null || block.lightAttenuation != 15) {
                        int atten = Math.max(block.lightAttenuation, 1);
                        if (nr < r - atten || ng < g - atten || nb < b - atten) {
                            int ir = Math.max(nr, r - atten);
                            int ig = Math.max(ng, g - atten);
                            int ib = Math.max(nb, b - atten);
                            neighbourPos.setBlockLight(ir, ig, ib);
                            neighbourPos.chunk.flagTouchingChunksForRemeshing(zone, neighbourPos.x, neighbourPos.y, neighbourPos.z, false);
                            if (ir > 1 || ig > 1 || ib > 1) {
                                lightQueue.addLast(neighbourPos);
                            }
                        }
                    }
                }
            }

            position.free();
        }

    }
}
