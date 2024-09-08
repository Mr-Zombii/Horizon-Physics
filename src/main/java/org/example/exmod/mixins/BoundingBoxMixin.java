package org.example.exmod.mixins;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import org.example.exmod.boundingBox.OrientedBoundingBoxGetter;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoundingBox.class)
public abstract class BoundingBoxMixin implements OrientedBoundingBoxGetter {

    @Shadow @Final public Vector3 min;
    @Shadow @Final public Vector3 max;

    @Shadow public abstract BoundingBox set(Vector3 minimum, Vector3 maximum);

    @Unique
    OrientedBoundingBox exampleMod$innerBoundingBox;

    @Inject(method = "set(Lcom/badlogic/gdx/math/collision/BoundingBox;)Lcom/badlogic/gdx/math/collision/BoundingBox;", at = @At("HEAD"))
    public void set0(BoundingBox bounds, CallbackInfoReturnable<BoundingBox> cir) {
        if (((OrientedBoundingBoxGetter)bounds).hasInnerBounds()) {
            exampleMod$innerBoundingBox = ((OrientedBoundingBoxGetter)bounds).getInnerBounds();
        } else exampleMod$innerBoundingBox = null;
    }

    @Override
    public boolean hasInnerBounds() {
        return exampleMod$innerBoundingBox != null;
    }

    @Override
    public OrientedBoundingBox getInnerBounds() {
        return exampleMod$innerBoundingBox;
    }

    @Override
    public void setInnerBounds(OrientedBoundingBox boundingBox) {
        exampleMod$innerBoundingBox = boundingBox;
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner000(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner000(out);
        } else {
            return out.set(min.x, min.y, min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner001(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner001(out);
        } else {
            return out.set(this.min.x, this.min.y, this.max.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner010(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner010(out);
        } else {
            return out.set(this.min.x, this.max.y, this.min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner011(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner011(out);
        } else {
            return out.set(this.min.x, this.max.y, this.max.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner100(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner100(out);
        } else {
            return out.set(this.max.x, this.min.y, this.min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner101(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner101(out);
        } else {
            return out.set(this.max.x, this.min.y, this.max.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner110(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner110(out);
        } else {
            return out.set(this.max.x, this.max.y, this.min.z);
        }
    }

    /**
     * @author Mr_Zombii
     * @reason Add OrientedBoundingBoxCompat
     */
    @Overwrite
    public Vector3 getCorner111(Vector3 out) {
        if (hasInnerBounds()) {
            return getInnerBounds().getCorner111(out);
        } else {
            return out.set(this.max.x, this.max.y, this.max.z);
        }
    }

}
