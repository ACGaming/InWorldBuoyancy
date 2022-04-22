/*
 * Copyright (C) 2017, 2018 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package mod.acgaming.inworldbuoyancy;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import mod.acgaming.inworldbuoyancy.handler.IWBHandler;
import mod.acgaming.inworldbuoyancy.handler.IWBHandlerCustom;
import mod.acgaming.inworldbuoyancy.utils.FluidReplacer;

@Mod(modid = "inworldbuoyancy", name = "InWorldBuoyancy", version = "${version}", acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "after:betterwithmods")
public class InWorldBuoyancy
{
    public static boolean isLiquid(IBlockState state)
    {
        return state.getBlock() instanceof BlockLiquid || state.getBlock() instanceof IFluidBlock;
    }

    private boolean omitItemInHand, floatBothWays;
    private boolean cannotDisplaceLiquid, inWorldBuoyancy;
    private Configuration config;
    private IWBHandler handler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = new Configuration(event.getSuggestedConfigurationFile());

        omitItemInHand = config.getBoolean("omitItemInHand", "general", false, "Exempts the item in the player's hand.");
        floatBothWays = config.getBoolean("floatBothWays", "general", false, "Makes non-floating items move down as well.");
        cannotDisplaceLiquid = config.getBoolean("cannotDisplaceLiquid", "features", true, "Makes it impossible to easily displace liquids with blocks.");
        inWorldBuoyancy = config.getBoolean("inWorldBuoyancy", "features", true, "Inventory is buoyant. Hah.");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        String[] customItems = config.getStringList("customBuoyancyList", "general", new String[0], "Custom list of items to float. If empty, default handler is used.\nUse registry names (with :) or no : for OreDictionary IDs.");

        if (config.hasChanged())
        {
            config.save();
        }

        if (customItems.length > 0)
        {
            handler = new IWBHandlerCustom(customItems);
        }
        else
        {
            try
            {
                if (Loader.isModLoaded("betterwithmods"))
                {
                    handler = (IWBHandler) Class.forName("mod.acgaming.inworldbuoyancy.handler.IWBHandlerBWM").newInstance();
                }
                else
                {
                    handler = new IWBHandler();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                handler = new IWBHandler();
            }
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldEvent.Load event)
    {
        if (cannotDisplaceLiquid)
        {
            event.getWorld().addEventListener(FluidReplacer.INSTANCE);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        EntityPlayer player = event.player;

        if (!inWorldBuoyancy || player.isCreative())
        {
            return;
        }

        if (event.phase == TickEvent.Phase.END && !player.getEntityWorld().isRemote && player.isInWater() && player.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
        {
            if (player.ticksExisted % 20 == 0)
            {
                World world = player.getEntityWorld();
                IBlockState blockState = world.getBlockState(player.getPosition());
                Fluid fluidAtPlayer = FluidRegistry.lookupFluidForBlock(blockState.getBlock());
                IItemHandler itemHandler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

                if (itemHandler != null)
                {
                    for (int i = 0; i < itemHandler.getSlots(); i++)
                    {
                        ItemStack itemStack = itemHandler.extractItem(i, Integer.MAX_VALUE, true);

                        if (!itemStack.isEmpty())
                        {
                            if (omitItemInHand && player.inventory.getCurrentItem() == itemStack)
                            {
                                return;
                            }
                            if (handler.isFloating(itemStack) || floatBothWays && !handler.isFloating(itemStack))
                            {
                                player.dropItem(itemStack, true, false);
                                player.inventory.decrStackSize(i, itemStack.getCount());
                            }
                            else if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
                            {
                                IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

                                if (fluidHandler != null)
                                {
                                    FluidStack fluidStack = fluidHandler.drain(Fluid.BUCKET_VOLUME, false);
                                    FluidActionResult result = null;

                                    if (fluidStack != null && fluidStack.getFluid() != fluidAtPlayer)
                                    {
                                        result = FluidUtil.tryPlaceFluid(player, world, player.getPosition(), itemStack, fluidStack);
                                    }
                                    else
                                    {
                                        itemStack = fluidHandler.getContainer();
                                        itemStack.setCount(1);
                                        fluidHandler = FluidUtil.getFluidHandler(itemStack);
                                        if (fluidHandler != null)
                                        {
                                            result = FluidUtil.tryPickUpFluid(fluidHandler.getContainer(), player, world, player.getPosition(), EnumFacing.UP);
                                        }
                                    }
                                    if (result != null)
                                    {
                                        if (result.isSuccess())
                                        {
                                            itemHandler.extractItem(i, 1, false);
                                            ItemHandlerHelper.giveItemToPlayer(player, result.getResult(), i);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}