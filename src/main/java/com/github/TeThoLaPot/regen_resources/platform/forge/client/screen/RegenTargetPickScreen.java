package com.github.TeThoLaPot.regen_resources.platform.forge.client.screen;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.widget.CheckMarkButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/** ブロック／タグ検索。一覧は上、検索は下。予測は検索欄の直上に最前面描画。 */
public final class RegenTargetPickScreen extends Screen {

    public enum Mode {
        ADD_BLOCK,
        ADD_TAG,
        TAGS_OF_BLOCK,
        /** ブロック ID のみ返す（mining_sample など） */
        PICK_BLOCK_ID
    }

    private static final int PANEL_W = 240;
    private static final int PANEL_H = 220;
    private static final int ROW_H = 18;
    private static final int SUGGEST_MAX = 5;

    private final Screen parent;
    private final Mode mode;
    private final @Nullable ResourceLocation blockForTags;
    private final @Nullable Integer replaceIndex;
    private final @Nullable Consumer<ResourceLocation> onBlockIdPicked;

    private EditBox searchBox;
    private List<PickEntry> allEntries = List.of();
    private List<PickEntry> filtered = List.of();
    private int scrollOffset;
    private int selectedIndex = -1;
    private int suggestHover = -1;

    private int leftPos;
    private int topPos;
    private int listX;
    private int listY;
    private int listW;
    private int listH;

    public RegenTargetPickScreen(RegenTargetEditScreen parent, Mode mode, @Nullable ResourceLocation blockForTags) {
        this(parent, mode, blockForTags, null, null);
    }

    RegenTargetPickScreen(
            RegenTargetEditScreen parent,
            Mode mode,
            @Nullable ResourceLocation blockForTags,
            @Nullable Integer replaceIndex) {
        this(parent, mode, blockForTags, replaceIndex, null);
    }

    private RegenTargetPickScreen(
            Screen parent,
            Mode mode,
            @Nullable ResourceLocation blockForTags,
            @Nullable Integer replaceIndex,
            @Nullable Consumer<ResourceLocation> onBlockIdPicked) {
        super(titleFor(mode));
        this.parent = parent;
        this.mode = mode;
        this.blockForTags = blockForTags;
        this.replaceIndex = replaceIndex;
        this.onBlockIdPicked = onBlockIdPicked;
    }

    /** mining_sample など、ブロック ID だけ選ぶ。 */
    public static RegenTargetPickScreen forBlockId(Screen returnTo, Consumer<ResourceLocation> onPicked) {
        return new RegenTargetPickScreen(returnTo, Mode.PICK_BLOCK_ID, null, null, onPicked);
    }

    static RegenTargetPickScreen forTagReplace(RegenTargetEditScreen parent, ResourceLocation blockId, int index) {
        return new RegenTargetPickScreen(parent, Mode.TAGS_OF_BLOCK, blockId, index, null);
    }

    private static Component titleFor(Mode mode) {
        return switch (mode) {
            case ADD_BLOCK, PICK_BLOCK_ID -> Component.translatable("screen.regen_resources.target_pick.title.block");
            case ADD_TAG -> Component.translatable("screen.regen_resources.target_pick.title.tag");
            case TAGS_OF_BLOCK -> Component.translatable("screen.regen_resources.target_pick.title.tags_of_block");
        };
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - PANEL_W) / 2;
        this.topPos = (this.height - PANEL_H) / 2;

        int searchY = this.topPos + PANEL_H - 48;
        int btnY = this.topPos + PANEL_H - 26;
        this.listX = this.leftPos + 8;
        this.listY = this.topPos + 18;
        this.listW = PANEL_W - 16;
        this.listH = searchY - 4 - this.listY;

        this.allEntries = buildEntries();
        this.filtered = new ArrayList<>(allEntries);

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + 8,
                searchY,
                PANEL_W - 16,
                18,
                Component.translatable("screen.regen_resources.target_pick.search"));
        this.searchBox.setMaxLength(128);
        this.searchBox.setHint(Component.translatable("screen.regen_resources.target_pick.search"));
        this.searchBox.setResponder(s -> applyFilter());
        this.addRenderableWidget(this.searchBox);
        this.setInitialFocus(this.searchBox);

        this.addRenderableWidget(
                Button.builder(Component.literal("✕"), b -> cancel())
                        .bounds(this.leftPos + 8, btnY, 20, 20)
                        .build());
        this.addRenderableWidget(new CheckMarkButton(this.leftPos + PANEL_W - 28, btnY, 20, 20, this::confirm));
    }

    private List<PickEntry> buildEntries() {
        List<PickEntry> out = new ArrayList<>();
        switch (mode) {
            case ADD_BLOCK, PICK_BLOCK_ID -> BuiltInRegistries.BLOCK.entrySet().stream()
                    .sorted(Comparator.comparing(e -> e.getKey().location().toString()))
                    .forEach(e -> out.add(PickEntry.block(e.getKey().location(), e.getValue())));
            case ADD_TAG -> blockTags().sorted(Comparator.comparing(ResourceLocation::toString))
                    .forEach(id -> out.add(PickEntry.tag(id)));
            case TAGS_OF_BLOCK -> {
                if (blockForTags != null) {
                    Block block = BuiltInRegistries.BLOCK.get(blockForTags);
                    if (block != null) {
                        Holder<Block> holder = block.builtInRegistryHolder();
                        blockTags()
                                .filter(id -> blockInTag(holder, id))
                                .sorted(Comparator.comparing(ResourceLocation::toString))
                                .forEach(id -> out.add(PickEntry.tag(id)));
                    }
                }
            }
        }
        return out;
    }

    private Stream<ResourceLocation> blockTags() {
        if (this.minecraft != null && this.minecraft.level != null) {
            return this.minecraft.level.registryAccess()
                    .registryOrThrow(Registries.BLOCK)
                    .getTags()
                    .map(pair -> pair.getFirst().location());
        }
        return BuiltInRegistries.BLOCK.getTags().map(pair -> pair.getFirst().location());
    }

    private static boolean blockInTag(Holder<Block> holder, ResourceLocation tagId) {
        TagKey<Block> key = TagKey.create(Registries.BLOCK, tagId);
        Optional<HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(key);
        return tag.isPresent() && tag.get().contains(holder);
    }

    private void applyFilter() {
        String q = this.searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) {
            filtered = new ArrayList<>(allEntries);
        } else {
            filtered = allEntries.stream().filter(e -> e.matchesQuery(q)).toList();
        }
        scrollOffset = 0;
        selectedIndex = filtered.isEmpty() ? -1 : 0;
        suggestHover = -1;
    }

    private List<PickEntry> suggestions() {
        String q = this.searchBox != null ? this.searchBox.getValue().trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) {
            return List.of();
        }
        return filtered.stream().limit(SUGGEST_MAX).toList();
    }

    private void confirm() {
        if (selectedIndex < 0 || selectedIndex >= filtered.size()) {
            return;
        }
        PickEntry entry = filtered.get(selectedIndex);
        if (mode == Mode.PICK_BLOCK_ID) {
            if (onBlockIdPicked != null && !entry.tag()) {
                onBlockIdPicked.accept(entry.id());
            }
            return;
        }
        if (!(parent instanceof RegenTargetEditScreen targetParent)) {
            this.minecraft.setScreen(parent);
            return;
        }
        RegenTargetSpec spec = entry.tag() ? RegenTargetSpec.ofTag(entry.id()) : RegenTargetSpec.block(entry.id());
        if (replaceIndex != null) {
            targetParent.replaceTarget(replaceIndex, spec);
        } else {
            targetParent.addTarget(spec);
        }
        this.minecraft.setScreen(parent);
    }

    private void cancel() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        drawPanel(graphics);

        int titleX = this.width / 2 - this.font.width(this.title) / 2;
        graphics.drawString(this.font, this.title, titleX, this.topPos + 6, 0x404040, false);

        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF373737);
        graphics.renderOutline(listX, listY, listW, listH, 0xFF555555);

        int visibleRows = Math.max(1, listH / ROW_H);
        int maxScroll = Math.max(0, filtered.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        graphics.enableScissor(listX + 1, listY + 1, listX + listW - 1, listY + listH - 1);
        for (int row = 0; row < visibleRows; row++) {
            int idx = scrollOffset + row;
            if (idx >= filtered.size()) {
                break;
            }
            PickEntry entry = filtered.get(idx);
            int ry = listY + row * ROW_H;
            boolean selected = idx == selectedIndex;
            boolean hover = mouseX >= listX && mouseX < listX + listW && mouseY >= ry && mouseY < ry + ROW_H
                    && mouseY < listY + listH;
            if (selected || hover) {
                graphics.fill(listX + 1, ry, listX + listW - 1, ry + ROW_H, selected ? 0xFF6B6B6B : 0xFF505050);
            }
            if (!entry.icon().isEmpty()) {
                graphics.renderItem(entry.icon(), listX + 2, ry + 1);
            }
            graphics.drawString(this.font, entry.label(), listX + 22, ry + 5, 0xFFE0E0E0, false);
        }
        graphics.disableScissor();

        for (var widget : this.renderables) {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        renderSuggestions(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    private void renderSuggestions(GuiGraphics graphics, int mouseX, int mouseY) {
        List<PickEntry> sug = suggestions();
        if (sug.isEmpty() || searchBox == null || !searchBox.isFocused()) {
            suggestHover = -1;
            return;
        }
        int sw = searchBox.getWidth();
        int sh = sug.size() * ROW_H + 4;
        int sx = searchBox.getX();
        int sy = searchBox.getY() - sh - 2;
        graphics.fill(sx, sy, sx + sw, sy + sh, 0xF0101020);
        graphics.renderOutline(sx, sy, sw, sh, 0xFFAA55FF);

        suggestHover = -1;
        for (int i = 0; i < sug.size(); i++) {
            int ry = sy + 2 + i * ROW_H;
            boolean hot = mouseX >= sx && mouseX < sx + sw && mouseY >= ry && mouseY < ry + ROW_H;
            if (hot) {
                suggestHover = i;
                graphics.fill(sx + 1, ry, sx + sw - 1, ry + ROW_H, 0xFF5A5A6A);
            }
            PickEntry e = sug.get(i);
            String text = e.id().toString();
            if (this.font.width(text) > sw - 8) {
                text = this.font.plainSubstrByWidth(text, sw - 12) + "…";
            }
            graphics.drawString(this.font, text, sx + 4, ry + 5, hot ? 0xFFFFFF55 : 0xFFCCCCCC, false);
        }
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && suggestHover >= 0) {
            List<PickEntry> sug = suggestions();
            if (suggestHover < sug.size()) {
                PickEntry chosen = sug.get(suggestHover);
                int idx = filtered.indexOf(chosen);
                if (idx >= 0) {
                    selectedIndex = idx;
                    searchBox.setValue(chosen.id().toString());
                    searchBox.moveCursorToEnd();
                }
                return true;
            }
        }
        if (button == 0 && mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            int row = (int) ((mouseY - listY) / ROW_H);
            int idx = scrollOffset + row;
            if (idx >= 0 && idx < filtered.size()) {
                selectedIndex = idx;
                clearSearchFocus();
                return true;
            }
        }
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && (searchBox == null || !searchBox.isMouseOver(mouseX, mouseY))) {
            clearSearchFocus();
        }
        return handled;
    }

    private void clearSearchFocus() {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.setFocused(false);
        }
        if (this.getFocused() == searchBox) {
            this.setFocused(null);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            int visibleRows = Math.max(1, listH / ROW_H);
            int maxScroll = Math.max(0, filtered.size() - visibleRows);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) delta));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record PickEntry(ResourceLocation id, boolean tag, Component label, ItemStack icon) {
        static PickEntry block(ResourceLocation id, Block block) {
            return new PickEntry(id, false, block.getName(), new ItemStack(block));
        }

        static PickEntry tag(ResourceLocation id) {
            return new PickEntry(
                    id,
                    true,
                    Component.literal("#" + id),
                    net.minecraft.world.item.Items.NAME_TAG.getDefaultInstance());
        }

        boolean matchesQuery(String q) {
            return id.toString().toLowerCase(Locale.ROOT).contains(q)
                    || id.getPath().toLowerCase(Locale.ROOT).contains(q)
                    || label.getString().toLowerCase(Locale.ROOT).contains(q);
        }
    }
}
