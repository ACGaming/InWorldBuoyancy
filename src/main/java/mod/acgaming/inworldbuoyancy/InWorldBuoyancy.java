/*
 * Copyright (C) 2017, 2018, 2022 Adrian Siekierka, ACGaming
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package mod.acgaming.inworldbuoyancy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import mod.acgaming.inworldbuoyancy.config.IWBDropList;
import mod.acgaming.inworldbuoyancy.config.IWBTransformList;

@Mod(modid = InWorldBuoyancy.MODID, name = InWorldBuoyancy.NAME, version = "${version}", acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "after:hardcorebuoy")
public class InWorldBuoyancy
{
    public static final String MODID = "inworldbuoyancy";
    public static final String NAME = "In-World Buoyancy";
    public static final Logger LOGGER = LogManager.getLogger("IWB");

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        LOGGER.info("IWB Initialized");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        IWBDropList.list();
        LOGGER.info("Drop List Initialized");

        IWBTransformList.list();
        LOGGER.info("Transform List Initialized");
    }
}