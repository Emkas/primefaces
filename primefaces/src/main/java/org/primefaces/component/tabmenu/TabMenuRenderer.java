/*
 * The MIT License
 *
 * Copyright (c) 2009-2025 PrimeTek Informatics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primefaces.component.tabmenu;

import org.primefaces.component.badge.BadgeRenderer;
import org.primefaces.component.menu.AbstractMenu;
import org.primefaces.component.menu.BaseMenuRenderer;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.util.HTML;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;
import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class TabMenuRenderer extends BaseMenuRenderer {

    @Override
    protected void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException {
        TabMenu menu = (TabMenu) abstractMenu;
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("TabMenu", menu);
        wb.finish();
    }

    @Override
    protected void encodeMarkup(FacesContext context, AbstractMenu component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        TabMenu menu = (TabMenu) component;
        String clientId = menu.getClientId(context);
        String styleClass = getStyleClassBuilder(context)
                .add(TabMenu.CONTAINER_CLASS)
                .add("ui-tabs-" + menu.getOrientation())
                .add(menu.getStyleClass())
                .build();
        int activeIndex = menu.getActiveIndex();
        List<?> elements = menu.getElements();

        writer.startElement("div", menu);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("class", styleClass, "styleClass");
        if (menu.getStyle() != null) {
            writer.writeAttribute("style", menu.getStyle(), "style");
        }

        writer.startElement("ul", null);
        writer.writeAttribute("class", TabMenu.NAVIGATOR_CLASS, null);
        writer.writeAttribute("role", "tablist", null);

        int i = 0;
        if (elements != null && !elements.isEmpty()) {
            for (Object element : elements) {
                if (element instanceof MenuElement) {
                    if (((MenuElement) element).isRendered() && (element instanceof MenuItem)) {
                        encodeItem(context, menu, (MenuItem) element, (i == activeIndex));
                        i++;
                    }
                }
            }
        }

        writer.endElement("ul");

        writer.endElement("div");
    }

    protected void encodeItem(FacesContext context, TabMenu menu, MenuItem item, boolean active) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String containerStyle = item.getContainerStyle();
        String containerStyleClass = getStyleClassBuilder(context)
                .add(item.getContainerStyleClass())
                .add(active, TabMenu.ACTIVE_TAB_HEADER_CLASS, TabMenu.INACTIVE_TAB_HEADER_CLASS)
                .add(item.isDisabled(), "ui-state-disabled", "ui-state-default")
                .add(item.getIcon() != null, "ui-tabmenuitem-hasicon")
                .add(item.getBadge() != null, "ui-overlay-badge")
                .build();

        //header container
        writer.startElement("li", null);
        writer.writeAttribute("class", containerStyleClass, null);
        writer.writeAttribute("role", "tab", null);
        writer.writeAttribute(HTML.ARIA_EXPANDED, String.valueOf(active), null);
        writer.writeAttribute(HTML.ARIA_SELECTED, String.valueOf(active), null);

        if (containerStyle != null) {
            writer.writeAttribute("style", containerStyle, null);
        }

        if (item.getBadge() != null) {
            BadgeRenderer.encode(context, item.getBadge());
        }

        encodeMenuItem(context, menu, item);

        writer.endElement("li");
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        // Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
