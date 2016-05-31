package com.choonster.testmod3;

import com.choonster.testmod3.client.gui.GuiHandler;
import com.choonster.testmod3.config.Config;
import com.choonster.testmod3.event.BlockEventHandler;
import com.choonster.testmod3.event.NetworkEventHandler;
import com.choonster.testmod3.event.PlayerEventHandler;
import com.choonster.testmod3.init.*;
import com.choonster.testmod3.proxy.IProxy;
import com.choonster.testmod3.tests.Tests;
import com.choonster.testmod3.tweak.snowbuildup.SnowBuildup;
import com.choonster.testmod3.util.BlockDumper;
import com.choonster.testmod3.world.gen.WorldGenOres;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.UUID;

@Mod(modid = TestMod3.MODID, guiFactory = "com.choonster.testmod3.config.GuiConfigFactoryTestMod3")
public class TestMod3 {
	public static final String MODID = "testmod3";

	public static CreativeTabTestMod3 creativeTab;

	@SidedProxy(clientSide = "com.choonster.testmod3.proxy.CombinedClientProxy", serverSide = "com.choonster.testmod3.proxy.DedicatedServerProxy")
	public static IProxy proxy;

	@Instance(MODID)
	public static TestMod3 instance;

	public static SimpleNetworkWrapper network;

	static {
		FluidRegistry.enableUniversalBucket(); // Must be called before preInit
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Logger.setLogger(event.getModLog());

		FMLLog.bigWarning("Random UUID: %s", UUID.randomUUID().toString());

		creativeTab = new CreativeTabTestMod3();
		Config.load(event);

		ModCapabilities.registerCapabilities();

		MinecraftForge.EVENT_BUS.register(new BlockEventHandler());
		MinecraftForge.EVENT_BUS.register(new NetworkEventHandler());
		MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());

		network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

		ModSoundEvents.registerSounds();
		ModMessages.registerMessages();
		ModFluids.registerFluids();
		ModBlocks.registerBlocks();
		ModBlocks.registerTileEntities();
		ModItems.registerItems();
		ModFluids.registerFluidContainers();
		ModBiomes.registerBiomes();
		ModMapGen.registerMapGen();
		ModEntities.registerEntities();

		SnowBuildup.init();

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		ModRecipes.registerRecipes();

		GameRegistry.registerWorldGenerator(new WorldGenOres(), 0);

		ModRecipes.removeCraftingRecipes();

		ModMapGen.registerWorldGenerators();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

		FMLInterModComms.sendMessage("Waila", "register", "com.choonster.testmod3.compat.waila.WailaCompat.register");

		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		BlockDumper.dump();

		proxy.postInit();

		Tests.runTests();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		ModCommands.registerCommands(event);
	}
}
