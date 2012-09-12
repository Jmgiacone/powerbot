/*import com.divicus.rsbot.framework.paint.components.Orientation;
import com.divicus.rsbot.framework.paint.components.PaintPanel;
import com.divicus.rsbot.framework.paint.components.PaintString;
import com.divicus.rsbot.framework.paint.components.PaintStringBuilder;     */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Game;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.widget.Widget;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.event.listener.PaintListener;

/**
 *
 * @author NaderSl
 */
@Manifest(name = "Divines Widget Explorer", authors = "Divine", version = 1.0, description = "A Powerfull Interactive Widget Explorer")
public class WidgetExplorer {//extends ActiveScript {

    /*private PaintPanel mainPanel;
    private final Font font = new Font("Arial", Font.PLAIN, 10);
    private String infoStrings[] = new String[]{
        "Index", "Validated", "Visible", "Absolute Loc", "Relative Loc", "Width", "Height", "ID", "Type", "Special Type",
        "Child ID", "Child Idx", "Texture ID", "Text", "Text Color", "Shadow Color", "Tooltip", "Border Thickness", "Selected Action",
        "Model ID", "Model Type", "Model Zoom", "Inventory", "Child Stack Size", "Bound-Array Idx", "Scrollable", "Parent ID"};
    private Object infoVals[];
    private final PaintString infoPaintStrings[] = new PaintString[27];

    @Override
    protected void setup() {

        provide(new UpdateStrat());
        final Manifest mf = getClass().getAnnotation(Manifest.class);
        PaintStringBuilder mainTitle = new PaintStringBuilder(new Point(0, 0));
        mainTitle.append(new PaintString(mf.name(), new Font("Arial", Font.BOLD, 10), Color.ORANGE, Color.BLACK));
        mainPanel = new PaintPanel(mainTitle, new Color(20, 20, 20, 180), Color.BLACK, new Point(), new Dimension(150, 330),
                new Color(20, 20, 20, 150), Color.BLACK);
        mainPanel.setMenuOrientation(Orientation.TOP);
        mainPanel.setMarginX(5);
        mainPanel.setMarginY(5);
        mainPanel.setSpacingY(1);

        for (int i = 0; i < 27; i++) {
            PaintStringBuilder psb = new PaintStringBuilder(new Point(0, 0));
            psb.append(new PaintString(infoStrings[i] + ": ", font, Color.MAGENTA, Color.BLACK));
            psb.append(infoPaintStrings[i] = new PaintString("", font, Color.WHITE, Color.BLACK));
            mainPanel.add(psb);
        }
    }

    private class UpdateStrat extends Strategy implements Runnable, MouseMotionListener, PaintListener, Comparator<WidgetChild> {

        private final Timer refresh = new Timer(10);
        private final List<WidgetChild> hoveredWidgets = Collections.synchronizedList(new ArrayList<WidgetChild>());
        private final Filter<WidgetChild> hoverFilter = new Filter<WidgetChild>() {
            @Override
            public boolean accept(WidgetChild child) {
                return child != null && child.validate() && child.visible();
            }
        };

        @Override
        public void run() {
            mainPanel.setLocation(0, 70);
        }

        @Override
        public boolean validate() {
            return Game.isLoggedIn();
        }

        private int getArea(WidgetChild child) {
            final Rectangle bounds = child.getBoundingRectangle();
            return bounds.width * bounds.height;
        }


        private WidgetChild[] getAllChildren(final Filter<WidgetChild> filter) {
            final ArrayList<WidgetChild> children = new ArrayList<WidgetChild>();

            for (final Widget widget : Widgets.getLoaded()) {
                for (final WidgetChild child : widget.getChildren()) {
                    if (filter.accept(child)) {
                        children.add(child);
                    }
                    for (final WidgetChild childSub : child.getChildren()) {
                        if (filter.accept(childSub)) {
                            children.add(childSub);
                        }
                    }
                }
            }
            return children.toArray(new WidgetChild[children.size()]);
        }

        private String getChatInput() {
            final WidgetChild chatInput = Widgets.get(137, 55);
            return chatInput == null || !chatInput.validate() ? null : chatInput.getText();
        }

        private int getOrder() {
            final String input = getChatInput();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ne) {
                return 0;
            }

        }

        private WidgetChild getHovered() {
            if (hoveredWidgets.isEmpty()) {
                return null;
            }
            final int order = getOrder();
            if (order > hoveredWidgets.size()) {
                return hoveredWidgets.get(hoveredWidgets.size() - 1);
            }

            if (order < 0) {
                return hoveredWidgets.get(0);
            }
            return hoveredWidgets.get(order);

        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (refresh.isRunning()) {
                return;
            }
            hoveredWidgets.clear();
            for (WidgetChild child : getAllChildren(hoverFilter)) {
                if (child.getBoundingRectangle().contains(e.getPoint())) {
                    hoveredWidgets.add(child);
                }
            }
            Collections.sort(hoveredWidgets, this);
            refresh.reset();
        }

        @Override
        public void onRepaint(Graphics g) {
            if (mainPanel != null) {
                mainPanel.paint((Graphics2D) g);
            }
            final WidgetChild hovered = getHovered();
            if (hovered == null) {
                return;
            }
            try {
                infoVals = new Object[]{
                    hovered.getIndex(), hovered.validate(), hovered.visible(), hovered.getAbsoluteLocation().x + "," + hovered.getAbsoluteLocation().y, hovered.getRelativeLocation().x + "," + hovered.getRelativeLocation().y,
                    hovered.getWidth(), hovered.getHeight(), hovered.getId(), hovered.getType(), hovered.getSpecialType(), hovered.getChildId(),
                    hovered.getChildIndex(), hovered.getTextureId(), hovered.getText(), hovered.getTextColor(), hovered.getShadowColor(),
                    hovered.getTooltip(), hovered.getBorderThickness(), hovered.getSelectedAction(), hovered.getModelId(), hovered.getModelType(),
                    hovered.getModelZoom(), hovered.isInventory(), hovered.getChildStackSize(), hovered.getBoundsArrayIndex(), hovered.isInScrollableArea(), hovered.getParentId()
                };
                for (int i = 0; i < infoPaintStrings.length; i++) {
                    infoPaintStrings[i].setText(infoVals[i] == null ? "" : infoVals[i].toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            final Rectangle bounds = hovered.getBoundingRectangle();
            g.setColor(Color.MAGENTA);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        }

        @Override
        public int compare(WidgetChild o1, WidgetChild o2) {
            final int diff = getArea(o1) - getArea(o2);
            return diff > 0 ? 1 : diff < 0 ? -1 : 0;
        }
    }*/
}
