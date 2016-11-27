package universalcoins.items;

import java.text.DecimalFormat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import universalcoins.UniversalCoins;
import universalcoins.util.UniversalAccounts;

public class ItemEnderCard extends ItemUCCard {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote)
			return EnumActionResult.FAIL;
		if (stack.getTagCompound() == null) {
			createNBT(stack, world, player);
		}
		long accountBalance = UniversalAccounts.getInstance()
				.getAccountBalance(stack.getTagCompound().getString("Account"));
		DecimalFormat formatter = new DecimalFormat("#,###,###,###");
		NonNullList<ItemStack> inventory = player.inventory.mainInventory;
		String accountNumber = stack.getTagCompound().getString("Account");
		int coinValue = 0;
		int depositAmount = 0;
		int coinsDeposited = 0;
		for (int i = 0; i < inventory.size(); i++) {
			ItemStack instack = player.inventory.getStackInSlot(i);
			if (instack != null) {
				switch (instack.getUnlocalizedName()) {
				case "item.iron_coin":
					coinValue = UniversalCoins.coinValues[0];
					break;
				case "item.gold_coin":
					coinValue = UniversalCoins.coinValues[1];
					break;
				case "item.emerald_coin":
					coinValue = UniversalCoins.coinValues[2];
					break;
				case "item.diamond_coin":
					coinValue = UniversalCoins.coinValues[3];
					break;
				case "item.obsidian_coin":
					coinValue = UniversalCoins.coinValues[4];
					break;
				}
				if (accountBalance == -1)
					// get out of here if the card is invalid
					return EnumActionResult.FAIL;
				if (coinValue == 0)
					continue;
				if (Long.MAX_VALUE - accountBalance > coinValue * instack.getCount()) {
					depositAmount = instack.getCount();
				} else {
					depositAmount = (int) (Long.MAX_VALUE - accountBalance) / coinValue;
				}
				UniversalAccounts.getInstance().creditAccount(accountNumber, coinValue * depositAmount);
				coinsDeposited += coinValue * depositAmount;
				ItemStack itemStack = inventory.get(i);
				itemStack.setCount(itemStack.getCount() - depositAmount);
				if (itemStack.getCount() == 0) {
					player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
					player.inventoryContainer.detectAndSendChanges();
				}
			}
		}
		if (coinsDeposited > 0) {
			player.sendMessage(new TextComponentString(I18n.translateToLocal("item.card.deposit") + " "
					+ formatter.format(coinsDeposited) + " " + I18n.translateToLocal(
							coinsDeposited > 1 ? "general.currency.multiple" : "general.currency.single")));
		}
		player.sendMessage(new TextComponentString(I18n.translateToLocal("item.card.balance") + " "
				+ formatter.format(UniversalAccounts.getInstance().getAccountBalance(accountNumber))));
		return EnumActionResult.FAIL;
	}

}