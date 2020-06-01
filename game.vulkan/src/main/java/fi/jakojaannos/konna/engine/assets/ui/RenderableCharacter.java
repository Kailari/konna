package fi.jakojaannos.konna.engine.assets.ui;

import org.lwjgl.stb.STBTTAlignedQuad;

import java.nio.ByteBuffer;

import fi.jakojaannos.konna.engine.view.TexturedQuad;

import static org.lwjgl.system.MemoryUtil.nmemAllocChecked;

public class RenderableCharacter extends STBTTAlignedQuad implements TexturedQuad {
    public static RenderableCharacter malloc() {
        return wrap(RenderableCharacter.class, nmemAllocChecked(SIZEOF));
    }

    private RenderableCharacter(final ByteBuffer container) {
        super(container);
    }

    @Override
    public float u0() {
        return s0();
    }

    @Override
    public float u1() {
        return s1();
    }

    @Override
    public float v0() {
        return t0();
    }

    @Override
    public float v1() {
        return t1();
    }
}
