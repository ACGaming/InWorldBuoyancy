package pl.asie.inventorybuoyancy;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nullable;

public class HopeLifter implements IWorldEventListener {
	public static final HopeLifter INSTANCE = new HopeLifter();
	public static boolean checkingForMixing = false;

	private HopeLifter() {

	}

	@Override
	public void notifyBlockUpdate(World worldIn, BlockPos oPos, IBlockState oldState, IBlockState newState, int flags) {
		if (checkingForMixing) {
			return;
		}

		if (oldState.getBlock() != newState.getBlock() && InventoryBuoyancy.isLiquid(oldState)
				&& newState.getMaterial() != Material.AIR && !newState.getBlock().isReplaceable(worldIn, oPos)) {

			Fluid f = FluidRegistry.lookupFluidForBlock(oldState.getBlock());
			boolean isGaseous = f != null && f.isGaseous();

			for (EnumFacing facing : EnumFacing.VALUES) {
				int i = 32;
				if (!isGaseous && facing == EnumFacing.DOWN) {
					continue;
				} else if (isGaseous && facing == EnumFacing.UP) {
					continue;
				}

				BlockPos pos = oPos.offset(facing);
				while (worldIn.isValid(pos) && i > 0) {
					IBlockState state = worldIn.getBlockState(pos);
					if (!InventoryBuoyancy.isLiquid(state) && state.getBlock().isReplaceable(worldIn, pos)) {
						break;
					}
					pos = pos.offset(facing);
					i--;
				}

				if (worldIn.isValid(pos)) {
					checkingForMixing = true;
					worldIn.setBlockState(pos, oldState);
					checkingForMixing = false;
					break;
				}
			}
		}
	}

	@Override
	public void notifyLightSet(BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {

	}

	@Override
	public void playRecord(SoundEvent soundIn, BlockPos pos) {

	}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

	}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

	}

	@Override
	public void onEntityAdded(Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(Entity entityIn) {

	}

	@Override
	public void broadcastSound(int soundID, BlockPos pos, int data) {

	}

	@Override
	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {

	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {

	}
}
