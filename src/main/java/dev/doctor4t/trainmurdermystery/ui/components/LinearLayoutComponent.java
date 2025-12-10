package dev.doctor4t.trainmurdermystery.ui.components;

import com.daqem.uilib.api.client.gui.component.IComponent;
import com.daqem.uilib.client.gui.component.AbstractComponent;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class LinearLayoutComponent extends AbstractComponent<LinearLayoutComponent> {

    private final List<IComponent<?>> children = new ArrayList<>();
    private final Orientation orientation;
    private final int spacing;

    public LinearLayoutComponent(int x, int y, int width, int height, Orientation orientation, int spacing) {
        super(null, x, y, width, height);
        this.orientation = orientation;
        this.spacing = spacing;
    }

    private void updateLayout() {
        int currentX = getX();
        int currentY = getY();

        for (IComponent<?> child : children) {
            child.setX(currentX);
            child.setY(currentY);

            if (orientation == Orientation.VERTICAL) {
                currentY += child.getHeight() + spacing;
            } else {
                currentX += child.getWidth() + spacing;
            }
        }
    }

    @Override
    public void startRenderable() {
        super.startRenderable();
        for (IComponent<?> child : children) {
            child.startRenderable();
        }
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        updateLayout();
        for (IComponent<?> child : children) {
            child.renderBase(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void renderTooltipsBase(DrawContext graphics, int mouseX, int mouseY, float delta) {
        super.renderTooltipsBase(graphics, mouseX, mouseY, delta);
        for (IComponent<?> child : children) {
            child.renderTooltipsBase(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean preformOnClickEvent(double mouseX, double mouseY, int button) {
        updateLayout();
        for (IComponent<?> child : children) {
            if (child.preformOnClickEvent(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.preformOnClickEvent(mouseX, mouseY, button);
    }

    @Override
    public void preformOnHoverEvent(double mouseX, double mouseY, float delta) {
        updateLayout();
        super.preformOnHoverEvent(mouseX, mouseY, delta);
        for (IComponent<?> child : children) {
            child.preformOnHoverEvent(mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean preformOnDragEvent(double mouseX, double mouseY, int button, double dragX, double dragY) {
        updateLayout();
        for (IComponent<?> child : children) {
            if (child.preformOnDragEvent(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.preformOnDragEvent(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean preformOnScrollEvent(double mouseX, double mouseY, double amountX, double amountY) {
        updateLayout();
        for (IComponent<?> child : children) {
            if (child.preformOnScrollEvent(mouseX, mouseY, amountX, amountY)) {
                return true;
            }
        }
        return super.preformOnScrollEvent(mouseX, mouseY, amountX, amountY);
    }

    @Override
    public boolean preformOnKeyPressedEvent(int keyCode, int scanCode, int modifiers) {
        updateLayout();
        for (IComponent<?> child : children) {
            if (child.preformOnKeyPressedEvent(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.preformOnKeyPressedEvent(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean preformOnCharTypedEvent(char c, int i) {
        updateLayout();
        for (IComponent<?> child : children) {
            if (child.preformOnCharTypedEvent(c, i)) {
                return true;
            }
        }
        return super.preformOnCharTypedEvent(c, i);
    }

    @Override
    public boolean preformOnMouseReleaseEvent(double mouseX, double mouseY, int button) {
        updateLayout();
        for (IComponent<?> child : children) {
            if (child.preformOnMouseReleaseEvent(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.preformOnMouseReleaseEvent(mouseX, mouseY, button);
    }

    @Override
    public List<IComponent<?>> getChildren() {
        return children;
    }

    public void addChild(IComponent<?> child) {
        children.add(child);
        child.setParent(this, false); // Set parent without adding to parent's children list again
        // Recalculate container size
        int totalWidth = 0;
        int totalHeight = 0;
        if (orientation == Orientation.VERTICAL) {
            for (IComponent<?> c : children) {
                totalHeight += c.getHeight();
                totalWidth = Math.max(totalWidth, c.getWidth());
            }
            totalHeight += Math.max(0, children.size() - 1) * spacing;
        } else {
            for (IComponent<?> c : children) {
                totalWidth += c.getWidth();
                totalHeight = Math.max(totalHeight, c.getHeight());
            }
            totalWidth += Math.max(0, children.size() - 1) * spacing;
        }
        setWidth(totalWidth);
        setHeight(totalHeight);
    }

    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }
}
