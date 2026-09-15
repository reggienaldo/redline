package net.reggie.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class ModRenderLayers {

    /*
     * Inverse Hull RenderLayer
     * Für Ryou Outline Glow
     */
    public static RenderLayer getInverseHull(Identifier texture) {
        return RenderLayer.of(
                "twopiece_inverse_hull",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                256,
                false,
                true,
                RenderLayer.MultiPhaseParameters.builder()

                        // Shader Program
                        .program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM)

                        // Texture
                        .texture(new RenderPhase.Texture(texture, false, false))

                        // Glow blending
                        .transparency(RenderPhase.ADDITIVE_TRANSPARENCY)

                        // Cull enabled = inverse shell effect cleaner
                        .cull(RenderPhase.ENABLE_CULLING)

                        // Lightmap support
                        .lightmap(RenderPhase.ENABLE_LIGHTMAP)

                        // Overlay UV support
                        .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)

                        // Write depth + color
                        .writeMaskState(RenderPhase.ALL_MASK)

                        .build(true)
        );
    }
}
