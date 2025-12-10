package dev.doctor4t.trainmurdermystery.ui.components;

import com.daqem.uilib.api.client.gui.component.IComponent;
import com.daqem.uilib.client.gui.component.AbstractComponent;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class LinearLayoutComponent extends AbstractComponent {

    private final List<IComponent<?>> children = new ArrayList<>();
    private final Orientation orientation;
    private final int spacing;

    public LinearLayoutComponent(int x, int y, int width, int height, Orientation orientation, int spacing) {
        super(x, y, width, height);
        this.orientation = orientation;
        this.spacing = spacing;
    }

    @Override
    public void renderBase(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int currentX = getX();
        int currentY = getY();

        for (IComponent<?> child : children) {
            child.setX(currentX);
            child.setY(currentY);
            child.renderBase(graphics, mouseX, mouseY, delta);

            if (orientation == Orientation.VERTICAL) {
                currentY += child.getHeight() + spacing;
            } else {
                currentX += child.getWidth() + spacing;
            }
        }
    }

    @Override
    public boolean preformOnClickEvent(double mouseX, double mouseY, int button) {
        for (IComponent<?> child : children) {
            if (child.preformOnClickEvent(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.preformOnClickEvent(mouseX, mouseY, button);
    }

    @Override
    public List<IComponent<?>> getChildren() {
        return children;
    }

    public void addChild(IComponent<?> child) {
        children.add(child);
        if (orientation == Orientation.VERTICAL) {
            setHeight(getHeight() + child.getHeight() + spacing);
        } else {
            setWidth(getWidth() + child.getWidth() + spacing);
        }
    }

    public enum Orientation {
        VERTICAL,
        HORIZONTAL
    }
}
