package me.zombii.horizon.mesh;

public interface IPhysicChunk {

    boolean needsRemeshing();
    void setNeedsRemeshing(boolean remeshing);

}
