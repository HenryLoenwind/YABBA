package com.latmod.yabba.tile;

import com.feed_the_beast.ftblib.lib.config.IConfigCallback;
import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.latmod.yabba.YabbaItems;
import com.latmod.yabba.api.YabbaConfigEvent;
import com.latmod.yabba.block.BlockAdvancedBarrelBase;
import com.latmod.yabba.item.upgrade.ItemUpgradeRedstone;
import com.latmod.yabba.util.BarrelLook;
import com.latmod.yabba.util.EnumBarrelModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class TileAdvancedBarrelBase extends TileBarrelBase implements IConfigCallback
{
	private BarrelLook look = BarrelLook.DEFAULT;
	public boolean alwaysDisplayData = false;
	public boolean displayBar = false;
	public long lastClick;

	private float cachedRotationY;
	private AxisAlignedBB cachedAABB;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.writeData(nbt, type);

		if (!look.model.isDefault())
		{
			nbt.setString("Model", look.model.getNBTName());
		}

		if (!look.skin.isEmpty())
		{
			nbt.setString("Skin", look.skin);
		}

		if (alwaysDisplayData)
		{
			nbt.setBoolean("AlwaysDisplayData", true);
		}

		if (displayBar)
		{
			nbt.setBoolean("DisplayBar", true);
		}
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
		super.readData(nbt, type);

		look = BarrelLook.get(EnumBarrelModel.getFromNBTName(nbt.getString("Model")), nbt.getString("Skin"));
		alwaysDisplayData = nbt.getBoolean("AlwaysDisplayData");
		displayBar = nbt.getBoolean("DisplayBar");
	}

	@Override
	public void updateContainingBlockInfo()
	{
		super.updateContainingBlockInfo();
		cachedRotationY = -1F;
		cachedAABB = null;
	}

	public float getRotationAngleY()
	{
		if (cachedRotationY == -1F)
		{
			IBlockState state = getBlockState();

			if (!(state.getBlock() instanceof BlockAdvancedBarrelBase))
			{
				return 0F;
			}

			cachedRotationY = state.getValue(BlockAdvancedBarrelBase.FACING).getHorizontalAngle() + 180F;
		}

		return cachedRotationY;
	}

	@Override
	public BarrelLook getLook()
	{
		return look;
	}

	@Override
	public boolean setLook(BarrelLook l, boolean simulate)
	{
		if (!look.equals(l))
		{
			if (!simulate)
			{
				look = l;
				markBarrelDirty(true);
				updateContainingBlockInfo();
			}

			return true;
		}

		return false;
	}

	public boolean canConnectRedstone(@Nullable EnumFacing facing)
	{
		return getUpgradeData(YabbaItems.UPGRADE_REDSTONE_OUT) instanceof ItemUpgradeRedstone.Data;
	}

	public int redstoneOutput(EnumFacing facing)
	{
		return 0;
	}

	public String getItemDisplayName()
	{
		return "ERROR";
	}

	public String getItemDisplayCount(boolean isSneaking, boolean isCreative, boolean infinite)
	{
		return "ERROR";
	}

	public void addItem(EntityPlayer player, EnumHand hand)
	{
	}

	public void addAllItems(EntityPlayer player, EnumHand hand)
	{
	}

	public void removeItem(EntityPlayer player, boolean stack)
	{
	}

	@Override
	public void createConfig(YabbaConfigEvent event)
	{
		super.createConfig(event);

		event.getConfig().addBool("always_display_data", () -> alwaysDisplayData, v -> alwaysDisplayData = v, false).setDisplayName(new TextComponentTranslation("yabba_client.general.always_display_data"));

		if (!tier.infiniteCapacity())
		{
			event.getConfig().addBool("display_bar", () -> displayBar, v -> displayBar = v, false).setDisplayName(new TextComponentTranslation("yabba_client.general.display_bar"));
		}
	}

	public AxisAlignedBB getAABB(IBlockState state)
	{
		if (cachedAABB == null)
		{
			cachedAABB = getLook().model.getAABB(state);
		}

		return cachedAABB;
	}
}