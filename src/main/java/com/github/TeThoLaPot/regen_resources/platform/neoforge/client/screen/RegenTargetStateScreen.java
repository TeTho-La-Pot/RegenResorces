package com.github.TeThoLaPot.regen_resources.platform.neoforge.client.screen;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget.CycleSelectWidget;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget.CheckMarkButton;
import com.github.TeThoLaPot.regen_resources.platform.neoforge.client.widget.StateValueToggle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 1 ブロックターゲットの match / restore プロパティ編集。 */
public final class RegenTargetStateScreen extends Screen {

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 220;
    private static final int CONTENT_TOP = 16;
    private static final int CONTENT_BOTTOM_PAD = 30;
    private static final int VALUE_ROW_H = 20;
    private static final int PROP_W = 100;
    private static final int INT_EDIT_THRESHOLD = 8;
    private static final int OUTER_PAD = 6;
    private static final int SECTION_PAD = 4;
    private static final int GROUP_PAD = 3;
    private static final int GROUP_INNER = 4;
    private static final int GROUP_GAP = 4;
    private static final int SECTION_GAP = 8;
    private static final String NONE_KEY = "__none__";

    private record UiFrame(int x, int y, int w, int h) {}

    private final RegenTargetEditScreen parent;
    private final int targetIndex;
    private final Block block;
    private final List<Property<?>> blockProperties;

    private final Map<String, Set<String>> matchDraft = new LinkedHashMap<>();
    private final Map<String, String> restoreDraft = new LinkedHashMap<>();

    private final List<MatchRow> matchRows = new ArrayList<>();
    private final List<RestoreRow> restoreRows = new ArrayList<>();
    private final List<UiFrame> matchGroupFrames = new ArrayList<>();
    private final List<UiFrame> restoreGroupFrames = new ArrayList<>();
    private @javax.annotation.Nullable UiFrame matchSectionFrame;
    private @javax.annotation.Nullable UiFrame restoreSectionFrame;
    private int matchLabelY;
    private int restoreLabelY;

    private int leftPos;
    private int topPos;
    private int contentScroll;
    private int contentHeight;
    private boolean pendingRebuild;

    public RegenTargetStateScreen(RegenTargetEditScreen parent, int targetIndex, RegenTargetSpec target) {
        super(Component.translatable("screen.regen_resources.target_state.title"));
        this.parent = parent;
        this.targetIndex = targetIndex;
        this.block = BuiltInRegistries.BLOCK.get(target.id());
        BlockState state = this.block.defaultBlockState();
        this.blockProperties = List.copyOf(state.getProperties());
        matchDraft.putAll(copyMatch(target.matchProperties()));
        restoreDraft.putAll(target.restoreProperties());
    }

    private int contentTopY() {
        return this.topPos + CONTENT_TOP;
    }

    private int contentBottomY() {
        return this.topPos + PANEL_H - CONTENT_BOTTOM_PAD;
    }

    private int contentViewH() {
        return Math.max(1, contentBottomY() - contentTopY());
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - contentViewH());
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - PANEL_W) / 2;
        this.topPos = (this.height - PANEL_H) / 2;
        this.clearWidgets();
        matchRows.clear();
        restoreRows.clear();

        ensureMatchRowsFromDraft();
        ensureRestoreRowsFromDraft();
        trimTrailingEmptyRows(matchRows);
        trimTrailingEmptyRows(restoreRows);
        if (matchRows.isEmpty()) {
            matchRows.add(new MatchRow());
        }
        if (restoreRows.isEmpty()) {
            restoreRows.add(new RestoreRow());
        }
        maybeAppendEmptyRow(matchRows);
        maybeAppendEmptyRow(restoreRows);

        layoutRows();
        addFooterButtons();
    }

    private void addFooterButtons() {
        int btnY = this.topPos + PANEL_H - 26;
        this.addRenderableWidget(
                Button.builder(Component.literal("✕"), b -> cancel())
                        .bounds(this.leftPos + 8, btnY, 20, 20)
                        .build());
        this.addRenderableWidget(new CheckMarkButton(this.leftPos + PANEL_W - 28, btnY, 20, 20, this::apply));
    }

    private void layoutRows() {
        matchGroupFrames.clear();
        restoreGroupFrames.clear();
        matchSectionFrame = null;
        restoreSectionFrame = null;

        int contentL = contentLeft();
        int contentR = contentRight();
        int sectionInnerL = contentL + SECTION_PAD;
        int sectionInnerR = contentR - SECTION_PAD;
        int groupInnerL = sectionInnerL + GROUP_PAD + GROUP_INNER;
        int groupW = sectionInnerR - sectionInnerL - GROUP_PAD * 2;

        int y = contentTopY() + 2 - contentScroll;
        int startY = y;
        matchLabelY = y;
        y += 12;

        int matchSectionTop = y;
        y += SECTION_PAD;
        for (int i = 0; i < matchRows.size(); i++) {
            MatchRow row = matchRows.get(i);
            int groupTop = y;
            y += GROUP_INNER;
            row.layout(groupInnerL, y);
            y += row.height();
            y += GROUP_INNER;
            matchGroupFrames.add(new UiFrame(sectionInnerL + GROUP_PAD, groupTop, groupW, y - groupTop));
            if (i < matchRows.size() - 1) {
                y += GROUP_GAP;
            }
        }
        y += SECTION_PAD;
        matchSectionFrame = new UiFrame(contentL, matchSectionTop, contentR - contentL, y - matchSectionTop);

        y += SECTION_GAP;
        restoreLabelY = y;
        y += 12;
        int restoreSectionTop = y;
        y += SECTION_PAD;
        for (int i = 0; i < restoreRows.size(); i++) {
            RestoreRow row = restoreRows.get(i);
            int groupTop = y;
            y += GROUP_INNER;
            row.layout(groupInnerL, y);
            y += row.height();
            y += GROUP_INNER;
            restoreGroupFrames.add(new UiFrame(sectionInnerL + GROUP_PAD, groupTop, groupW, y - groupTop));
            if (i < restoreRows.size() - 1) {
                y += GROUP_GAP;
            }
        }
        y += SECTION_PAD;
        restoreSectionFrame = new UiFrame(contentL, restoreSectionTop, contentR - contentL, y - restoreSectionTop);

        contentHeight = (y - startY) + 4;
        contentScroll = Mth.clamp(contentScroll, 0, maxScroll());
    }

    private int contentLeft() {
        return this.leftPos + OUTER_PAD;
    }

    private int contentRight() {
        return this.leftPos + PANEL_W - OUTER_PAD;
    }

    private int valueColumnX() {
        return contentLeft() + SECTION_PAD + GROUP_PAD + GROUP_INNER + PROP_W + 6;
    }

    private int valueColumnW() {
        return contentRight() - SECTION_PAD - GROUP_PAD - GROUP_INNER - valueColumnX();
    }

    private void ensureMatchRowsFromDraft() {
        for (String prop : matchDraft.keySet()) {
            MatchRow row = new MatchRow();
            row.propertyName = prop;
            row.selectedValues.addAll(matchDraft.get(prop));
            matchRows.add(row);
        }
    }

    private void ensureRestoreRowsFromDraft() {
        for (Map.Entry<String, String> e : restoreDraft.entrySet()) {
            RestoreRow row = new RestoreRow();
            row.propertyName = e.getKey();
            row.restoreValue = e.getValue();
            restoreRows.add(row);
        }
    }

    private static Map<String, Set<String>> copyMatch(Map<String, Set<String>> in) {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : in.entrySet()) {
            out.put(e.getKey(), new LinkedHashSet<>(e.getValue()));
        }
        return out;
    }

    private void rebuild() {
        pendingRebuild = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingRebuild) {
            pendingRebuild = false;
            refreshLayout();
        }
    }

    private void refreshLayout() {
        this.clearWidgets();
        trimTrailingEmptyRows(matchRows);
        trimTrailingEmptyRows(restoreRows);
        if (matchRows.isEmpty()) {
            matchRows.add(new MatchRow());
        }
        if (restoreRows.isEmpty()) {
            restoreRows.add(new RestoreRow());
        }
        maybeAppendEmptyRow(matchRows);
        maybeAppendEmptyRow(restoreRows);
        layoutRows();
        addFooterButtons();
    }

    private void syncDraftFromRows() {
        matchDraft.clear();
        for (MatchRow row : matchRows) {
            if (row.propertyName != null) {
                matchDraft.put(row.propertyName, Set.copyOf(row.selectedValues));
            }
        }
        restoreDraft.clear();
        for (RestoreRow row : restoreRows) {
            if (row.propertyName != null) {
                String v = row.restoreValue;
                restoreDraft.put(row.propertyName, v != null ? v : "");
            }
        }
    }

    private Set<String> usedMatchProperties() {
        Set<String> used = new LinkedHashSet<>();
        for (MatchRow row : matchRows) {
            if (row.propertyName != null) {
                used.add(row.propertyName);
            }
        }
        return used;
    }

    private Set<String> usedRestoreProperties() {
        Set<String> used = new LinkedHashSet<>();
        for (RestoreRow row : restoreRows) {
            if (row.propertyName != null) {
                used.add(row.propertyName);
            }
        }
        return used;
    }

    private List<String> availablePropertyOptions(Set<String> used, @javax.annotation.Nullable String current) {
        List<String> opts = new ArrayList<>();
        opts.add(NONE_KEY);
        for (Property<?> p : blockProperties) {
            String name = p.getName();
            if (!used.contains(name) || name.equals(current)) {
                opts.add(name);
            }
        }
        return opts;
    }

    private static boolean useIntEditBox(Property<?> prop, Collection<?> values) {
        return prop instanceof IntegerProperty && values.size() > INT_EDIT_THRESHOLD;
    }

    private void maybeAppendEmptyRow(List<?> rows) {
        if (rows.isEmpty()) {
            return;
        }
        boolean lastHasProperty = false;
        if (rows.get(rows.size() - 1) instanceof MatchRow mr) {
            lastHasProperty = mr.propertyName != null;
        } else if (rows.get(rows.size() - 1) instanceof RestoreRow rr) {
            lastHasProperty = rr.propertyName != null;
        }
        if (!lastHasProperty) {
            return;
        }
        Set<String> used = rows.get(0) instanceof MatchRow ? usedMatchProperties() : usedRestoreProperties();
        if (used.size() >= blockProperties.size()) {
            return;
        }
        if (rows.get(0) instanceof MatchRow) {
            matchRows.add(new MatchRow());
        } else {
            restoreRows.add(new RestoreRow());
        }
    }

    /** 途中の「なし」行を削除し、後ろの選択済みを繰り上げ。末尾に空き行を1つ残す。 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void compactEmptyRows(List rows) {
        rows.removeIf(RegenTargetStateScreen::isRowEmpty);
        if (rows.isEmpty()) {
            if (rows == matchRows) {
                matchRows.add(new MatchRow());
            } else {
                restoreRows.add(new RestoreRow());
            }
            return;
        }
        maybeAppendEmptyRow(rows);
    }

    private static void trimTrailingEmptyRows(List<?> rows) {
        while (rows.size() > 1) {
            Object last = rows.get(rows.size() - 1);
            Object prev = rows.get(rows.size() - 2);
            if (isRowEmpty(last) && isRowEmpty(prev)) {
                rows.remove(rows.size() - 1);
            } else {
                break;
            }
        }
    }

    private static boolean isRowEmpty(Object row) {
        if (row instanceof MatchRow mr) {
            return mr.propertyName == null;
        }
        if (row instanceof RestoreRow rr) {
            return rr.propertyName == null;
        }
        return true;
    }

    private void apply() {
        syncDraftFromRows();
        matchDraft.entrySet().removeIf(e -> e.getValue() == null || e.getValue().isEmpty());
        restoreDraft.entrySet().removeIf(e -> e.getValue() == null || e.getValue().isEmpty());
        RegenTargetSpec updated = RegenTargetSpec.block(BuiltInRegistries.BLOCK.getKey(block))
                .withMatch(matchDraft)
                .withRestore(restoreDraft);
        parent.replaceTarget(targetIndex, updated);
        this.minecraft.setScreen(parent);
    }

    private void cancel() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        drawPanel(graphics);

        int titleX = this.width / 2 - this.font.width(this.title) / 2;
        graphics.drawString(this.font, this.title, titleX, this.topPos + 6, 0x404040, false);

        int clipL = this.leftPos + 2;
        int clipT = contentTopY();
        int clipR = this.leftPos + PANEL_W - 2;
        int clipB = contentBottomY();
        graphics.enableScissor(clipL, clipT, clipR, clipB);

        if (matchSectionFrame != null) {
            drawSectionFrame(graphics, matchSectionFrame);
        }
        if (restoreSectionFrame != null) {
            drawSectionFrame(graphics, restoreSectionFrame);
        }
        for (UiFrame f : matchGroupFrames) {
            drawGroupFrame(graphics, f);
        }
        for (UiFrame f : restoreGroupFrames) {
            drawGroupFrame(graphics, f);
        }

        graphics.drawString(
                this.font,
                Component.translatable("screen.regen_resources.target_state.match"),
                contentLeft(),
                matchLabelY,
                0x404040,
                false);
        graphics.drawString(
                this.font,
                Component.translatable("screen.regen_resources.target_state.restore"),
                contentLeft(),
                restoreLabelY,
                0x404040,
                false);

        for (var widget : this.renderables) {
            if (widget instanceof Button) {
                continue;
            }
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();

        for (var widget : this.renderables) {
            if (widget instanceof Button b) {
                b.render(graphics, mouseX, mouseY, partialTick);
            }
        }
        // オーバーレイは枠外でも見えるよう scissor 外
        for (var widget : this.renderables) {
            if (widget instanceof CycleSelectWidget<?> cycle) {
                cycle.renderOverlayFront(graphics);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
    }

    private void drawPanel(GuiGraphics graphics) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + PANEL_W - 1, y + PANEL_H - 1, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + PANEL_W - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + PANEL_H - 1, 0xFFFFFFFF);
        graphics.fill(x + 1, y + PANEL_H - 2, x + PANEL_W - 1, y + PANEL_H - 1, 0xFF555555);
        graphics.fill(x + PANEL_W - 2, y + 1, x + PANEL_W - 1, y + PANEL_H - 1, 0xFF555555);
    }

    private static void drawSectionFrame(GuiGraphics graphics, UiFrame f) {
        graphics.fill(f.x(), f.y(), f.x() + f.w(), f.y() + f.h(), 0xFF8B8B8B);
        graphics.fill(f.x(), f.y(), f.x() + f.w(), f.y() + 1, 0xFF373737);
        graphics.fill(f.x(), f.y(), f.x() + 1, f.y() + f.h(), 0xFF373737);
        graphics.fill(f.x(), f.y() + f.h() - 1, f.x() + f.w(), f.y() + f.h(), 0xFFFFFFFF);
        graphics.fill(f.x() + f.w() - 1, f.y(), f.x() + f.w(), f.y() + f.h(), 0xFFFFFFFF);
        graphics.fill(f.x() + 1, f.y() + 1, f.x() + f.w() - 1, f.y() + f.h() - 1, 0xFFB0B0B0);
    }

    private static void drawGroupFrame(GuiGraphics graphics, UiFrame f) {
        graphics.fill(f.x(), f.y(), f.x() + f.w(), f.y() + f.h(), 0xFFA0A0A0);
        graphics.fill(f.x(), f.y(), f.x() + f.w(), f.y() + 1, 0xFF555555);
        graphics.fill(f.x(), f.y(), f.x() + 1, f.y() + f.h(), 0xFF555555);
        graphics.fill(f.x(), f.y() + f.h() - 1, f.x() + f.w(), f.y() + f.h(), 0xFFD0D0D0);
        graphics.fill(f.x() + f.w() - 1, f.y(), f.x() + f.w(), f.y() + f.h(), 0xFFD0D0D0);
        graphics.fill(f.x() + 1, f.y() + 1, f.x() + f.w() - 1, f.y() + f.h() - 1, 0xFFC6C6C6);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (var w : this.renderables) {
            if (w instanceof CycleSelectWidget<?> c && c.isMouseOverSelectArea(mouseX, mouseY)) {
                return c.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
            }
        }
        int max = maxScroll();
        if (max <= 0) {
            return false;
        }
        if (mouseX >= this.leftPos && mouseX < this.leftPos + PANEL_W
                && mouseY >= contentTopY() && mouseY < contentBottomY()) {
            int next = Mth.clamp(contentScroll - (int) (scrollY * 12), 0, max);
            if (next != contentScroll) {
                contentScroll = next;
                pendingRebuild = true;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled;
        if (mouseY >= this.topPos + PANEL_H - 30) {
            handled = super.mouseClicked(mouseX, mouseY, button);
        } else if (mouseX < this.leftPos
                || mouseX >= this.leftPos + PANEL_W
                || mouseY < contentTopY()
                || mouseY >= contentBottomY()) {
            handled = false;
        } else {
            handled = super.mouseClicked(mouseX, mouseY, button);
        }
        if (button == 0 && !isOverAnyEditBox(mouseX, mouseY)) {
            clearEditBoxFocus();
        }
        return handled;
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
    public boolean isPauseScreen() {
        return false;
    }

    private Component labelForPropertyKey(String key) {
        if (NONE_KEY.equals(key)) {
            return Component.translatable("screen.regen_resources.target_state.property.none");
        }
        return Component.literal(key);
    }

    private final class MatchRow {
        @javax.annotation.Nullable String propertyName;
        final Set<String> selectedValues = new LinkedHashSet<>();
        @javax.annotation.Nullable CycleSelectWidget<String> propertyCycle;
        final List<AbstractWidget> valueWidgets = new ArrayList<>();
        int y;

        void layout(int x, int y) {
            this.y = y;
            removeWidgets();

            List<String> options = availablePropertyOptions(usedMatchProperties(), propertyName);
            String initial = propertyName == null ? NONE_KEY : propertyName;
            propertyCycle = new CycleSelectWidget<>(
                    x,
                    y,
                    PROP_W,
                    18,
                    options,
                    initial,
                    RegenTargetStateScreen.this::labelForPropertyKey,
                    key -> onPropertyChanged(key));
            RegenTargetStateScreen.this.addRenderableWidget(propertyCycle);

            if (propertyName == null) {
                return;
            }
            Property<?> prop = findProperty(propertyName);
            if (prop == null) {
                return;
            }
            Collection<?> values = prop.getPossibleValues();
            int vx = valueColumnX();
            int vw = valueColumnW();
            if (useIntEditBox(prop, values)) {
                EditBox intBox = new EditBox(
                        RegenTargetStateScreen.this.font,
                        vx,
                        y,
                        vw,
                        18,
                        Component.translatable("screen.regen_resources.target_state.value"));
                intBox.setMaxLength(16);
                if (!selectedValues.isEmpty()) {
                    intBox.setValue(selectedValues.iterator().next());
                }
                intBox.setFilter(s -> s.isEmpty() || s.matches("-?\\d{1,10}"));
                intBox.setResponder(v -> {
                    selectedValues.clear();
                    if (prop.getValue(v.trim()).isPresent()) {
                        selectedValues.add(v.trim());
                    }
                });
                valueWidgets.add(intBox);
                RegenTargetStateScreen.this.addRenderableWidget(intBox);
                return;
            }

            int cy = y;
            for (Object raw : values) {
                String valueName = valueName(prop, raw);
                boolean checked = selectedValues.contains(valueName);
                StateValueToggle toggle = new StateValueToggle(vx, cy, vw, valueName, checked, selected -> {
                    if (selected) {
                        selectedValues.add(valueName);
                    } else {
                        selectedValues.remove(valueName);
                    }
                });
                valueWidgets.add(toggle);
                RegenTargetStateScreen.this.addRenderableWidget(toggle);
                cy += VALUE_ROW_H;
            }
        }

        void removeWidgets() {
            if (propertyCycle != null) {
                RegenTargetStateScreen.this.removeWidget(propertyCycle);
            }
            for (AbstractWidget w : valueWidgets) {
                RegenTargetStateScreen.this.removeWidget(w);
            }
            valueWidgets.clear();
        }

        int height() {
            if (propertyName == null) {
                return 22;
            }
            Property<?> prop = findProperty(propertyName);
            if (prop == null) {
                return 22;
            }
            Collection<?> values = prop.getPossibleValues();
            if (useIntEditBox(prop, values)) {
                return 22;
            }
            return Math.max(22, values.size() * VALUE_ROW_H + 2);
        }

        private void onPropertyChanged(String key) {
            if (NONE_KEY.equals(key)) {
                propertyName = null;
                selectedValues.clear();
                compactEmptyRows(matchRows);
            } else {
                propertyName = key;
                if (!matchDraft.containsKey(key)) {
                    selectedValues.clear();
                }
                trimTrailingEmptyRows(matchRows);
                maybeAppendEmptyRow(matchRows);
            }
            rebuild();
        }
    }

    private final class RestoreRow {
        @javax.annotation.Nullable String propertyName;
        @javax.annotation.Nullable String restoreValue;
        @javax.annotation.Nullable CycleSelectWidget<String> propertyCycle;
        @javax.annotation.Nullable CycleSelectWidget<String> valueCycle;
        @javax.annotation.Nullable EditBox intBox;
        int y;

        void layout(int x, int y) {
            this.y = y;
            removeWidgets();

            List<String> options = availablePropertyOptions(usedRestoreProperties(), propertyName);
            String initial = propertyName == null ? NONE_KEY : propertyName;
            propertyCycle = new CycleSelectWidget<>(
                    x,
                    y,
                    PROP_W,
                    18,
                    options,
                    initial,
                    RegenTargetStateScreen.this::labelForPropertyKey,
                    this::onPropertyChanged);
            RegenTargetStateScreen.this.addRenderableWidget(propertyCycle);

            if (propertyName == null) {
                return;
            }
            Property<?> prop = findProperty(propertyName);
            if (prop == null) {
                return;
            }
            Collection<?> values = prop.getPossibleValues();
            int vx = valueColumnX();
            int vw = valueColumnW();
            if (useIntEditBox(prop, values)) {
                intBox = new EditBox(
                        RegenTargetStateScreen.this.font,
                        vx,
                        y,
                        vw,
                        18,
                        Component.translatable("screen.regen_resources.target_state.value"));
                intBox.setMaxLength(16);
                if (restoreValue != null) {
                    intBox.setValue(restoreValue);
                }
                intBox.setFilter(s -> s.isEmpty() || s.matches("-?\\d{1,10}"));
                intBox.setResponder(v -> restoreValue = v.trim());
                RegenTargetStateScreen.this.addRenderableWidget(intBox);
                return;
            }

            List<String> valueOpts = new ArrayList<>();
            for (Object raw : values) {
                valueOpts.add(valueName(prop, raw));
            }
            String valInitial = restoreValue != null && valueOpts.contains(restoreValue)
                    ? restoreValue
                    : valueOpts.isEmpty() ? NONE_KEY : valueOpts.get(0);
            valueCycle = new CycleSelectWidget<>(
                    vx,
                    y,
                    vw,
                    18,
                    valueOpts,
                    valInitial,
                    Component::literal,
                    v -> restoreValue = v);
            RegenTargetStateScreen.this.addRenderableWidget(valueCycle);
        }

        void removeWidgets() {
            if (propertyCycle != null) {
                RegenTargetStateScreen.this.removeWidget(propertyCycle);
            }
            if (valueCycle != null) {
                RegenTargetStateScreen.this.removeWidget(valueCycle);
            }
            if (intBox != null) {
                RegenTargetStateScreen.this.removeWidget(intBox);
            }
            valueCycle = null;
            intBox = null;
        }

        int height() {
            return 22;
        }

        private void onPropertyChanged(String key) {
            if (NONE_KEY.equals(key)) {
                propertyName = null;
                restoreValue = null;
                compactEmptyRows(restoreRows);
            } else {
                propertyName = key;
                restoreValue = restoreDraft.get(key);
                if (restoreValue == null || restoreValue.isEmpty()) {
                    Property<?> prop = findProperty(key);
                    if (prop != null && !prop.getPossibleValues().isEmpty()) {
                        restoreValue = valueName(prop, prop.getPossibleValues().iterator().next());
                    }
                }
                trimTrailingEmptyRows(restoreRows);
                maybeAppendEmptyRow(restoreRows);
            }
            rebuild();
        }
    }

    private @javax.annotation.Nullable Property<?> findProperty(String name) {
        BlockState state = block.defaultBlockState();
        for (Property<?> p : state.getProperties()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String valueName(Property<?> prop, Object raw) {
        return ((Property) prop).getName((Comparable) raw);
    }
}
