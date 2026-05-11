package com.github.TeThoLaPot.regen_resources.platform.neoforge.config;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenRule;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenRuleRegistry;
import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

/** {@code /reload} 時に {@link RegenPresetIo} からルールを再読み込みする。 */
public final class RegenPresetsReloadListener extends SimplePreparableReloadListener<List<RegenRule>> {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    protected List<RegenRule> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("regen_resources:preset_io");
        try {
            return RegenPresetIo.loadOrCreateDefaults();
        } finally {
            profiler.pop();
        }
    }

    @Override
    protected void apply(List<RegenRule> rules, ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.push("regen_resources:preset_apply");
        try {
            RegenRuleRegistry.setRules(rules);
            LOGGER.info("RegenResources: {} preset rule(s) reloaded via /reload", rules.size());
        } finally {
            profiler.pop();
        }
    }
}
