package eu.crackscout.crackcore;


import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import eu.crackscout.crackcore.events.ChestLogger;
import eu.crackscout.crackcore.events.GeneralEventHandler;
import eu.crackscout.crackcore.events.HopperLogger;
import eu.crackscout.crackcore.utils.DatabaseManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(CrackCoreMod.MODID)
public class CrackCoreMod {
	
	// Define mod id in a common place for everything to reference
    public static final String MODID = "crackcoremod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    
/*
 *
 * Adding Items and Blocks as need - not needed now (REL: ITEM-BLOCK_INT)
 * 
 */    
//    // Create a Deferred Register to hold Blocks which will all be registered under the "crackcoremod" namespace
//    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
//    // Create a Deferred Register to hold Items which will all be registered under the "crackcoremod" namespace
//    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
//    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "crackcoremod" namespace
//    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
//
//    // Creates a new Block with the id "crackcoremod:example_block", combining the namespace and path
//    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
//    // Creates a new BlockItem with the id "crackcoremod:example_block", combining the namespace and path
//    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));
//
//    // Creates a new food item with the id "crackcoremod:example_id", nutrition 1 and saturation 2
//    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
//            .alwaysEat().nutrition(1).saturationMod(2f).build())));
//
//    // Creates a creative tab with the id "crackcoremod:example_tab" for the example item, that is placed after the combat tab
//    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
//            .withTabsBefore(CreativeModeTabs.COMBAT)
//            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
//            .displayItems((parameters, output) -> {
//                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
//            }).build());
//
//    
    private static DatabaseManager databaseManager;
    
    public CrackCoreMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

//(REL: ITEM-BLOCK_INT)     
//        // Register the Deferred Register to the mod event bus so blocks get registered
//        BLOCKS.register(modEventBus);
//        // Register the Deferred Register to the mod event bus so items get registered
//        ITEMS.register(modEventBus);
//        // Register the Deferred Register to the mod event bus so tabs get registered
//        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        // Register a different class as event Handler 
//        MinecraftForge.EVENT_BUS.register(new GeneralEventHandler());
        MinecraftForge.EVENT_BUS.register(new ChestLogger());
        MinecraftForge.EVENT_BUS.register(new HopperLogger());

//        // Register the item to a creative tab
//        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // Datenbankverbindung herstellen
//       databaseManager = new DatabaseManager();
//        try {
//            databaseManager.connect("localhost", 3306, "database", "user", "password");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

//    // Add the example block item to the building blocks tab
//    private void addCreative(BuildCreativeModeTabContentsEvent event)
//    {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
//            event.accept(EXAMPLE_BLOCK_ITEM);
//    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    
    
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
