package com.github.TeThoLaPot.regen_resources.platform.forge.client.screen;

import com.github.TeThoLaPot.regen_resources.common.block.RegenTemplate;
import com.github.TeThoLaPot.regen_resources.common.block.RegenVisual;
import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.screen.RegenPresetDraftEditor.RootMode;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.widget.CycleSelectWidget;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.widget.FilledCheckbox;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.widget.TargetCycleHoverPreview;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.widget.TargetSelectButton;
import com.github.TeThoLaPot.regen_resources.platform.forge.config.RegenPresetIo;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenResourcesNetwork;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.RegenSettingsSnapshot;
import com.github.TeThoLaPot.regen_resources.platform.forge.network.ServerboundSaveRegenSettingsPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * ワールド再生設定 UI。
 * <ul>
 *   <li>左: 設定一覧 / 新規・名前編集・コピー・削除</li>
 *   <li>右: プリセット → tick/entries 分岐 → entries（+/×）→ ターゲット</li>
 *   <li>右下: 保存して閉じる</li>
 * </ul>
 */
public final class RegenSettingsScreen extends Screen {

    private static final int PANEL_W = 360;
    private static final int PANEL_H = 260;
    private static final int LIST_W = 120;
    private static final int LIST_H = 150;
    private static final int ROW_H = 18;
    private static final int BAND_FILL = 0xFF6B6B6B;
    private static final int BAND_BORDER = 0xFFFFFFFF;
    private static final int LIST_INNER_PAD = 2;
    private static final int LIST_SCROLLBAR_W = 6;
    private static final int RIGHT_TEXT_PAD = 10;

    private static final String DIM_NONE = "__none__";

    private final LinkedHashMap<String, String> drafts = new LinkedHashMap<>();
    private String selectedName;
    private PresetList presetList;
    private CycleSelectWidget<RegenVisual> presetCycle;
    private CycleSelectWidget<RootMode> modeCycle;
    private EditBox rootTickBox;
    private int leftPos;
    private int topPos;
    private int rightScroll;
    private int rightEditorHeight;
    private final List<TickLabelDraw> tickLabels = new ArrayList<>();
    private final List<EntryFrameDraw> entryFrames = new ArrayList<>();

    private record TickLabelDraw(int x, int y, int w, Component text, @Nullable Component tooltip) {}

    private record EntryFrameDraw(int x, int y, int w, int h) {}

    public RegenSettingsScreen(RegenSettingsSnapshot snapshot) {
        super(Component.translatable("screen.regen_resources.settings.title"));
        drafts.putAll(snapshot.asMap());
        if (!drafts.isEmpty()) {
            selectedName = drafts.keySet().iterator().next();
        }
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - PANEL_W) / 2;
        this.topPos = (this.height - PANEL_H) / 2;

        this.presetList = new PresetList(this.leftPos + 10, this.topPos + 20, LIST_W, LIST_H);
        this.addRenderableWidget(this.presetList);
        rebuildList();

        int btnY1 = this.topPos + 176;
        int btnY2 = this.topPos + 198;
        int btnW = 58;
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.regen_resources.settings.btn.create"), b -> openCreate())
                        .bounds(this.leftPos + 10, btnY1, btnW, 18)
                        .build());
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.regen_resources.settings.btn.rename"), b -> openRename())
                        .bounds(this.leftPos + 10 + btnW + 4, btnY1, btnW, 18)
                        .build());
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.regen_resources.settings.btn.copy"), b -> copySelected())
                        .bounds(this.leftPos + 10, btnY2, btnW, 18)
                        .build());
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.regen_resources.settings.btn.delete"), b -> deleteSelected())
                        .bounds(this.leftPos + 10 + btnW + 4, btnY2, btnW, 18)
                        .build());

        this.presetCycle = null;
        this.modeCycle = null;
        this.rootTickBox = null;
        this.tickLabels.clear();
        this.entryFrames.clear();
        this.rightEditorHeight = 0;

        if (selectedName != null) {
            buildRightEditor();
        } else {
            this.rightScroll = 0;
        }

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), b -> this.onClose())
                        .bounds(this.leftPos + 10, this.topPos + PANEL_H - 26, 72, 18)
                        .build());
        int saveW = 100;
        int folderW = 90;
        int footerGap = 4;
        int saveX = this.leftPos + PANEL_W - 10 - saveW;
        int folderX = saveX - footerGap - folderW;
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable("screen.regen_resources.settings.btn.open_folder"),
                                b -> openPresetsFolder())
                        .bounds(folderX, this.topPos + PANEL_H - 26, folderW, 18)
                        .build());
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.regen_resources.settings.btn.save_close"), b -> saveAndClose())
                        .bounds(saveX, this.topPos + PANEL_H - 26, saveW, 18)
                        .build());
    }

    private void buildRightEditor() {
        int rightX = rightContentLeft();
        int rightW = rightContentWidth();
        int y = this.topPos + 36 - rightScroll;
        tickLabels.clear();
        entryFrames.clear();

        List<RegenVisual> visuals = Arrays.stream(RegenVisual.values())
                .filter(v -> !v.getSerializedName().endsWith("_preset"))
                .toList();
        RegenVisual current = toSelectableVisual(readPresetVisual(selectedName), visuals);
        addFieldLabel(
                rightX,
                y + 5,
                Component.translatable("screen.regen_resources.settings.preset_label"),
                Component.translatable("screen.regen_resources.settings.help.preset"));
        int presetLabelW = this.font.width(Component.translatable("screen.regen_resources.settings.preset_label")) + 4;
        this.presetCycle = new CycleSelectWidget<>(
                rightX + presetLabelW,
                y,
                Math.max(40, rightW - presetLabelW),
                18,
                visuals,
                current,
                v -> Component.literal(v.getSerializedName()),
                this::onPresetChanged);
        this.addRenderableWidget(this.presetCycle);
        y += 22;

        JsonObject root = currentRoot();
        RootMode mode = RegenPresetDraftEditor.detectMode(root);
        this.modeCycle = new CycleSelectWidget<>(
                rightX,
                y,
                rightW,
                18,
                List.of(RootMode.GLOBAL_TICK, RootMode.ENTRIES),
                mode,
                m -> Component.translatable(
                        m == RootMode.GLOBAL_TICK
                                ? "screen.regen_resources.settings.mode.global_tick"
                                : "screen.regen_resources.settings.mode.entries"),
                this::onModeChanged);
        this.addRenderableWidget(this.modeCycle);
        y += 22;

        if (mode == RootMode.GLOBAL_TICK) {
            Component globalLabel = Component.translatable("screen.regen_resources.settings.label.global_tick");
            int labelW = this.font.width(globalLabel) + 4;
            addFieldLabel(
                    rightX,
                    y + 5,
                    globalLabel,
                    Component.translatable("screen.regen_resources.settings.help.global_tick"));
            this.rootTickBox = new EditBox(
                    this.font, rightX + labelW, y, Math.max(40, rightW - labelW), 18, globalLabel);
            this.rootTickBox.setValue(Long.toString(RegenPresetDraftEditor.readRootTick(root)));
            this.rootTickBox.setFilter(s -> s.isEmpty() || s.matches("\\d{1,9}"));
            this.rootTickBox.setResponder(s -> {
                if (s == null || s.isBlank()) {
                    return;
                }
                try {
                    long v = Long.parseLong(s);
                    JsonObject r = currentRoot();
                    RegenPresetDraftEditor.applyRootMode(r, RootMode.GLOBAL_TICK, v);
                    putRoot(r);
                } catch (NumberFormatException ignored) {
                }
            });
            this.addRenderableWidget(this.rootTickBox);
            y += 22;
        }

        if (mode == RootMode.ENTRIES) {
            boolean missingTick = false;
            for (var el : RegenPresetDraftEditor.entries(root)) {
                if (el.isJsonObject() && !RegenPresetDraftEditor.hasOwnEntryTick(el.getAsJsonObject())) {
                    missingTick = true;
                    break;
                }
            }
            if (missingTick) {
                RegenPresetDraftEditor.ensureEntriesHaveTick(root, 1200L);
                putRoot(root);
            }
        }
        {
            boolean missingTargets = false;
            for (var el : RegenPresetDraftEditor.entries(root)) {
                if (el.isJsonObject() && !RegenPresetDraftEditor.hasTargetsKey(el.getAsJsonObject())) {
                    missingTargets = true;
                    break;
                }
            }
            if (missingTargets) {
                RegenPresetDraftEditor.ensureEntriesHaveTargets(root);
                putRoot(root);
            }
        }
        JsonArray entries = RegenPresetDraftEditor.entries(root);
        boolean customPreset = isCustomVisual(current);
        Component entryLabel = Component.translatable("screen.regen_resources.settings.label.entry_tick");
        Component targetLabel = Component.translatable("screen.regen_resources.settings.label.target");
        Component naturalLabel = Component.translatable("screen.regen_resources.settings.label.natural_regen");
        Component miningLabel = Component.translatable("screen.regen_resources.settings.label.mining_sample");
        Component templateLabel = Component.translatable("screen.regen_resources.settings.label.template");
        Component exclusionLabel = Component.translatable("screen.regen_resources.settings.label.dimension_exclusion");
        int entryLabelW = this.font.width(entryLabel) + 4;
        int targetLabelW = this.font.width(targetLabel) + 4;
        List<String> allDims = availableDimensionIds();

        for (int i = 0; i < entries.size(); i++) {
            final int index = i;
            JsonObject entry = entries.get(i).getAsJsonObject();
            int frameTop = y;
            int framePad = 3;
            y += framePad;
            int deleteY = y;
            int fieldX = rightX + 22;
            int fieldW = Math.max(40, rightX + rightW - fieldX - framePad);
            boolean forceEntryTick = mode == RootMode.ENTRIES;
            boolean showEntryTick = forceEntryTick || RegenPresetDraftEditor.hasOwnEntryTick(entry);
            boolean showDimensions = RegenPresetDraftEditor.hasDimensionsKey(entry);
            boolean showNatural = RegenPresetDraftEditor.hasNaturalRegenKey(entry);
            boolean showMining = RegenPresetDraftEditor.hasMiningSampleKey(entry);
            boolean showTemplate = RegenPresetDraftEditor.hasTemplateKey(entry) && customPreset;

            RegenPresetDraftEditor.ensureTargetsKey(entry);
            if (RegenPresetDraftEditor.hasTemplateKey(entry) && !customPreset) {
                RegenPresetDraftEditor.clearTemplateKey(entry);
                putRoot(root);
                showTemplate = false;
            }

            int count = RegenPresetDraftEditor.targetCount(entry);
            addFieldLabel(
                    fieldX,
                    y + 5,
                    targetLabel,
                    Component.translatable("screen.regen_resources.settings.help.target"));
            TargetSelectButton targetBtn = new TargetSelectButton(
                    fieldX + targetLabelW,
                    y,
                    Math.max(40, fieldW - targetLabelW),
                    18,
                    Component.translatable("screen.regen_resources.settings.btn.target_select", count),
                    b -> openTargets(index));
            targetBtn.setTargets(RegenPresetDraftEditor.readTargets(entry));
            this.addRenderableWidget(targetBtn);
            y += 22;

            if (showEntryTick) {
                addFieldLabel(
                        fieldX,
                        y + 5,
                        entryLabel,
                        Component.translatable("screen.regen_resources.settings.help.entry_tick"));
                int clearW = forceEntryTick ? 0 : 18;
                int tickBoxX = fieldX + entryLabelW;
                int tickBoxW = Math.max(40, fieldW - entryLabelW - (clearW > 0 ? clearW + 2 : 0));
                EditBox tickBox = new EditBox(this.font, tickBoxX, y, tickBoxW, 18, entryLabel);
                long fallback = mode == RootMode.GLOBAL_TICK ? RegenPresetDraftEditor.readRootTick(root) : 1200L;
                tickBox.setValue(Long.toString(RegenPresetDraftEditor.readEntryTick(entry, fallback)));
                tickBox.setFilter(s -> s.isEmpty() || s.matches("\\d{1,9}"));
                tickBox.setResponder(s -> {
                    if (s == null || s.isBlank()) {
                        return;
                    }
                    try {
                        long v = Long.parseLong(s);
                        JsonObject r = currentRoot();
                        JsonObject e = RegenPresetDraftEditor.entryAt(r, index);
                        RegenPresetDraftEditor.setEntryTick(e, v);
                        putRoot(r);
                    } catch (NumberFormatException ignored) {
                    }
                });
                this.addRenderableWidget(tickBox);
                if (!forceEntryTick) {
                    this.addRenderableWidget(
                            Button.builder(Component.literal("×"), b -> {
                                        JsonObject r = currentRoot();
                                        JsonObject e = RegenPresetDraftEditor.entryAt(r, index);
                                        RegenPresetDraftEditor.clearEntryTick(e);
                                        putRoot(r);
                                        rebuildEditor();
                                    })
                                    .bounds(tickBoxX + tickBoxW + 2, y, 18, 18)
                                    .build());
                }
                y += 22;
            }

            if (showDimensions) {
                y = addDimensionsBlock(fieldX, fieldW, y, index, entry, allDims, exclusionLabel);
            }

            if (showNatural) {
                y = addCheckboxRow(
                        fieldX,
                        fieldW,
                        y,
                        naturalLabel,
                        Component.translatable("screen.regen_resources.settings.help.natural_regen"),
                        RegenPresetDraftEditor.readNaturalRegen(entry),
                        selected -> {
                            JsonObject r = currentRoot();
                            JsonObject e = RegenPresetDraftEditor.entryAt(r, index);
                            RegenPresetDraftEditor.setNaturalRegen(e, selected);
                            putRoot(r);
                        },
                        () -> {
                            JsonObject r = currentRoot();
                            JsonObject e = RegenPresetDraftEditor.entryAt(r, index);
                            RegenPresetDraftEditor.clearNaturalRegenKey(e);
                            putRoot(r);
                            rebuildEditor();
                        });
            }

            if (showMining) {
                y = addMiningSampleRow(fieldX, fieldW, y, index, entry, miningLabel);
            }

            if (showTemplate) {
                addFieldLabel(
                        fieldX,
                        y + 5,
                        templateLabel,
                        Component.translatable("screen.regen_resources.settings.help.template"));
                int labelW = this.font.width(templateLabel) + 4;
                int btnW2 = Math.max(40, fieldW - labelW - 20);
                this.addRenderableWidget(
                        Button.builder(
                                        Component.translatable("screen.regen_resources.settings.btn.template_edit"),
                                        b -> openCustomVisual(index))
                                .bounds(fieldX + labelW, y, btnW2, 18)
                                .build());
                this.addRenderableWidget(
                        Button.builder(Component.literal("×"), b -> {
                                    JsonObject r = currentRoot();
                                    JsonObject e = RegenPresetDraftEditor.entryAt(r, index);
                                    RegenPresetDraftEditor.clearTemplateKey(e);
                                    putRoot(r);
                                    rebuildEditor();
                                })
                                .bounds(fieldX + labelW + btnW2 + 2, y, 18, 18)
                                .build());
                y += 22;
            }

            List<EntryFieldOption> remaining = remainingEntryOptions(
                    forceEntryTick,
                    showEntryTick,
                    showDimensions,
                    showNatural,
                    showMining,
                    showTemplate,
                    customPreset);
            if (!remaining.isEmpty()) {
                List<EntryFieldOption> opts = new ArrayList<>();
                opts.add(EntryFieldOption.NONE);
                opts.addAll(remaining);
                this.addRenderableWidget(
                        new CycleSelectWidget<>(
                                fieldX,
                                y,
                                fieldW,
                                18,
                                opts,
                                EntryFieldOption.NONE,
                                o -> Component.translatable(o.langKey()),
                                picked -> {
                                    if (picked == null || picked == EntryFieldOption.NONE) {
                                        return;
                                    }
                                    JsonObject r = currentRoot();
                                    JsonObject e = RegenPresetDraftEditor.entryAt(r, index);
                                    applyEntryFieldOption(e, picked, r, mode);
                                    putRoot(r);
                                    rebuildEditor();
                                }));
                y += 22;
            }

            this.addRenderableWidget(
                    Button.builder(Component.literal("×"), b -> {
                                JsonObject r = currentRoot();
                                RegenPresetDraftEditor.removeEntry(r, index);
                                putRoot(r);
                                rebuildEditor();
                            })
                            .bounds(rightX + framePad, deleteY, 18, 18)
                            .build());

            y += framePad;
            entryFrames.add(new EntryFrameDraw(rightX, frameTop, rightW, Math.max(22, y - frameTop)));
            y += 4;
        }

        this.addRenderableWidget(
                Button.builder(Component.literal("+"), b -> {
                            JsonObject r = currentRoot();
                            JsonArray arr = RegenPresetDraftEditor.entries(r);
                            long tick =
                                    mode == RootMode.GLOBAL_TICK
                                            ? RegenPresetDraftEditor.readRootTick(r)
                                            : 1200L;
                            arr.add(RegenPresetDraftEditor.newEntry(mode, tick));
                            putRoot(r);
                            rebuildEditor();
                        })
                        .bounds(rightX, y, 18, 18)
                        .build());
        int contentStart = this.topPos + 36 - rightScroll;
        this.rightEditorHeight = y + 28 - contentStart;
        int max = maxRightScroll();
        if (rightScroll > max) {
            int delta = rightScroll - max;
            rightScroll = max;
            shiftRightEditor(delta);
        }
    }

    private void shiftRightEditor(int delta) {
        if (delta == 0) {
            return;
        }
        for (var renderable : this.renderables) {
            if (isRightEditorWidget(renderable) && renderable instanceof AbstractWidget w) {
                w.setY(w.getY() + delta);
            }
        }
        List<TickLabelDraw> shiftedLabels = new ArrayList<>();
        for (TickLabelDraw label : tickLabels) {
            shiftedLabels.add(new TickLabelDraw(label.x(), label.y() + delta, label.w(), label.text(), label.tooltip()));
        }
        tickLabels.clear();
        tickLabels.addAll(shiftedLabels);
        List<EntryFrameDraw> shiftedFrames = new ArrayList<>();
        for (EntryFrameDraw f : entryFrames) {
            shiftedFrames.add(new EntryFrameDraw(f.x(), f.y() + delta, f.w(), f.h()));
        }
        entryFrames.clear();
        entryFrames.addAll(shiftedFrames);
    }

    private void addFieldLabel(int x, int y, Component text, @Nullable Component tooltip) {
        tickLabels.add(new TickLabelDraw(x, y, this.font.width(text), text, tooltip));
    }

    private int addCheckboxRow(
            int fieldX,
            int fieldW,
            int y,
            Component label,
            Component help,
            boolean selected,
            java.util.function.Consumer<Boolean> onToggle,
            Runnable onClear) {
        addFieldLabel(fieldX, y + 5, label, help);
        int labelW = this.font.width(label) + 4;
        int boxY = y + (18 - FilledCheckbox.BOX) / 2;
        this.addRenderableWidget(new FilledCheckbox(fieldX + labelW, boxY, selected, onToggle));
        this.addRenderableWidget(
                Button.builder(Component.literal("×"), b -> onClear.run())
                        .bounds(fieldX + fieldW - 18, y, 18, 18)
                        .build());
        return y + 22;
    }

    private static RegenVisual toSelectableVisual(RegenVisual current, List<RegenVisual> allowed) {
        if (allowed.contains(current)) {
            return current;
        }
        String name = current.getSerializedName();
        if (name.endsWith("_preset")) {
            RegenVisual base = RegenVisual.tryParseToken(name.substring(0, name.length() - "_preset".length()));
            if (base != null && allowed.contains(base)) {
                return base;
            }
        }
        return allowed.isEmpty() ? current : allowed.get(0);
    }

    private int addDimensionsBlock(
            int fieldX,
            int fieldW,
            int y,
            int entryIndex,
            JsonObject entry,
            List<String> allDims,
            Component exclusionLabel) {
        addFieldLabel(
                fieldX,
                y + 5,
                Component.translatable("screen.regen_resources.settings.label.dimensions"),
                Component.translatable("screen.regen_resources.settings.help.dimensions"));
        y += 14;

        List<String> selected = new ArrayList<>(RegenPresetDraftEditor.readDimensionsList(entry));
        selected.removeIf(s -> s == null || s.isBlank());
        {
            List<String> raw = RegenPresetDraftEditor.readDimensionsList(entry);
            if (selected.size() != raw.size()) {
                JsonObject r = currentRoot();
                JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
                RegenPresetDraftEditor.writeDimensionsList(e, selected);
                putRoot(r);
            }
        }

        for (int di = 0; di < selected.size(); di++) {
            final int dimIndex = di;
            String current = selected.get(di);
            List<String> opts = dimensionOptions(allDims, selected, current);
            this.addRenderableWidget(
                    new CycleSelectWidget<>(
                            fieldX,
                            y,
                            Math.max(40, fieldW - 20),
                            18,
                            opts,
                            current,
                            this::labelDimensionOption,
                            picked -> {
                                JsonObject r = currentRoot();
                                JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
                                List<String> dims = new ArrayList<>(RegenPresetDraftEditor.readDimensionsList(e));
                                if (dimIndex < 0 || dimIndex >= dims.size()) {
                                    return;
                                }
                                if (DIM_NONE.equals(picked)) {
                                    dims.remove(dimIndex);
                                } else {
                                    dims.set(dimIndex, picked);
                                }
                                RegenPresetDraftEditor.writeDimensionsList(e, dims);
                                putRoot(r);
                                rebuildEditor();
                            }));
            y += 22;
        }

        Set<String> used = new LinkedHashSet<>(selected);
        boolean canAddMore = allDims.stream().anyMatch(d -> !used.contains(d));
        if (canAddMore) {
            List<String> opts = dimensionOptions(allDims, selected, null);
            this.addRenderableWidget(
                    new CycleSelectWidget<>(
                            fieldX,
                            y,
                            Math.max(40, fieldW - 20),
                            18,
                            opts,
                            DIM_NONE,
                            this::labelDimensionOption,
                            picked -> {
                                if (picked == null || DIM_NONE.equals(picked)) {
                                    return;
                                }
                                JsonObject r = currentRoot();
                                JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
                                List<String> dims = new ArrayList<>(RegenPresetDraftEditor.readDimensionsList(e));
                                if (!dims.contains(picked)) {
                                    dims.add(picked);
                                    RegenPresetDraftEditor.writeDimensionsList(e, dims);
                                    putRoot(r);
                                    rebuildEditor();
                                }
                            }));
            y += 22;
        }

        y = addCheckboxRow(
                fieldX,
                fieldW,
                y,
                exclusionLabel,
                Component.translatable("screen.regen_resources.settings.help.dimension_exclusion"),
                RegenPresetDraftEditor.readDimensionExclusion(entry),
                selectedFlag -> {
                    JsonObject r = currentRoot();
                    JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
                    RegenPresetDraftEditor.ensureDimensionsKey(e);
                    RegenPresetDraftEditor.setDimensionExclusion(e, selectedFlag);
                    putRoot(r);
                },
                () -> {
                    JsonObject r = currentRoot();
                    JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
                    RegenPresetDraftEditor.clearDimensionsKey(e);
                    putRoot(r);
                    rebuildEditor();
                });
        return y;
    }

    private Component labelDimensionOption(String id) {
        if (DIM_NONE.equals(id)) {
            return Component.translatable("screen.regen_resources.settings.option.none");
        }
        return Component.literal(id);
    }

    private static List<String> dimensionOptions(List<String> all, List<String> selected, @Nullable String current) {
        List<String> opts = new ArrayList<>();
        opts.add(DIM_NONE);
        for (String d : all) {
            if (d.equals(current) || !selected.contains(d)) {
                opts.add(d);
            }
        }
        if (current != null && !current.isBlank() && !opts.contains(current)) {
            opts.add(current);
        }
        return opts;
    }

    private List<String> availableDimensionIds() {
        List<String> out = new ArrayList<>();
        if (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.connection != null) {
            for (ResourceKey<Level> key : this.minecraft.player.connection.levels()) {
                out.add(key.location().toString());
            }
        }
        if (out.isEmpty()) {
            out.add("minecraft:overworld");
            out.add("minecraft:the_nether");
            out.add("minecraft:the_end");
        }
        out.sort(String::compareTo);
        return out;
    }

    private int addMiningSampleRow(int fieldX, int fieldW, int y, int entryIndex, JsonObject entry, Component miningLabel) {
        addFieldLabel(
                fieldX,
                y + 5,
                miningLabel,
                Component.translatable("screen.regen_resources.settings.help.mining_sample"));
        int labelW = this.font.width(miningLabel) + 4;
        String current = RegenPresetDraftEditor.readMiningSample(entry);
        Component btnText = current == null || current.isBlank()
                ? Component.translatable("screen.regen_resources.settings.btn.mining_select")
                : Component.literal(current);
        int btnW = Math.max(40, fieldW - labelW - 20);
        this.addRenderableWidget(
                Button.builder(btnText, b -> openMiningSamplePicker(entryIndex))
                        .bounds(fieldX + labelW, y, btnW, 18)
                        .build());
        this.addRenderableWidget(
                Button.builder(Component.literal("×"), b -> {
                            JsonObject r = currentRoot();
                            JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
                            RegenPresetDraftEditor.clearMiningSampleKey(e);
                            putRoot(r);
                            rebuildEditor();
                        })
                        .bounds(fieldX + labelW + btnW + 2, y, 18, 18)
                        .build());
        return y + 22;
    }

    private void openMiningSamplePicker(int entryIndex) {
        this.minecraft.setScreen(RegenTargetPickScreen.forBlockId(this, blockId -> {
            JsonObject r = currentRoot();
            JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
            RegenPresetDraftEditor.ensureMiningSampleKey(e);
            RegenPresetDraftEditor.setMiningSample(e, blockId.toString());
            putRoot(r);
            this.minecraft.setScreen(this);
            rebuildEditor();
        }));
    }

    private static boolean isCustomVisual(RegenVisual visual) {
        return visual == RegenVisual.CUSTOM || visual == RegenVisual.CUSTOM_PRESET;
    }

    private enum EntryFieldOption {
        NONE("screen.regen_resources.settings.option.none"),
        ENTRY_TICK("screen.regen_resources.settings.option.entry_tick"),
        DIMENSIONS("screen.regen_resources.settings.option.dimensions"),
        NATURAL_REGEN("screen.regen_resources.settings.option.natural_regen"),
        MINING_SAMPLE("screen.regen_resources.settings.option.mining_sample"),
        TEMPLATE("screen.regen_resources.settings.option.template");

        private final String langKey;

        EntryFieldOption(String langKey) {
            this.langKey = langKey;
        }

        String langKey() {
            return langKey;
        }
    }

    private static List<EntryFieldOption> remainingEntryOptions(
            boolean forceEntryTick,
            boolean showEntryTick,
            boolean showDimensions,
            boolean showNatural,
            boolean showMining,
            boolean showTemplate,
            boolean customPreset) {
        List<EntryFieldOption> out = new ArrayList<>();
        if (!forceEntryTick && !showEntryTick) {
            out.add(EntryFieldOption.ENTRY_TICK);
        }
        if (!showDimensions) {
            out.add(EntryFieldOption.DIMENSIONS);
        }
        if (!showNatural) {
            out.add(EntryFieldOption.NATURAL_REGEN);
        }
        if (!showMining) {
            out.add(EntryFieldOption.MINING_SAMPLE);
        }
        if (customPreset && !showTemplate) {
            out.add(EntryFieldOption.TEMPLATE);
        }
        return out;
    }

    private void applyEntryFieldOption(JsonObject entry, EntryFieldOption option, JsonObject root, RootMode mode) {
        switch (option) {
            case NONE -> {}
            case ENTRY_TICK -> {
                long fallback = mode == RootMode.GLOBAL_TICK
                        ? RegenPresetDraftEditor.readRootTick(root)
                        : 1200L;
                RegenPresetDraftEditor.setEntryTick(entry, fallback);
            }
            case DIMENSIONS -> RegenPresetDraftEditor.ensureDimensionsKey(entry);
            case NATURAL_REGEN -> RegenPresetDraftEditor.ensureNaturalRegenKey(entry);
            case MINING_SAMPLE -> RegenPresetDraftEditor.ensureMiningSampleKey(entry);
            case TEMPLATE -> RegenPresetDraftEditor.ensureTemplateKey(entry);
        }
    }

    private int rightWellTop() {
        return this.topPos + 19;
    }

    private int rightWellHeight() {
        return PANEL_H - 52;
    }

    private int maxRightScroll() {
        int contentAnchor = 36 - 19; // first editor row relative to well top
        int viewH = Math.max(1, rightWellHeight() - contentAnchor - 6);
        return Math.max(0, rightEditorHeight - viewH);
    }

    private boolean isRightEditorWidget(Object renderable) {
        if (renderable instanceof PresetList) {
            return false;
        }
        if (renderable instanceof AbstractWidget w) {
            if (isFooterBarWidget(w)) {
                return false;
            }
            return w.getX() >= this.leftPos + 10 + LIST_W;
        }
        return false;
    }

    /** パネル下端のキャンセル／フォルダ／保存のみ（スクロールで落ちた + などは含めない）。 */
    private boolean isFooterBarWidget(AbstractWidget w) {
        if (w.getY() < this.topPos + PANEL_H - 30) {
            return false;
        }
        // 左: キャンセル、右: フォルダを開く／保存して閉じる
        return w.getX() <= this.leftPos + 12 || w.getX() >= this.leftPos + PANEL_W - 210;
    }

    private void openTargets(int entryIndex) {
        JsonObject root = currentRoot();
        JsonObject entry = RegenPresetDraftEditor.entryAt(root, entryIndex);
        RegenPresetDraftEditor.ensureTargetsKey(entry);
        putRoot(root);
        List<RegenTargetSpec> initial = RegenPresetDraftEditor.readTargets(entry);
        RegenTargetEditScreen.open(this, initial, updated -> {
            JsonObject r = currentRoot();
            JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
            RegenPresetDraftEditor.writeTargets(e, updated);
            putRoot(r);
        });
    }

    private void openCustomVisual(int entryIndex) {
        JsonObject root = currentRoot();
        JsonObject entry = RegenPresetDraftEditor.entryAt(root, entryIndex);
        RegenPresetDraftEditor.ensureTemplateKey(entry);
        putRoot(root);

        RegenTemplate template = RegenTemplate.fromSerializedName(RegenPresetDraftEditor.readTemplate(entry));
        if (template == null) {
            template = RegenTemplate.CUBE_ALL;
        }
        Map<String, String> textures = new LinkedHashMap<>(RegenPresetDraftEditor.readTextures(entry));
        String mining = RegenPresetDraftEditor.readMiningSample(entry);
        RegenCustomVisualScreen.Draft initial = new RegenCustomVisualScreen.Draft(template, textures, mining);

        this.minecraft.setScreen(new RegenCustomVisualScreen(this, initial, draft -> {
            JsonObject r = currentRoot();
            JsonObject e = RegenPresetDraftEditor.entryAt(r, entryIndex);
            RegenPresetDraftEditor.setTemplate(e, draft.template().getSerializedName());
            RegenPresetDraftEditor.writeTextures(e, draft.textures());
            RegenPresetDraftEditor.ensureMiningSampleKey(e);
            RegenPresetDraftEditor.setMiningSample(e, draft.miningSample());
            putRoot(r);
            rebuildEditor();
        }));
    }

    private JsonObject currentRoot() {
        return RegenPresetDraftEditor.parse(drafts.getOrDefault(selectedName, "{}"));
    }

    private void putRoot(JsonObject root) {
        if (selectedName == null) {
            return;
        }
        drafts.put(selectedName, root.toString());
    }

    private void onModeChanged(RootMode mode) {
        if (selectedName == null || mode == null) {
            return;
        }
        JsonObject root = currentRoot();
        long tick = rootTickBox != null && !rootTickBox.getValue().isBlank()
                ? parseLongSafe(rootTickBox.getValue(), 1200L)
                : RegenPresetDraftEditor.readRootTick(root);
        RegenPresetDraftEditor.applyRootMode(root, mode, tick);
        putRoot(root);
        rebuildEditor();
    }

    private static long parseLongSafe(String s, long fallback) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private void rebuildList() {
        this.presetList.children().clear();
        for (String name : drafts.keySet()) {
            this.presetList.children().add(presetList.new Entry(name));
        }
        if (selectedName != null) {
            for (PresetList.Entry e : this.presetList.children()) {
                if (e.name.equals(selectedName)) {
                    this.presetList.setSelected(e);
                    break;
                }
            }
        }
    }

    private void select(String name) {
        selectedName = name;
        rightScroll = 0;
        rebuildEditor();
    }

    private void openCreate() {
        this.minecraft.setScreen(
                new RegenSettingsNameScreen(
                        this,
                        RegenSettingsNameScreen.Mode.CREATE,
                        "",
                        name -> {
                            if (drafts.containsKey(name)) {
                                return;
                            }
                            drafts.put(name, RegenPresetIo.defaultEmptyPresetJson("stone_preset"));
                            selectedName = name;
                            rebuildEditor();
                        }));
    }

    private void openRename() {
        if (selectedName == null) {
            return;
        }
        String from = selectedName;
        this.minecraft.setScreen(
                new RegenSettingsNameScreen(
                        this,
                        RegenSettingsNameScreen.Mode.RENAME,
                        from,
                        name -> {
                            if (name.equals(from) || drafts.containsKey(name)) {
                                return;
                            }
                            String json = drafts.remove(from);
                            drafts.put(name, json);
                            selectedName = name;
                            rebuildEditor();
                        }));
    }

    private void copySelected() {
        if (selectedName == null) {
            return;
        }
        String base = selectedName.toLowerCase(Locale.ROOT).replace(".json", "");
        String candidate = base + "_copy.json";
        int n = 2;
        while (drafts.containsKey(candidate)) {
            candidate = base + "_copy" + n + ".json";
            n++;
        }
        if (!RegenPresetIo.isValidPresetFileName(candidate)) {
            return;
        }
        drafts.put(candidate, drafts.get(selectedName));
        selectedName = candidate;
        rebuildEditor();
    }

    private void deleteSelected() {
        if (selectedName == null) {
            return;
        }
        drafts.remove(selectedName);
        selectedName = drafts.isEmpty() ? null : drafts.keySet().iterator().next();
        rightScroll = 0;
        rebuildEditor();
    }

    private void rebuildEditor() {
        this.rebuildWidgets();
    }

    private void onPresetChanged(RegenVisual visual) {
        if (selectedName == null || visual == null) {
            return;
        }
        JsonObject obj = currentRoot();
        obj.addProperty("preset", visual.getSerializedName());
        if (!isCustomVisual(visual)) {
            for (var el : RegenPresetDraftEditor.entries(obj)) {
                if (el.isJsonObject()) {
                    RegenPresetDraftEditor.clearTemplateKey(el.getAsJsonObject());
                }
            }
        }
        putRoot(obj);
        rebuildEditor();
    }

    private RegenVisual readPresetVisual(String name) {
        if (name == null) {
            return RegenVisual.STONE_PRESET;
        }
        try {
            JsonObject obj = RegenPresetDraftEditor.parse(drafts.get(name));
            if (obj.has("preset")) {
                RegenVisual v = RegenVisual.tryParseToken(obj.get("preset").getAsString());
                if (v != null) {
                    return v;
                }
            }
            if (obj.has("visual")) {
                RegenVisual v = RegenVisual.tryParseToken(obj.get("visual").getAsString());
                if (v != null) {
                    return v;
                }
            }
        } catch (RuntimeException ignored) {
        }
        return RegenVisual.STONE_PRESET;
    }

    private void saveAndClose() {
        List<RegenSettingsSnapshot.PresetFile> files = new ArrayList<>();
        for (Map.Entry<String, String> e : drafts.entrySet()) {
            files.add(new RegenSettingsSnapshot.PresetFile(e.getKey(), e.getValue()));
        }
        RegenResourcesNetwork.CHANNEL.sendToServer(new ServerboundSaveRegenSettingsPacket(files));
        this.onClose();
    }

    /** 単機／統合サーバー時のみ、ワールドの RegenPresets フォルダを OS で開く。 */
    private void openPresetsFolder() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        var integrated = this.minecraft.getSingleplayerServer();
        if (integrated == null) {
            this.minecraft.player.displayClientMessage(
                    Component.translatable("screen.regen_resources.settings.open_folder.remote"), true);
            return;
        }
        try {
            var dir = RegenPresetIo.rulesDirFor(integrated);
            java.nio.file.Files.createDirectories(dir);
            Util.getPlatform().openFile(dir.toFile());
        } catch (Exception ex) {
            this.minecraft.player.displayClientMessage(
                    Component.translatable("screen.regen_resources.settings.open_folder.failed"), true);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
        drawOpaquePanel(graphics);

        int titleX = this.width / 2 - this.font.width(this.title) / 2;
        graphics.drawString(this.font, this.title, titleX, this.topPos + 6, 0x404040, false);

        int rightX = rightContentLeft();
        int rightInnerY = this.topPos + 22 + 4;
        int rightTextMaxW = rightContentWidth();
        if (drafts.isEmpty()) {
            drawCenteredWrappedHint(
                    graphics,
                    Component.translatable("screen.regen_resources.settings.empty_list"),
                    rightX,
                    rightInnerY + 20,
                    rightTextMaxW);
        } else if (selectedName == null) {
            drawWrappedHint(
                    graphics,
                    Component.translatable("screen.regen_resources.settings.none_selected"),
                    rightX,
                    rightInnerY + 20,
                    rightTextMaxW);
        }
    }

    private int rightContentLeft() {
        return this.leftPos + 10 + LIST_W + 12 + RIGHT_TEXT_PAD;
    }

    private int rightContentWidth() {
        return this.leftPos + PANEL_W - 10 - RIGHT_TEXT_PAD - rightContentLeft();
    }

    private void drawWrappedHint(GuiGraphics graphics, Component text, int x, int y, int maxWidth) {
        for (var line : this.font.split(text, Math.max(40, maxWidth))) {
            graphics.drawString(this.font, line, x, y, 0x505050, false);
            y += this.font.lineHeight + 2;
        }
    }

    private void drawCenteredWrappedHint(GuiGraphics graphics, Component text, int x, int y, int maxWidth) {
        int w = Math.max(40, maxWidth);
        for (var line : this.font.split(text, w)) {
            int lineX = x + (w - this.font.width(line)) / 2;
            graphics.drawString(this.font, line, lineX, y, 0x505050, false);
            y += this.font.lineHeight + 2;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        for (var renderable : this.renderables) {
            if (renderable instanceof CycleSelectWidget<?> cycle
                    && cycle.isMouseOverSelectArea(mouseX, mouseY)) {
                return cycle.mouseScrolled(mouseX, mouseY, delta);
            }
        }
        int max = maxRightScroll();
        if (max <= 0) {
            return false;
        }
        int rx = this.leftPos + 10 + LIST_W + 11;
        int ry = rightWellTop();
        int rw = this.leftPos + PANEL_W - 10 - rx;
        int rh = rightWellHeight();
        if (mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + rh) {
            int next = Mth.clamp(rightScroll - (int) (delta * 12), 0, max);
            if (next != rightScroll) {
                rightScroll = next;
                rebuildEditor();
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int rx = this.leftPos + 10 + LIST_W + 11;
        int ry = rightWellTop();
        int rw = this.leftPos + PANEL_W - 10 - rx;
        int rh = rightWellHeight();
        boolean handled;
        if (mouseX >= rx && mouseX < rx + rw && mouseY >= ry && mouseY < ry + rh) {
            handled = super.mouseClicked(mouseX, mouseY, button);
        } else if (mouseY >= this.topPos + PANEL_H - 30) {
            handled = mouseClickedFooterBar(mouseX, mouseY, button);
        } else if (mouseX < this.leftPos + 10 + LIST_W + 8) {
            handled = super.mouseClicked(mouseX, mouseY, button);
        } else {
            handled = false;
        }
        if (button == 0 && !isOverAnyEditBox(mouseX, mouseY)) {
            clearEditBoxFocus();
        }
        return handled;
    }

    private boolean mouseClickedFooterBar(double mouseX, double mouseY, int button) {
        for (var renderable : this.renderables) {
            if (renderable instanceof AbstractWidget w
                    && isFooterBarWidget(w)
                    && w.isMouseOver(mouseX, mouseY)
                    && w.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverAnyEditBox(double mouseX, double mouseY) {
        for (var renderable : this.renderables) {
            if (renderable instanceof EditBox box && box.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private void clearEditBoxFocus() {
        for (var renderable : this.renderables) {
            if (renderable instanceof EditBox box && box.isFocused()) {
                box.setFocused(false);
            }
        }
        if (this.getFocused() instanceof EditBox) {
            this.setFocused(null);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.presetList != null) {
            this.presetList.clearHoverPreview();
        }
        this.renderBackground(graphics);

        for (var renderable : this.renderables) {
            if (!isRightEditorWidget(renderable)) {
                renderable.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        int rx = this.leftPos + 10 + LIST_W + 11;
        int ry = rightWellTop();
        int rw = this.leftPos + PANEL_W - 10 - rx;
        int rh = rightWellHeight();
        graphics.enableScissor(rx + 1, ry + 1, rx + rw - 1, ry + rh - 1);
        if (selectedName != null) {
            for (EntryFrameDraw frame : entryFrames) {
                drawEntryFrame(graphics, frame);
            }
            for (TickLabelDraw label : tickLabels) {
                graphics.drawString(this.font, label.text(), label.x(), label.y(), 0x404040, false);
            }
            for (var renderable : this.renderables) {
                if (isRightEditorWidget(renderable)) {
                    renderable.render(graphics, mouseX, mouseY, partialTick);
                }
            }
        }
        graphics.disableScissor();

        // ウェル下〜フッタ直前を覆い、下方向のはみ出しを隠す
        int coverTop = ry + rh;
        int coverBottom = this.topPos + PANEL_H - 28;
        if (coverBottom > coverTop) {
            graphics.fill(rx, coverTop, rx + rw, coverBottom, 0xFFC6C6C6);
        }
        // ウェル下縁を描き直す
        graphics.fill(rx, ry + rh - 1, rx + rw, ry + rh, 0xFFFFFFFF);

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        for (var renderable : this.renderables) {
            if (renderable instanceof CycleSelectWidget<?> cycle) {
                // ウェル外にはみ出すオーバーレイは出さない（下方向クリップ）
                if (cycle.getY() + cycle.getHeight() > ry + rh) {
                    continue;
                }
                cycle.renderOverlayFront(graphics);
            }
        }
        for (var renderable : this.renderables) {
            if (renderable instanceof TargetSelectButton targetBtn) {
                // ウェル内ホバー時のみプレビュー
                if (targetBtn.getY() + targetBtn.getHeight() <= ry + rh && targetBtn.getY() >= ry) {
                    targetBtn.renderHoverPreview(graphics);
                }
            }
        }
        if (this.presetList != null) {
            this.presetList.renderHoverPreview(graphics);
        }
        graphics.pose().popPose();

        // フッタボタンを最前面に再描画
        for (var renderable : this.renderables) {
            if (renderable instanceof AbstractWidget w && isFooterBarWidget(w)) {
                w.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        renderFieldLabelTooltip(graphics, mouseX, mouseY);
    }

    private static void drawEntryFrame(GuiGraphics graphics, EntryFrameDraw f) {
        graphics.fill(f.x(), f.y(), f.x() + f.w(), f.y() + f.h(), 0xFF9A9A9A);
        graphics.fill(f.x(), f.y(), f.x() + f.w(), f.y() + 1, 0xFF555555);
        graphics.fill(f.x(), f.y(), f.x() + 1, f.y() + f.h(), 0xFF555555);
        graphics.fill(f.x(), f.y() + f.h() - 1, f.x() + f.w(), f.y() + f.h(), 0xFFD0D0D0);
        graphics.fill(f.x() + f.w() - 1, f.y(), f.x() + f.w(), f.y() + f.h(), 0xFFD0D0D0);
        graphics.fill(f.x() + 1, f.y() + 1, f.x() + f.w() - 1, f.y() + f.h() - 1, 0xFFB8B8B8);
    }

    private void renderFieldLabelTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int rx = this.leftPos + 10 + LIST_W + 11;
        int ry = rightWellTop();
        int rw = this.leftPos + PANEL_W - 10 - rx;
        int rh = rightWellHeight();
        if (mouseX < rx || mouseX >= rx + rw || mouseY < ry || mouseY >= ry + rh) {
            return;
        }
        for (TickLabelDraw label : tickLabels) {
            if (label.tooltip() == null) {
                continue;
            }
            if (mouseX >= label.x()
                    && mouseX < label.x() + label.w()
                    && mouseY >= label.y()
                    && mouseY < label.y() + 10) {
                graphics.renderTooltip(this.font, label.tooltip(), mouseX, mouseY);
                return;
            }
        }
    }

    private void drawOpaquePanel(GuiGraphics graphics) {
        int x = this.leftPos;
        int y = this.topPos;
        int w = PANEL_W;
        int h = PANEL_H;

        graphics.fill(x, y, x + w, y + h, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFFFFFFFF);
        graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF555555);
        graphics.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFF555555);

        fillWell(graphics, x + 9, y + 19, LIST_W + 2, LIST_H + 2);
        int rx = x + 10 + LIST_W + 11;
        int rw = x + w - 10 - rx;
        fillWell(graphics, rx, y + 19, rw, h - 52);
    }

    private static void fillWell(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xFF8B8B8B);
        graphics.fill(x, y, x + w, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + h, 0xFF373737);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFFA0A0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private final class PresetList extends ObjectSelectionList<PresetList.Entry> {
        private @Nullable Entry hoverPreviewEntry;
        private @Nullable Entry lastHoverSynced;
        private int hoverPreviewLeft;
        private int hoverPreviewTop;
        private int hoverPreviewWidth;

        PresetList(int x, int y, int width, int height) {
            super(RegenSettingsScreen.this.minecraft, width, height, y, y + height, ROW_H);
            this.setLeftPos(x);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
        }

        void clearHoverPreview() {
            this.hoverPreviewEntry = null;
        }

        void renderHoverPreview(GuiGraphics graphics) {
            if (this.hoverPreviewEntry == null) {
                this.lastHoverSynced = null;
                return;
            }
            this.hoverPreviewEntry.preview.renderNearAnchor(
                    graphics, true, this.hoverPreviewLeft, this.hoverPreviewTop, this.hoverPreviewWidth);
        }

        private void noteHover(Entry entry, int left, int top, int width) {
            if (this.lastHoverSynced != entry) {
                entry.syncPreviewFromDraft();
                this.lastHoverSynced = entry;
            }
            this.hoverPreviewEntry = entry;
            this.hoverPreviewLeft = left;
            this.hoverPreviewTop = top;
            this.hoverPreviewWidth = width;
        }

        @Override
        protected void renderBackground(GuiGraphics graphics) {
            graphics.fill(this.x0, this.y0, this.x0 + this.width, this.y0 + this.height, 0xFF909090);
        }

        @Override
        protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int outerColor, int innerColor) {}

        @Override
        protected int getScrollbarPosition() {
            return this.x0 + this.width - LIST_SCROLLBAR_W;
        }

        @Override
        public int getRowWidth() {
            int scroll = this.getMaxScroll() > 0 ? LIST_SCROLLBAR_W : 0;
            return this.width - LIST_INNER_PAD * 2 - scroll;
        }

        @Override
        public int getRowLeft() {
            return this.x0 + LIST_INNER_PAD;
        }

        final class Entry extends ObjectSelectionList.Entry<Entry> {
            private final String name;
            private final TargetCycleHoverPreview preview = new TargetCycleHoverPreview();
            private long marqueeStartMs = -1L;

            Entry(String name) {
                this.name = name;
                syncPreviewFromDraft();
            }

            private void syncPreviewFromDraft() {
                JsonObject root = RegenPresetDraftEditor.parse(
                        RegenSettingsScreen.this.drafts.getOrDefault(this.name, "{}"));
                this.preview.setFromTargets(RegenPresetDraftEditor.readAllTargets(root));
            }

            @Override
            public void render(
                    GuiGraphics graphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovering,
                    float partialTick) {
                boolean selected = PresetList.this.getSelected() == this;
                boolean active = selected || hovering;
                if (active) {
                    graphics.fill(left, top, left + width, top + height, BAND_BORDER);
                    graphics.fill(left + 1, top + 1, left + width - 1, top + height - 1, BAND_FILL);
                }
                if (hovering) {
                    PresetList.this.noteHover(this, left, top, width);
                }

                var font = RegenSettingsScreen.this.font;
                int color = active ? 0xFFFFFF : 0xE0E0E0;
                int textPad = 5;
                int clipLeft = left + textPad;
                int clipRight = left + width - textPad;
                int textMaxW = Math.max(1, clipRight - clipLeft);
                int textW = font.width(name);
                int textY = top + Math.max(0, (height - 8) / 2);

                graphics.enableScissor(clipLeft, top, clipRight, top + height);
                if (active && textW > textMaxW) {
                    if (marqueeStartMs < 0L) {
                        marqueeStartMs = Util.getMillis();
                    }
                    int offset = marqueeOffset(textW - textMaxW, Util.getMillis() - marqueeStartMs);
                    graphics.drawString(font, name, clipLeft - offset, textY, color, false);
                } else {
                    marqueeStartMs = -1L;
                    graphics.drawString(font, name, clipLeft, textY, color, false);
                }
                graphics.disableScissor();
            }

            private static int marqueeOffset(int overflow, long elapsedMs) {
                if (overflow <= 0) {
                    return 0;
                }
                final long pauseStart = 900L;
                final long scrollMs = Math.max(2500L, overflow * 55L);
                final long pauseEnd = 900L;
                final long pauseReset = 400L;
                long cycle = pauseStart + scrollMs + pauseEnd + pauseReset;
                long t = elapsedMs % cycle;
                if (t < pauseStart) {
                    return 0;
                }
                t -= pauseStart;
                if (t < scrollMs) {
                    float p = t / (float) scrollMs;
                    return Mth.clamp(Math.round(overflow * p), 0, overflow);
                }
                t -= scrollMs;
                if (t < pauseEnd) {
                    return overflow;
                }
                return 0;
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                PresetList.this.setSelected(this);
                RegenSettingsScreen.this.select(this.name);
                this.marqueeStartMs = Util.getMillis();
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(name);
            }
        }
    }
}
