package sculktransporting.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import sculktransporting.blockentities.BaseSculkItemTransporterBlockEntity;

public abstract class BaseSculkItemTransporterBlock extends SculkSensorBlock {
	public BaseSculkItemTransporterBlock(Properties properties) {
		super(properties, 8); //maybe make range configurable? -R
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		SculkSensorPhase phase = getPhase(state);

		if (phase == SculkSensorPhase.COOLDOWN)
			level.setBlockAndUpdate(pos, state.setValue(PHASE, SculkSensorPhase.INACTIVE));
		else if (phase == SculkSensorPhase.ACTIVE && level.getBlockEntity(pos) instanceof BaseSculkItemTransporterBlockEntity be && !be.hasStoredItemSignal())
			deactivate(level, pos, state);
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {}

	@Override
	public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

	@Override
	public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type);

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof BaseSculkItemTransporterBlockEntity be) {
				if (be.hasStoredItemSignal())
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getStoredItemSignal());
				else if (be.getListener().receivingEvent != null && be.getListener().receivingEvent.entity() instanceof ItemEntity item)
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), item.getItem());
			}

			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	public static void deactivate(Level level, BlockPos pos, BlockState state) {
		SculkSensorBlock.deactivate(level, pos, state);
		level.setBlockAndUpdate(pos, state.setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, 0)); //skip SculkSensorPhase.COOLDOWN to reduce delay
	}

	public static void activate(Entity entity, Level level, BlockPos pos, BlockState state, int distance) {
		level.scheduleTick(pos, state.getBlock(), 0);
		SculkSensorBlock.activate(entity, level, pos, state, distance);
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return 0; //TODO: maybe comparator output signal should be how many items the block is currently holding? -R
	}

	@Override
	public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
		return 0;
	}
}