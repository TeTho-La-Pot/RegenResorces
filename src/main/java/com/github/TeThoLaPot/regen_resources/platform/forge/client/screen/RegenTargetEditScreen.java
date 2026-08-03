package com.github.TeThoLaPot.regen_resources.platform.forge.client.screen;

import com.github.TeThoLaPot.regen_resources.common.regen.RegenTargetSpec;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.widget.CheckMarkButton;
import com.github.TeThoLaPot.regen_resources.platform.forge.client.widget.ContextPopupMenu;
import com.github.TeThoLaPot.regen_resources.platform.forge.inventory.RegenTargetMenu;
import com.github.TeThoLaPot.regen_resources.platform.forge.inventory.TargetFilterSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * ターゲット編集。下段は実インベントリの表示（ローカル操作のみ）。
 * 上段は {@link TargetFilterSlot}（バニラと同じスロット描画座標）。
 */
public final class RegenTargetEditScreen extends AbstractContainerScreen<RegenTargetMenu> {

    private final Screen parent;
    private final Consumer<List<RegenTargetSpec>> onConfirm;
    private final ContextPopupMenu contextMenu = new ContextPopupMenu();
    private final AbstractContainerMenu previousMenu;
    private int infoX;
    private int infoY;
    private static final int INFO_SIZE = 12;

    public RegenTargetEditScreen(
            RegenTargetMenu menu,
            Inventory playerInv,
            Screen parent,
            Consumer<List<RegenTargetSpec>> onConfirm) {
        super(menu, playerInv, Component.translatable("screen.regen_resources.target_edit.title"));
        this.parent = parent;
        this.onConfirm = onConfirm;
        this.imageWidth = 176;
        this.imageHeight = 166;
        LocalPlayer player = Minecraft.getInstance().player;
        this.previousMenu = player != null ? player.containerMenu : null;
    }

    public static void open(Screen parent, List<RegenTargetSpec> initial, Consumer<List<RegenTargetSpec>> onConfirm) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        RegenTargetMenu menu = new RegenTargetMenu(0, player.getInventory(), initial);
        player.containerMenu = menu;
        mc.setScreen(new RegenTargetEditScreen(menu, player.getInventory(), parent, onConfirm));
    }

    public void addTarget(RegenTargetSpec spec) {
        this.menu.addTarget(spec);
    }

    public void replaceTarget(int index, RegenTargetSpec spec) {
        this.menu.replaceTarget(index, spec);
    }

    public void acceptGhostIngredient(ItemStack stack) {
        RegenTargetSpec spec = specFromBlockItem(stack);
        if (spec != null) {
            addTarget(spec);
        }
    }

    public void acceptGhostAtIndex(int visibleIndex, ItemStack stack) {
        RegenTargetSpec spec = specFromBlockItem(stack);
        if (spec == null) {
            return;
        }
        this.menu.setTargetAt(this.menu.targetIndexForVisible(visibleIndex), spec);
    }

    public static int maxRegisterSlots() {
        return RegenTargetMenu.VISIBLE_SLOTS;
    }

    public Rect2i getRegisterSlotArea(int visibleIndex) {
        int col = visibleIndex % RegenTargetMenu.FILTER_COLS;
        int row = visibleIndex / RegenTargetMenu.FILTER_COLS;
        int x = this.leftPos + 8 + col * 18;
        int y = this.topPos + RegenTargetMenu.FILTER_SLOT_Y0 + row * 18;
        return new Rect2i(x, y, 16, 16);
    }

    @Override
    protected void init() {
        super.init();
        this.infoX = this.leftPos + this.imageWidth - INFO_SIZE - 6;
        this.infoY = this.topPos + 2;
        // フィルタ2段(18,36)とプレイヤー(84)のあいだ
        int barY = this.topPos + 56;
        this.addRenderableWidget(
                Button.builder(Component.literal("✕"), b -> cancel())
                        .bounds(this.leftPos + 8, barY, 20, 20)
                        .build());
        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.regen_resources.target_edit.clear_all"), b -> clearAll())
                        .bounds(this.leftPos + 36, barY, 80, 20)
                        .build());
        this.addRenderableWidget(
                new CheckMarkButton(this.leftPos + this.imageWidth - 28, barY, 20, 20, this::confirm));
    }

    private void confirm() {
        this.menu.clearCarried();
        onConfirm.accept(List.copyOf(this.menu.targets()));
        leaveToParent();
    }

    private void cancel() {
        this.menu.clearCarried();
        leaveToParent();
    }

    private void clearAll() {
        this.menu.clearCarried();
        this.menu.clearTargets();
    }

    private void leaveToParent() {
        if (this.minecraft.player != null) {
            this.minecraft.player.containerMenu =
                    previousMenu != null ? previousMenu : this.minecraft.player.inventoryMenu;
        }
        this.minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        this.menu.clearCarried();
        leaveToParent();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + this.imageWidth - 1, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + this.imageHeight - 1, 0xFFFFFFFF);
        graphics.fill(x + 1, y + this.imageHeight - 2, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xFF555555);
        graphics.fill(x + this.imageWidth - 2, y + 1, x + this.imageWidth - 1, y + this.imageHeight - 1, 0xFF555555);

        // バニラ同様: ウェルは slot より 1px 外側の 18x18、アイテムは slot.x/y
        for (Slot slot : this.menu.slots) {
            drawSlotWell(graphics, this.leftPos + slot.x - 1, this.topPos + slot.y - 1);
        }
    }

    private static void drawSlotWell(GuiGraphics graphics, int x, int y) {
        // バニラ・スロット相当: 面 #8B8B8B、上左 #373737、下右 #FFFFFF
        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 18, 0xFF373737);
        graphics.fill(x, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!contextMenu.isOpen()) {
            this.renderTooltip(graphics, mouseX, mouseY);
        }
        // スロットアイテムより前面に出す（バニラアイテム描画の z より上）
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        contextMenu.render(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
        if (isOverInfo(x, y)) {
            List<Component> lines = List.of(
                    Component.translatable("screen.regen_resources.target_edit.info.1"),
                    Component.translatable("screen.regen_resources.target_edit.info.2"),
                    Component.translatable("screen.regen_resources.target_edit.info.3"),
                    Component.translatable("screen.regen_resources.target_edit.info.4"),
                    Component.translatable("screen.regen_resources.target_edit.info.5"));
            graphics.renderComponentTooltip(this.font, lines, x, y);
            return;
        }
        if (this.hoveredSlot instanceof TargetFilterSlot filter) {
            ItemStack stack = filter.getItem();
            if (stack.isEmpty()) {
                return;
            }
            int ti = filter.targetIndex();
            List<RegenTargetSpec> targets = this.menu.targets();
            Component title = stack.getHoverName();
            if (ti >= 0 && ti < targets.size()) {
                RegenTargetSpec spec = targets.get(ti);
                if (spec.isTag()) {
                    title = Component.literal(spec.displayId());
                }
            }
            List<Component> lines = List.of(
                    title,
                    Component.translatable("screen.regen_resources.target_edit.tooltip.left_click")
                            .withStyle(ChatFormatting.GRAY),
                    Component.translatable("screen.regen_resources.target_edit.tooltip.right_click")
                            .withStyle(ChatFormatting.GRAY));
            graphics.renderTooltip(this.font, lines, Optional.empty(), stack, x, y);
            return;
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (contextMenu.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }
        if (isOverFilterArea(mouseX, mouseY) && this.menu.maxFilterScrollRows() > 0) {
            int rows = delta > 0 ? 1 : -1;
            if (this.menu.scrollFilter(rows)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean isOverFilterArea(double mouseX, double mouseY) {
        int x0 = this.leftPos + 7;
        int y0 = this.topPos + RegenTargetMenu.FILTER_SLOT_Y0 - 1;
        int x1 = x0 + RegenTargetMenu.FILTER_COLS * 18 + 2;
        int y1 = y0 + RegenTargetMenu.FILTER_ROWS * 18 + 2;
        return mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, this.height, 0xC0101010);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, (this.imageWidth - this.font.width(this.title)) / 2, 6, 0x404040, false);
        // ローカル座標で情報マーク（renderLabels は leftPos/topPos 基準）
        int ix = infoX - this.leftPos;
        int iy = infoY - this.topPos;
        boolean hot = isOverInfo(mouseX, mouseY);
        graphics.fill(ix, iy, ix + INFO_SIZE, iy + INFO_SIZE, hot ? 0xFF5A5A6A : 0xFF3A3A3A);
        graphics.renderOutline(ix, iy, INFO_SIZE, INFO_SIZE, hot ? 0xFFFFFFFF : 0xFF888898);
        String mark = "i";
        graphics.drawString(
                this.font,
                mark,
                ix + (INFO_SIZE - this.font.width(mark)) / 2,
                iy + (INFO_SIZE - 8) / 2,
                hot ? 0xFFFFFF55 : 0xFFE0E0E0,
                false);
    }

    private boolean isOverInfo(double mouseX, double mouseY) {
        return mouseX >= infoX && mouseX < infoX + INFO_SIZE && mouseY >= infoY && mouseY < infoY + INFO_SIZE;
    }

    /**
     * サーバへクリックを送らない（containerId=0 が inventoryMenu と衝突してスロットがずれる）。
     * ゴースト操作のみローカルで行う。
     */
    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        if (slot instanceof TargetFilterSlot filter) {
            handleFilterClick(filter, mouseButton);
            return;
        }
        if (slot == null) {
            this.menu.clearCarried();
            return;
        }
        // プレイヤー枠: 実体は動かさず、持っている表示だけゴーストでコピー
        if (mouseButton == 0) {
            ItemStack carried = this.menu.getCarried();
            if (!carried.isEmpty()) {
                this.menu.clearCarried();
                return;
            }
            ItemStack inSlot = slot.getItem();
            if (!inSlot.isEmpty()) {
                this.menu.setCarried(inSlot.copyWithCount(1));
            }
        }
    }

    private void handleFilterClick(TargetFilterSlot filter, int mouseButton) {
        int index = filter.targetIndex();
        ItemStack carried = this.menu.getCarried();
        if (mouseButton == 0) {
            if (!carried.isEmpty()) {
                RegenTargetSpec spec = specFromBlockItem(carried);
                if (spec != null) {
                    this.menu.setTargetAt(index, spec);
                }
                // 消費なし（ゴーストのまま）
                return;
            }
            if (index < this.menu.targets().size()) {
                this.menu.removeTarget(index);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (contextMenu.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 1) {
            Slot under = this.getSlotUnderMouse();
            if (under instanceof TargetFilterSlot filter) {
                openFilterContext(filter, (int) mouseX, (int) mouseY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openFilterContext(TargetFilterSlot filter, int mouseX, int mouseY) {
        int index = filter.targetIndex();
        if (index < this.menu.targets().size()) {
            RegenTargetSpec spec = this.menu.targets().get(index);
            if (!spec.isTag()) {
                contextMenu.open(
                        mouseX,
                        mouseY,
                        List.of(
                                new ContextPopupMenu.Item(
                                        Component.translatable("screen.regen_resources.target.context.edit"),
                                        () -> openStateEditor(index, spec)),
                                new ContextPopupMenu.Item(
                                        Component.translatable("screen.regen_resources.target.context.switch_tag"),
                                        () -> openTagOfBlockPicker(index, spec))));
            }
        } else if (this.menu.targets().size() < RegenTargetMenu.MAX_TARGETS) {
            contextMenu.open(
                    mouseX,
                    mouseY,
                    List.of(
                            new ContextPopupMenu.Item(
                                    Component.translatable("screen.regen_resources.target.context.add_block"),
                                    this::openBlockPicker),
                            new ContextPopupMenu.Item(
                                    Component.translatable("screen.regen_resources.target.context.add_tag"),
                                    this::openTagPicker)));
        }
    }

    private void openBlockPicker() {
        this.menu.clearCarried();
        this.minecraft.setScreen(new RegenTargetPickScreen(this, RegenTargetPickScreen.Mode.ADD_BLOCK, null));
    }

    private void openTagPicker() {
        this.menu.clearCarried();
        this.minecraft.setScreen(new RegenTargetPickScreen(this, RegenTargetPickScreen.Mode.ADD_TAG, null));
    }

    private void openTagOfBlockPicker(int index, RegenTargetSpec spec) {
        this.menu.clearCarried();
        this.minecraft.setScreen(RegenTargetPickScreen.forTagReplace(this, spec.id(), index));
    }

    private void openStateEditor(int index, RegenTargetSpec spec) {
        this.menu.clearCarried();
        this.minecraft.setScreen(new RegenTargetStateScreen(this, index, spec));
    }

    private static @Nullable RegenTargetSpec specFromBlockItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        return id == null ? null : RegenTargetSpec.block(id);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
