package universalcoins.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import universalcoins.UniversalCoins;
import universalcoins.tile.TilePowerBase;
import universalcoins.tile.TilePowerReceiver;

public class BlockPowerReceiver extends BlockRotatable {

	public BlockPowerReceiver() {
		setHardness(3.0F);
		setCreativeTab(UniversalCoins.tabUniversalCoins);
		setResistance(30.0F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side,
			float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TilePowerReceiver) {
			TilePowerReceiver tentity = (TilePowerReceiver) te;
			if (player.getName().matches(tentity.blockOwner)) {
				player.openGui(UniversalCoins.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player,
			ItemStack stack) {
		if (world.isRemote)
			return;
		if (stack.hasTagCompound()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TilePowerReceiver) {
				TilePowerReceiver tentity = (TilePowerReceiver) te;
				NBTTagCompound tagCompound = stack.getTagCompound();
				if (tagCompound == null) {
					return;
				}
				NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < tagList.tagCount(); i++) {
					NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
					byte slot = tag.getByte("Slot");
					if (slot >= 0 && slot < tentity.getSizeInventory()) {
						tentity.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(tag));
					}
				}
				tentity.resetPowerDirection();
				tentity.coinSum = tagCompound.getInteger("coinSum");
				tentity.rfLevel = tagCompound.getInteger("rfLevel");
			}
			world.markBlockForUpdate(pos);
		}
		((TilePowerReceiver) world.getTileEntity(pos)).blockOwner = player.getName();
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		String ownerName = ((TilePowerReceiver) world.getTileEntity(pos)).blockOwner;
		if (player.capabilities.isCreativeMode) {
			super.removedByPlayer(world, pos, player, willHarvest);
			return false;
		}
		if (player.getDisplayName().equals(ownerName) && !world.isRemote) {
			ItemStack stack = getItemStackWithData(world, pos.getX(), pos.getY(), pos.getZ());
			EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			world.spawnEntityInWorld(entityItem);
			super.removedByPlayer(world, pos, player, willHarvest);
		}
		return false;
	}

	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		String ownerName = ((TilePowerBase) world.getTileEntity(new BlockPos(x, y, z))).blockOwner;
		if (player.getDisplayName().equals(ownerName)) {
			this.setHardness(3.0F);
		} else {
			this.setHardness(-1.0F);
		}
	}

	public ItemStack getItemStackWithData(World world, int x, int y, int z) {
		ItemStack stack = new ItemStack(UniversalCoins.proxy.blockPowerReceiver);
		TileEntity tentity = world.getTileEntity(new BlockPos(x, y, z));
		if (tentity instanceof TilePowerReceiver) {
			TilePowerReceiver te = (TilePowerReceiver) tentity;
			NBTTagList itemList = new NBTTagList();
			NBTTagCompound tagCompound = new NBTTagCompound();
			for (int i = 0; i < te.getSizeInventory(); i++) {
				ItemStack invStack = te.getStackInSlot(i);
				if (invStack != null) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setByte("Slot", (byte) i);
					invStack.writeToNBT(tag);
					itemList.appendTag(tag);
				}
			}
			tagCompound.setInteger("coinSum", te.coinSum);
			tagCompound.setInteger("rfLevel", te.rfLevel);
			stack.setTagCompound(tagCompound);
			return stack;
		} else
			return stack;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TilePowerReceiver();
	}

	@Override
	 public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		TilePowerReceiver tileEntity = (TilePowerReceiver) world.getTileEntity(pos);
		tileEntity.resetPowerDirection();
	}
}