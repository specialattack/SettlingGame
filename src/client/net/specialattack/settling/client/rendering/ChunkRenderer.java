
package net.specialattack.settling.client.rendering;

import net.specialattack.settling.common.world.Chunk;

import org.lwjgl.opengl.GL11;

public class ChunkRenderer {

    public int glCallList = -1;
    public boolean dirty = true;
    public final Chunk chunk;

    public ChunkRenderer(Chunk chunk) {
        this.chunk = chunk;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void createGlChunk() {
        if (this.glCallList < 0) {
            this.glCallList = GL11.glGenLists(1);
        }
        GL11.glNewList(this.glCallList, GL11.GL_COMPILE);
        GL11.glTranslated((float) -this.chunk.chunkX * 16.0F, 0.0F, (float) -this.chunk.chunkZ * 16.0F);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                TileRenderer.renderTile(this.chunk.section.world, x + chunk.chunkX * 16, z + chunk.chunkZ * 16);
            }
        }
        GL11.glEndList();

        this.dirty = false;
    }

    public void renderChunk() {
        if (this.glCallList > 0 && !this.dirty) {
            GL11.glPushMatrix();
            GL11.glTranslated((float) this.chunk.chunkX * 16.0F, 0.0F, (float) this.chunk.chunkZ * 16.0F);
            GL11.glCallList(this.glCallList);
            GL11.glPopMatrix();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.chunk == null) ? 0 : this.chunk.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ChunkRenderer other = (ChunkRenderer) obj;
        if (this.chunk == null) {
            if (other.chunk != null) {
                return false;
            }
        }
        else if (!this.chunk.equals(other.chunk)) {
            return false;
        }
        return true;
    }

}
