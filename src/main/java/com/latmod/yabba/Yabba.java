package com.latmod.yabba;

import com.feed_the_beast.ftblib.FTBLib;
import com.feed_the_beast.ftblib.lib.OtherMods;
import com.latmod.yabba.net.YabbaNetHandler;
import com.latmod.yabba.tile.TileDecorativeBlock;
import com.latmod.yabba.util.AntibarrelData;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LatvianModder
 */
@Mod(
		modid = Yabba.MOD_ID,
		name = Yabba.MOD_NAME,
		version = "0.0.0.yabba",
		dependencies = FTBLib.THIS_DEP + ";after:" + OtherMods.FORESTRY
)
public class Yabba
{
	public static final String MOD_ID = "yabba";
	public static final String MOD_NAME = "YABBA";

	@Mod.Instance(MOD_ID)
	public static Yabba MOD;

	@SidedProxy(serverSide = "com.latmod.yabba.YabbaCommon", clientSide = "com.latmod.yabba.client.YabbaClient")
	public static YabbaCommon PROXY;

	public static final Logger LOGGER = LogManager.getLogger("YABBA");

	public static final CreativeTabs TAB = new CreativeTabs(MOD_ID)
	{
		@Override
		public ItemStack createIcon()
		{
			ItemStack stack = new ItemStack(YabbaItems.DECORATIVE_BLOCK);
			new TileDecorativeBlock().writeToPickBlock(stack);
			return stack;
		}
	};

	@Mod.EventHandler
	public void onPreInit(FMLPreInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new YabbaGuiHandler());
		YabbaConfig.sync();
		YabbaNetHandler.init();

		CapabilityManager.INSTANCE.register(AntibarrelData.class, new Capability.IStorage<AntibarrelData>()
		{
			@Override
			public NBTBase writeNBT(Capability<AntibarrelData> capability, AntibarrelData instance, EnumFacing side)
			{
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<AntibarrelData> capability, AntibarrelData instance, EnumFacing side, NBTBase nbt)
			{
				if (nbt instanceof NBTTagCompound)
				{
					instance.deserializeNBT((NBTTagCompound) nbt);
				}
			}
		}, () -> new AntibarrelData(null));

		PROXY.preInit();
	}

	@Mod.EventHandler
	public void onPostInit(FMLPostInitializationEvent event)
	{
		PROXY.postInit();
	}
}