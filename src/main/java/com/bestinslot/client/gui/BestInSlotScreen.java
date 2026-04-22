package com.bestinslot.client.gui;

import com.bestinslot.client.query.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@SuppressWarnings("null")
public class BestInSlotScreen extends Screen {

    private static final int PADDING = 8;
    private static final int TAB_HEIGHT = 20;
    private static final int SORT_BAR_HEIGHT = 20;
    private static final int ROW_HEIGHT = 22;
    private static final int ICON_SIZE = 16;

    private final ItemQueryProvider queryProvider = new AttributeQueryProvider();

    private ItemCategory currentCategory = ItemCategory.MELEE_WEAPONS;
    private SortCriteria currentSort = currentCategory.getDefaultSort();
    private boolean ascending = false;

    private List<CategorizedItem> items = List.of();
    private int scrollOffset = 0;
    private int contentLeft;
    private int contentTop;
    private int contentWidth;
    private int contentHeight;
    private int visibleRows;

    public BestInSlotScreen() {
        super(Component.translatable("screen.bestinslot.title"));
    }

    @Override
    protected void init() {
        super.init();

        contentWidth = Math.min(360, this.width - PADDING * 2);
        int totalHeight = this.height - PADDING * 2;
        contentLeft = (this.width - contentWidth) / 2;
        contentTop = PADDING;
        contentHeight = totalHeight;

        visibleRows = (contentHeight - TAB_HEIGHT - SORT_BAR_HEIGHT - PADDING) / ROW_HEIGHT;

        buildWidgets();
        refreshItems();
    }

    private void buildWidgets() {
        clearWidgets();

        // Category tabs
        ItemCategory[] categories = ItemCategory.values();
        int tabWidth = contentWidth / categories.length;
        for (int i = 0; i < categories.length; i++) {
            ItemCategory cat = categories[i];
            int x = contentLeft + i * tabWidth;
            int w = (i == categories.length - 1) ? (contentWidth - i * tabWidth) : tabWidth;
            addRenderableWidget(Button.builder(
                    Component.literal(cat.getDisplayName()),
                    btn -> selectCategory(cat)
            ).bounds(x, contentTop, w, TAB_HEIGHT).build());
        }

        // Sort buttons
        SortCriteria[] sorts = currentCategory.getApplicableSorts();
        int sortY = contentTop + TAB_HEIGHT;
        int sortBtnWidth = contentWidth / sorts.length;
        for (int i = 0; i < sorts.length; i++) {
            SortCriteria sort = sorts[i];
            int x = contentLeft + i * sortBtnWidth;
            int w = (i == sorts.length - 1) ? (contentWidth - i * sortBtnWidth) : sortBtnWidth;
            String label = sort.getDisplayName();
            if (sort == currentSort) {
                label += ascending ? " \u25B2" : " \u25BC";
            }
            addRenderableWidget(Button.builder(
                    Component.literal(label),
                    btn -> selectSort(sort)
            ).bounds(x, sortY, w, SORT_BAR_HEIGHT).build());
        }
    }

    private void selectCategory(ItemCategory category) {
        if (currentCategory == category) return;
        currentCategory = category;
        currentSort = category.getDefaultSort();
        ascending = false;
        scrollOffset = 0;
        buildWidgets();
        refreshItems();
    }

    private void selectSort(SortCriteria sort) {
        if (currentSort == sort) {
            ascending = !ascending;
        } else {
            currentSort = sort;
            ascending = false;
        }
        scrollOffset = 0;
        buildWidgets();
        refreshItems();
    }

    private void refreshItems() {
        items = queryProvider.query(currentCategory, currentSort, ascending);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int listTop = contentTop + TAB_HEIGHT + SORT_BAR_HEIGHT;
        int listBottom = contentTop + contentHeight;

        // Background for the list area
        graphics.fill(contentLeft, listTop, contentLeft + contentWidth, listBottom, 0xCC000000);

        // Draw items
        int maxScroll = Math.max(0, items.size() - visibleRows);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        // Column headers info
        SortCriteria[] sorts = currentCategory.getApplicableSorts();

        ItemStack hoveredStack = ItemStack.EMPTY;
        int hoveredX = 0, hoveredY = 0;

        for (int i = 0; i < visibleRows && (i + scrollOffset) < items.size(); i++) {
            CategorizedItem entry = items.get(i + scrollOffset);
            int rowY = listTop + i * ROW_HEIGHT + 2;
            int rowX = contentLeft + 4;

            // Alternating row background
            if (i % 2 == 0) {
                graphics.fill(contentLeft, rowY - 2, contentLeft + contentWidth, rowY + ROW_HEIGHT - 2, 0x20FFFFFF);
            }

            // Row number
            String rank = String.valueOf(i + scrollOffset + 1) + ".";
            graphics.drawString(this.font, rank, rowX, rowY + 4, 0x888888, false);
            rowX += this.font.width("000.") + 2;

            // Item icon
            graphics.renderItem(entry.stack(), rowX, rowY);
            int iconX = rowX;
            int iconY = rowY;
            rowX += ICON_SIZE + 4;

            // Item name (truncated if needed)
            String name = entry.stack().getHoverName().getString();
            int nameMaxWidth = contentWidth / 3;
            if (this.font.width(name) > nameMaxWidth) {
                while (this.font.width(name + "...") > nameMaxWidth && name.length() > 1) {
                    name = name.substring(0, name.length() - 1);
                }
                name += "...";
            }
            graphics.drawString(this.font, name, rowX, rowY + 4, 0xFFFFFF, false);
            rowX += nameMaxWidth + 8;

            // Stat values
            int statColWidth = (contentLeft + contentWidth - rowX) / Math.max(sorts.length, 1);
            for (int s = 0; s < sorts.length; s++) {
                String val = entry.getFormattedStat(sorts[s]);
                int statX = rowX + s * statColWidth;
                int textWidth = this.font.width(val);
                graphics.drawString(this.font, val, statX + (statColWidth - textWidth) / 2, rowY + 4, 0xAAFFAA, false);
            }

            // Check if mouse is hovering over the item icon for tooltip
            if (mouseX >= iconX && mouseX < iconX + ICON_SIZE
                    && mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                hoveredStack = entry.stack();
                hoveredX = mouseX;
                hoveredY = mouseY;
            }
        }

        // Scrollbar
        if (items.size() > visibleRows) {
            int scrollbarX = contentLeft + contentWidth - 4;
            int scrollAreaHeight = listBottom - listTop;
            int thumbHeight = Math.max(10, scrollAreaHeight * visibleRows / items.size());
            int thumbY = listTop + (scrollAreaHeight - thumbHeight) * scrollOffset / maxScroll;
            graphics.fill(scrollbarX, listTop, scrollbarX + 4, listBottom, 0x40FFFFFF);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xAAFFFFFF);
        }

        // Item count
        String countText = items.size() + " items";
        graphics.drawString(this.font, countText, contentLeft + contentWidth - this.font.width(countText) - 6,
                listBottom - 12, 0x666666, false);

        // Tooltip (rendered last so it's on top)
        if (!hoveredStack.isEmpty()) {
            graphics.renderTooltip(this.font, hoveredStack, hoveredX, hoveredY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, items.size() - visibleRows);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 3));
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
