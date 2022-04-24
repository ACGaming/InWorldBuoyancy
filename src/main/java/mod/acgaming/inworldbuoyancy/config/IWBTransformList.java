/*
 * Copyright (C) 2017, 2018, 2022 Adrian Siekierka, ACGaming
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package mod.acgaming.inworldbuoyancy.config;

import java.lang.reflect.Array;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.acgaming.inworldbuoyancy.InWorldBuoyancy;

public class IWBTransformList
{
    private static final Map<Item, Item> transformMap = new Object2ObjectOpenHashMap<>();
    private static final Map<Integer, Item> oreDictMap = new Object2ObjectOpenHashMap<>();
    private static int oreID;

    public static void list()
    {
        if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("Checking for custom transform config entries...");
        if (IWBConfig.customTransformList.length > 0)
        {
            if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug(IWBConfig.customTransformList.length + " custom config entries found!");
            for (final String id : IWBConfig.customTransformList)
            {
                int arrowIndex = id.indexOf("-->"), colonIndex = id.indexOf(':');

                if (colonIndex == -1 || arrowIndex == -1) throw new IllegalArgumentException(id + " is not valid! Check config comment for formatting tips!");

                // ITEM OUTPUT
                ResourceLocation locOutputItem = new ResourceLocation(id.substring(arrowIndex + 3));
                if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug(locOutputItem);

                // ORE DICTIONARY ITEM INPUT
                if (OreDictionary.doesOreNameExist(id.substring(0, arrowIndex)) && ForgeRegistries.ITEMS.containsKey(locOutputItem))
                {
                    if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("getOreID: " + OreDictionary.getOreID(id.substring(0, arrowIndex)));
                    oreDictMap.put(OreDictionary.getOreID(id.substring(0, arrowIndex)), ForgeRegistries.ITEMS.getValue(locOutputItem));
                }
                // REGULAR ITEM INPUT
                else if (id.indexOf(':') >= 0)
                {
                    ResourceLocation locInputItem = new ResourceLocation(id.substring(0, arrowIndex));
                    if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug(locInputItem);
                    if (ForgeRegistries.ITEMS.containsKey(locInputItem) && ForgeRegistries.ITEMS.containsKey(locOutputItem))
                    {
                        transformMap.put(ForgeRegistries.ITEMS.getValue(locInputItem), ForgeRegistries.ITEMS.getValue(locOutputItem));
                    }
                }
            }
        }
    }

    public static boolean hasRegularItem(ItemStack itemStack)
    {
        // CHECK TRANSFORM MAP FOR REGULAR ITEM
        return transformMap.get(itemStack.getItem()) != null;
    }

    public static boolean hasOreDictItem(ItemStack itemStack)
    {
        // CHECK ORE DICT MAP FOR ORE ID
        for (int i = 0; i < OreDictionary.getOreIDs(itemStack).length; i++)
        {
            oreID = (int) Array.get(OreDictionary.getOreIDs(itemStack), i);
            if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("oreID: " + oreID);
            if (oreDictMap.get(oreID) != null) return true;
        }
        return false;
    }

    public static ItemStack getTransformItem(ItemStack itemStack)
    {
        if (hasRegularItem(itemStack))
        {
            if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("transformMap: " + transformMap.get(itemStack.getItem()));
            // RETURN TRANSFORM STACK
            return new ItemStack(transformMap.get(itemStack.getItem()));
        }
        else if (hasOreDictItem(itemStack))
        {
            if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug("oreDictMap: " + oreDictMap.get(oreID));
            // RETURN TRANSFORM STACK
            return new ItemStack(oreDictMap.get(oreID));
        }
        return null;
    }
}