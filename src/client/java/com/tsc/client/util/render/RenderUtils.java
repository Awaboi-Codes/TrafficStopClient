package com.tsc.client.util.render;

import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.tsc.client.TrafficStopClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

import com.tcc.tscmain;

public class RenderUtils implements ClientModInitializer {
    private static RenderUtils instance;

    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("tsc-client", "pipeline/debug_filled_box_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    );

    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private BufferBuilder buffer;

    private static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("tsc-client", "pipeline/lines_through_walls"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );

    private static final ByteBufferBuilder lineAllocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private BufferBuilder lineBuffer;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private MappableRingBuffer vertexBuffer;
    private MappableRingBuffer lineVertexBuffer;

    public static RenderUtils getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::renderLoop);
    }

    private void renderLoop(WorldRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 1. Get the current camera position from the render state context
        Vec3 camera = context.worldState().cameraRenderState.pos;
        PoseStack matrices = context.matrices();

// 2. FIX: Grab the modern orientation quaternion directly from the state context
// Do NOT use mc.gameRenderer.getMainCamera() here
        org.joml.Quaternionf camRotation = context.worldState().cameraRenderState.orientation;

// 3. Compute the forward look vector relative to the camera
        org.joml.Vector3f forward = new org.joml.Vector3f(0, 0, -1).rotate(camRotation);

// 4. Increase this value so it projects far enough out to be visible (e.g., 5-10 blocks)
        float blocksForward = 5.0f;

        LocalPlayer plr = mc.player;

        // 1. Get the player's eye coordinates
        Vec3 eyePos = plr.getEyePosition();

// 2. Get the player's look vector
        Vec3 lookVec = plr.getLookAngle();

// 3. Compute the offset position 0.3 blocks ahead
        Vec3 targetPos = eyePos.add(lookVec.scale(0.3));

        double tracerStartX = targetPos.x;
        double tracerStartY = targetPos.y;
        double tracerStartZ = targetPos.z;

// Now, draw your line/box vertex from (tracerStartX, tracerStartY, tracerStartZ)
// to (tracerEndX, tracerEndY, tracerEndZ)


        /*/ if (TrafficStopClient.isPlayerESP) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof Player player)) continue;
                if (player == mc.player) continue;

                matrices.pushPose();
                matrices.translate(-camera.x, -camera.y, -camera.z);

                // Filled box
                if (buffer == null) {
                    buffer = new BufferBuilder(allocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
                }
                AABB box = player.getBoundingBox();
                renderFilledBox(matrices.last().pose(), buffer,
                        (float) box.minX, (float) box.minY, (float) box.minZ,
                        (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                        0f, 1f, 0f, 0.3f);

                // Tracer to entity center (bottom of bounding box for a clean look)
                if (lineBuffer == null) {
                    lineBuffer = new BufferBuilder(lineAllocator, LINES_THROUGH_WALLS.getVertexFormatMode(), LINES_THROUGH_WALLS.getVertexFormat());
                }
                float targetX = (float)(box.minX + box.maxX) / 2f;
                float targetY = (float) box.minY;
                float targetZ = (float)(box.minZ + box.maxZ) / 2f;
                renderTracer(matrices.last().pose(), lineBuffer,
                        tracerStartX, tracerStartY, tracerStartZ,
                        targetX, targetY, targetZ,
                        0f, 1f, 0f, 0.9f); // bright green

                matrices.popPose();
            }
        } /*/

        if (TrafficStopClient.isPlayerESP) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                // 1. Check if it's a Player first, and handle the local player check safely
                if (entity instanceof Player player) {
                    if (player == mc.player) continue;
                }

                matrices.pushPose();
                matrices.translate(-camera.x, -camera.y, -camera.z);

                // 3. Use the base entity to get the bounding box so it works for both classes
                AABB box = entity.getBoundingBox();

                // Filled box
                if (buffer == null) {
                    buffer = new BufferBuilder(allocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
                }
                renderFilledBox(matrices.last().pose(), buffer, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.maxY, (float) box.maxZ, 0f, 1f, 0f, 0.3f);

                // Tracer to entity center
                if (lineBuffer == null) {
                    lineBuffer = new BufferBuilder(lineAllocator, LINES_THROUGH_WALLS.getVertexFormatMode(), LINES_THROUGH_WALLS.getVertexFormat());
                }
                float targetX = (float)(box.minX + box.maxX) / 2f;
                float targetY = (float)(box.minY + box.maxY) / 2f;
                float targetZ = (float)(box.minZ + box.maxZ) / 2f;
                renderTracer(matrices.last().pose(), lineBuffer, (float)tracerStartX, (float)tracerStartY, (float)tracerStartZ, targetX, targetY, targetZ, 0f, 1f, 0f, 0.9f);

                matrices.popPose();
            }
        }

        if (TrafficStopClient.isChestESP) {
            int playerChunkX = mc.player.chunkPosition().x;
            int playerChunkZ = mc.player.chunkPosition().z;
            int chunkRadius = 8;

            for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
                for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
                    LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;

                    chunk.getBlockEntities().forEach((pos, blockEntity) -> {
                        Block block = mc.level.getBlockState(pos).getBlock();
                        if (!EspUtils.CHEST_ESP_BLOCKS.contains(block)) return;

                        AABB box = new AABB(pos);
                        matrices.pushPose();
                        matrices.translate(-camera.x, -camera.y, -camera.z);

                        if (buffer == null) {
                            buffer = new BufferBuilder(allocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
                        }
                        renderFilledBox(matrices.last().pose(), buffer,
                                (float) box.minX, (float) box.minY, (float) box.minZ,
                                (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                                1f, 0.5f, 0f, 0.3f);

                        matrices.popPose();
                    });
                }
            }
        }

        if (TrafficStopClient.isBaseESP) {
            int playerChunkX = mc.player.chunkPosition().x;
            int playerChunkZ = mc.player.chunkPosition().z;
            int chunkRadius = 8;

            for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
                for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
                    LevelChunk chunk = mc.level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null) continue;

                    chunk.getBlockEntities().forEach((pos, blockEntity) -> {
                        Block block = mc.level.getBlockState(pos).getBlock();
                        if (!EspUtils.BASE_ESP_BLOCKS.contains(block)) return;

                        AABB box = new AABB(pos);
                        matrices.pushPose();
                        matrices.translate(-camera.x, -camera.y, -camera.z);

                        if (buffer == null) {
                            buffer = new BufferBuilder(allocator, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
                        }
                        renderFilledBox(matrices.last().pose(), buffer,
                                (float) box.minX, (float) box.minY, (float) box.minZ,
                                (float) box.maxX, (float) box.maxY, (float) box.maxZ,
                                1f, 0.5f, 0f, 0.3f);

                        matrices.popPose();
                    });
                }
            }
        }

        if (buffer != null) {
            drawFilledThroughWalls(mc, FILLED_THROUGH_WALLS);
        }
        if (lineBuffer != null) {
            drawLinesThroughWalls(mc, LINES_THROUGH_WALLS);
        }
    }

    /**
     * Queues a tracer line from (x1,y1,z1) to (x2,y2,z2) into the line buffer.
     * The LINES vertex format requires each vertex to also carry the "other" endpoint
     * as a normal, which Minecraft's line shader uses to expand lines to a set width.
     */
    private void renderTracer(Matrix4fc pose, BufferBuilder buf,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float r, float g, float b, float a) {
        // Direction vector (used as the "normal" for line width expansion)
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len == 0) return;
        float nx = dx / len, ny = dy / len, nz = dz / len;

        buf.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(1.0f);
        buf.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(nx, ny, nz).setLineWidth(1.0f);
    }

    private void renderFilledBox(Matrix4fc positionMatrix, BufferBuilder buffer,
                                 float minX, float minY, float minZ,
                                 float maxX, float maxY, float maxZ,
                                 float red, float green, float blue, float alpha) {
        // Front Face
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Back face
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        // Left face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Right face
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Top face
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Bottom face
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    private void drawFilledThroughWalls(Minecraft client, RenderPipeline pipeline) {
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();
        GpuBuffer vertices = upload(drawParameters, format, builtBuffer, false);
        draw(client, pipeline, builtBuffer, drawParameters, vertices, format, false);
        vertexBuffer.rotate();
        buffer = null;
    }

    private void drawLinesThroughWalls(Minecraft client, RenderPipeline pipeline) {
        MeshData builtBuffer = lineBuffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();
        GpuBuffer vertices = upload(drawParameters, format, builtBuffer, true);
        draw(client, pipeline, builtBuffer, drawParameters, vertices, format, true);
        lineVertexBuffer.rotate();
        lineBuffer = null;
    }

    private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer, boolean isLines) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();
        MappableRingBuffer target = isLines ? lineVertexBuffer : vertexBuffer;

        if (target == null || target.size() < vertexBufferSize) {
            if (target != null) target.close();
            target = new MappableRingBuffer(
                    () -> tscmain.MOD_ID + (isLines ? " line" : " fill") + " render pipeline",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    vertexBufferSize
            );
            if (isLines) lineVertexBuffer = target;
            else vertexBuffer = target;
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(
                target.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }
        return target.currentBuffer();
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer,
                             MeshData.DrawState drawParameters, GpuBuffer vertices,
                             VertexFormat format, boolean isLines) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> tscmain.MOD_ID + (isLines ? " line" : " fill") + " render pipeline rendering",
                        client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
                        client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        allocator.close();
        lineAllocator.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        if (lineVertexBuffer != null) {
            lineVertexBuffer.close();
            lineVertexBuffer = null;
        }
    }
}