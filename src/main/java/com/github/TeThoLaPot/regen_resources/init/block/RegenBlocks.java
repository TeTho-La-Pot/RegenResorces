package com.github.TeThoLaPot.regen_resources.init.block;

import com.github.TeThoLaPot.regen_resources.init.entity.BlockEntities;
import com.github.TeThoLaPot.regen_resources.init.entity.RegenBlockEntity;
import com.github.TeThoLaPot.regen_resources.network.RegenMimicClientCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

public class RegenBlocks extends Block implements EntityBlock {

    public static final EnumProperty<RegenVisual> VISUAL =
            EnumProperty.create("visual", RegenVisual.class);

    public RegenBlocks(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VISUAL, RegenVisual.STONE_PRESET));
    }

    /**
     * 硬度・適正ツール・破壊音などの参照用 {@link BlockState}。
     * {@link RegenVisual#MIMIC} のときのみ BE／ネットワーク Hint の復元対象鉱石を見る。
     * それ以外はプリセットに対応した石・深層岩・ネザーラック・エンドストーン等を見る。
     */
    public static BlockState mimicStateAt(BlockGetter level, BlockPos pos) {
        return mimicStateAt(level, pos, level.getBlockState(pos));
    }

    /**
     * Mixin の {@code getDestroySpeed} など、既に {@code shell} を持つ呼び出しから二重の {@code getBlockState} を避ける。
     */
    public static BlockState mimicStateAt(BlockGetter level, BlockPos pos, BlockState shell) {
        if (shell.hasProperty(VISUAL) && shell.is(Re_Blocks.REGEN_BLOCK.get())) {
            RegenVisual v = shell.getValue(VISUAL);
            if (v != RegenVisual.MIMIC) {
                return v.presetMiningMaterial();
            }
        }
        if (level instanceof Level levelClient && levelClient.isClientSide()) {
            BlockState hinted = RegenMimicClientCache.peek(levelClient, pos);
            if (hinted != null) {
                return hinted;
            }
        }
        /*
         * {@link BlockGetter#getBlockEntity} はサーバーでチャンク昇格を起こし得る。スポーン準備中の再入で固まるため、
         * キャッシュ済み FULL チャンクは {@link LevelChunk#getBlockEntity} だけ使う。
         */
        RegenBlockEntity regen = regenBlockEntityIfReady(level, pos);
        if (regen != null) {
            return regen.getMimicState();
        }
        return Blocks.STONE.defaultBlockState();
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return mimicStateAt(level, pos).getExplosionResistance(level, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return mimicStateAt(level, pos).getSoundType();
    }

    /**
     * タグだけでは適正ツールTierを表せないので、復元対象ブロックの判定に丸める。
     */
    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return ForgeHooks.isCorrectToolForDrops(mimicStateAt(level, pos), player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VISUAL);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(VISUAL) == RegenVisual.MIMIC ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(VISUAL) != RegenVisual.MIMIC) {
            return Shapes.block();
        }
        RegenBlockEntity regen = regenBlockEntityIfReady(level, pos);
        if (regen == null) {
            return Shapes.block();
        }
        BlockState mimic = regen.getMimicState();
        try {
            return mimic.getShape(level, pos, ctx);
        } catch (RuntimeException ignored) {
            return Shapes.block();
        }
    }

    /**
     * サーバーでは {@link LevelChunk#getBlockEntity} のみ（昇格を起こさない {@link net.minecraft.server.level.ServerChunkCache#getChunkNow}）。
     * クライアントは従来どおり {@link BlockGetter#getBlockEntity}。
     */
    private static @Nullable RegenBlockEntity regenBlockEntityIfReady(BlockGetter level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            LevelChunk chunk = serverLevel.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
                return null;
            }
            BlockEntity be = chunk.getBlockEntity(pos);
            return be instanceof RegenBlockEntity r ? r : null;
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof RegenBlockEntity r ? r : null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntities.REGEN_ORE_ENTITY.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return null;
    }
}
