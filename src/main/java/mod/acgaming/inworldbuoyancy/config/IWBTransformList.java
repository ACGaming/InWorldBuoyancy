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

import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.acgaming.inworldbuoyancy.InWorldBuoyancy;

public class IWBTransformList
{
    private static final Map<Item, Item> transformMap = new Object2ObjectOpenHashMap<>();

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

                ResourceLocation locInputItem = new ResourceLocation(id.substring(0, arrowIndex));
                if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug(locInputItem);

                ResourceLocation locOutputItem = new ResourceLocation(id.substring(arrowIndex + 3));
                if (IWBConfig.debug) InWorldBuoyancy.LOGGER.debug(locOutputItem);

                if (ForgeRegistries.ITEMS.containsKey(locInputItem) && ForgeRegistries.ITEMS.containsKey(locOutputItem)) transformMap.put(ForgeRegistries.ITEMS.getValue(locInputItem), ForgeRegistries.ITEMS.getValue(locOutputItem));
            }
        }
    }

    public static boolean hasTransformItem(ItemStack itemStack)
    {
        return transformMap.get(itemStack.getItem()) != null;
    }

    public static ItemStack getTransformItem(ItemStack itemStack)
    {
        return new ItemStack(transformMap.get(itemStack.getItem()));
    }
}