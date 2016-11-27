package universalcoins.blocks;

import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import universalcoins.UniversalCoins;
import universalcoins.tileentity.TileUCSign;

public class BlockUCWallSign extends BlockWallSign {

	private Class signEntityClass;
	private boolean isStanding;

	public BlockUCWallSign(Class tileEntity) {
		super();
		this.isStanding = false;
		this.signEntityClass = tileEntity;
		float f = 0.25F;
		float f1 = 1.0F;
		//this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
		 this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		this.getBoundingBox(blockState, worldIn, pos);
		return super.getSelectedBoundingBox(blockState, worldIn, pos);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_) {
		try {
			return (TileUCSign) this.signEntityClass.newInstance();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		UUID owner = ((TileUCSign) world.getTileEntity(pos)).blockOwnerId;
		if (player.getDisplayName().equals(owner)) {
			this.setHardness(1.0F);
		} else {
			this.setHardness(-1.0F);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity instanceof TileUCSign) {
			TileUCSign tentity = (TileUCSign) tileEntity;
			if (player.getUniqueID().equals(tentity.blockOwnerId)) {
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		UUID owner = ((TileUCSign) world.getTileEntity(pos)).blockOwnerId;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(state, world, pos, player, willHarvest);
			return true;
		}
		if (player.getDisplayName().equals(owner) && !world.isRemote) {
			super.removedByPlayer(state, world, pos, player, willHarvest);
			return true;
		}
		return false;
	}

	//@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (neighborBlock.getLocalizedName().matches("Chest") && tileEntity != null
				&& tileEntity instanceof TileUCSign) {
			((TileUCSign) tileEntity).scanChestContents();
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return UniversalCoins.proxy.uc_sign;
	}
}
