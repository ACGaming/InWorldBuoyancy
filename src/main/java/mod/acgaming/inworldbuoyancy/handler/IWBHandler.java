/*
 * Copyright (C) 2017, 2018, 2022 Adrian Siekierka, ACGaming
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package mod.acgaming.inworldbuoyancy.handler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import betterwithmods.module.hardcore.world.HCBuoy;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import mod.acgaming.inworldbuoyancy.InWorldBuoyancy;
import mod.acgaming.inworldbuoyancy.config.IWBConfig;
import mod.acgaming.inworldbuoyancy.util.IWBFluidReplacer;

@Mod.EventBusSubscriber(modid = InWorldBuoyancy.MODID)
public class IWBHandler
{
    private static final Set<Item> itemSet = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final TIntSet oreSet = new TIntHashSet();

    public static boolean isFloating(ItemStack stack)
    {
        Item item = stack.getItem();
        if (item instanceof ItemBlock)
        {
            if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("Checking " + stack.getItem().getRegistryName() + " for ItemBlock with Material.WOOD...");
            Block block = ((ItemBlock) item).getBlock();
            return (block.getDefaultState().getMaterial() == Material.WOOD);
        }
        else
        {
            if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("Checking " + stack.getItem().getRegistryName() + " for ore dictionary entries containing 'wood'...");
            for (int i : OreDictionary.getOreIDs(stack))
            {
                String oreName = OreDictionary.getOreName(i);
                if (oreName.toLowerCase().contains("wood")) return true;
            }
        }
        if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("Checking for custom config entries...");
        if (IWBConfig.customBuoyancyList.length > 0)
        {
            if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug(IWBConfig.customBuoyancyList.length + " custom config entries found!");
            for (String s : IWBConfig.customBuoyancyList)
            {
                if (s.indexOf(':') >= 0)
                {
                    ResourceLocation loc = new ResourceLocation(s);
                    if (ForgeRegistries.ITEMS.containsKey(loc)) itemSet.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(s)));
                }
                else oreSet.add(OreDictionary.getOreID(s));
            }
            if (itemSet.contains(stack.getItem())) return true;
            else
            {
                for (int i : OreDictionary.getOreIDs(stack)) if (oreSet.contains(i)) return true;
                return false;
            }
        }
        if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("Checking for Better With Mods buoyancy...");
        if (Loader.isModLoaded("betterwithmods")) return HCBuoy.getBuoyancy(stack) > 0.5F;
        return false;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        EntityPlayer player = event.player;

        if (!IWBConfig.inWorldBuoyancy || player.isCreative())
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
                            if (IWBConfig.omitItemInHand && player.inventory.getCurrentItem() == itemStack)
                            {
                                return;
                            }
                            if (isFloating(itemStack) || IWBConfig.floatBothWays && !isFloating(itemStack))
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWorldLoad(WorldEvent.Load event)
    {
        if (IWBConfig.cannotDisplaceLiquid)
        {
            event.getWorld().addEventListener(IWBFluidReplacer.INSTANCE);
        }
    }
}