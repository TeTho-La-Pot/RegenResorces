/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.SimplePreparableReloadListener
 *  net.minecraft.util.profiling.ProfilerFiller
 *  org.slf4j.Logger
 */
package com.github.TeThoLaPot.regen_resources.platform.forge.config;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public final class RegenPresetsReloadListener
extends SimplePreparableReloadListener<List<RegenRule>> {
    private static final Logger LOGGER = LogUtils.getLogger();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected List<RegenRule> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.m_6180_("regen_resources:preset_io");
        try {
            List<RegenRule> list = RegenPresetIo.loadOrCreateDefaults();
            return list;
        }
        finally {
            profiler.m_7238_();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void apply(List<RegenRule> rules, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.m_6180_("regen_resources:preset_apply");
        try {
            RegenRuleRegistry.setRules(rules);
            LOGGER.info("RegenResources: {} preset rule(s) reloaded via /reload", (Object)rules.size());
        }
        finally {
            profiler.m_7238_();
        }
    }
}

