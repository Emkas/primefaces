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
package org.primefaces.component.splitbutton;

import org.primefaces.component.api.MenuItemAware;
import org.primefaces.component.menu.AbstractMenu;
import org.primefaces.component.menu.Menu;
import org.primefaces.component.menubutton.MenuButton;
import org.primefaces.component.overlaypanel.OverlayPanel;
import org.primefaces.expression.SearchExpressionUtils;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.model.menu.Separator;
import org.primefaces.model.menu.Submenu;
import org.primefaces.renderkit.MenuItemAwareRenderer;
import org.primefaces.util.ComponentTraversalUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.LangUtils;
import org.primefaces.util.SharedStringBuilder;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionEvent;

public class SplitButtonRenderer extends MenuItemAwareRenderer {

    private static final String SB_BUILD_ONCLICK = SplitButtonRenderer.class.getName() + "#buildOnclick";

    @Override
    public void decode(FacesContext context, UIComponent component) {
        SplitButton button = (SplitButton) component;
        if (button.isDisabled()) {
            return;
        }

        boolean menuItemDecoded = decodeDynamicMenuItem(context, component);
        if (!menuItemDecoded) {
            String clientId = button.getClientId(context);
            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
            String param = button.isAjax() ? clientId : clientId + "_button";
            if (params.containsKey(param)) {
                component.queueEvent(new ActionEvent(component));
            }
        }

        OverlayPanel customOverlay = button.getCustomOverlay();
        if (customOverlay != null) {
            customOverlay.getRenderer().decode(context, customOverlay);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SplitButton button = (SplitButton) component;
        MenuModel model = button.getModel();
        if (model != null && button.getElementsCount() > 0) {
            model.generateUniqueIds();
        }

        encodeMarkup(context, button);
        encodeScript(context, button);
    }

    protected void encodeMarkup(FacesContext context, SplitButton button) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = button.getClientId(context);
        String menuId = clientId + "_menu";
        String menuButtonId = clientId + "_menuButton";
        String buttonId = clientId + "_button";
        String styleClass = button.getStyleClass();
        styleClass = styleClass == null ? SplitButton.STYLE_CLASS : SplitButton.STYLE_CLASS + " " + styleClass;

        boolean hasOverlay = shouldBeRendered(context, button);

        writer.startElement("div", button);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", styleClass, "id");
        writer.writeAttribute(HTML.ARIA_HASPOPUP, Boolean.toString(hasOverlay), null);
        writer.writeAttribute(HTML.ARIA_CONTROLS, menuId, null);
        writer.writeAttribute(HTML.ARIA_EXPANDED, "false", null);
        if (button.getStyle() != null) {
            writer.writeAttribute("style", button.getStyle(), "id");
        }

        encodeDefaultButton(context, button, buttonId);
        OverlayPanel customOverlay = button.getCustomOverlay();
        if (customOverlay != null || button.getElementsCount() > 0) {
            encodeMenuIcon(context, button, menuButtonId);
            if (hasOverlay) {
                encodeMenu(context, button, menuId);
            }
        }

        writer.endElement("div");

        if (customOverlay != null) {
            customOverlay.setFor(clientId);
            customOverlay.getRenderer().encodeEnd(context, customOverlay);
        }
    }

    protected void encodeDefaultButton(FacesContext context, SplitButton button, String id) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String icon = button.getIcon();
        String onclick = buildOnclick(context, button);

        // confirm dialog expects data-pfconfirmcommand attribute on div instead of button
        if (LangUtils.isNotEmpty(onclick)) {
            if (button.requiresConfirmation()) {
                writer.writeAttribute("data-pfconfirmcommand", onclick, null);
            }
        }

        writer.startElement("button", button);
        writer.writeAttribute("id", id, "id");
        writer.writeAttribute("name", id, "name");
        writer.writeAttribute("class", button.resolveStyleClass(), "styleClass");

        if (!onclick.isEmpty()) {
            if (button.requiresConfirmation()) {
                writer.writeAttribute("onclick", button.getConfirmationScript(), "onclick");
                // data-pfconfirmcommand is added to the div
            }
            else {
                writer.writeAttribute("onclick", onclick, "onclick");
            }
        }

        // GitHub #9381 ignore style as its applied to parent div
        List<String> attrs = new ArrayList<>(HTML.BUTTON_WITHOUT_CLICK_ATTRS);
        attrs.remove("style");
        renderPassThruAttributes(context, button, attrs);

        if (button.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", "disabled");
        }

        //icon
        if (!isValueBlank(icon)) {
            String defaultIconClass = button.getIconPos().equals("left") ? HTML.BUTTON_LEFT_ICON_CLASS : HTML.BUTTON_RIGHT_ICON_CLASS;
            String iconClass = defaultIconClass + " " + icon;

            writer.startElement("span", null);
            writer.writeAttribute("class", iconClass, null);
            writer.endElement("span");
        }

        //text
        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_TEXT_CLASS, null);

        renderButtonValue(writer, true, button.getValue(), button.getTitle(), button.getAriaLabel());

        writer.endElement("span");

        writer.endElement("button");
    }

    protected void encodeMenuIcon(FacesContext context, SplitButton button, String id) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String buttonClass = SplitButton.MENU_ICON_BUTTON_CLASS;
        if (button.isDisabled()) {
            buttonClass += " ui-state-disabled";
        }

        writer.startElement("button", button);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", id, null);
        writer.writeAttribute("type", "button", null);
        writer.writeAttribute("class", buttonClass, null);

        if (button.isDisabled()) {
            writer.writeAttribute("disabled", "disabled", "disabled");
        }

        //icon
        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_LEFT_ICON_CLASS + " ui-icon-triangle-1-s", null);
        writer.endElement("span");

        //text
        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_TEXT_CLASS, null);
        writer.writeText(getIconOnlyButtonText(button.getTitle(), button.getAriaLabel()), null);
        writer.endElement("span");

        writer.endElement("button");
    }

    protected void encodeScript(FacesContext context, SplitButton button) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("SplitButton", button)
            .attr("appendTo", SearchExpressionUtils.resolveOptionalClientIdForClientSide(context, button, button.getAppendTo()));

        if (button.isFilter()) {
            wb.attr("filter", true)
                    .attr("filterMatchMode", button.getFilterMatchMode(), null)
                    .nativeAttr("filterFunction", button.getFilterFunction(), null)
                    .attr("filterNormalize", button.isFilterNormalize(), false)
                    .attr("filterInputAutoFocus", button.isFilterInputAutoFocus(), true);
        }

        wb.attr("disableOnAjax", button.isDisableOnAjax(), true)
            .attr("disabledAttr", button.isDisabled(), false)
            .finish();
    }

    protected String buildOnclick(FacesContext context, SplitButton button) throws IOException {
        StringBuilder onclick = SharedStringBuilder.get(context, SB_BUILD_ONCLICK);
        if (button.getOnclick() != null) {
            onclick.append(button.getOnclick()).append(";");
        }

        if (button.isAjax()) {
            onclick.append(buildAjaxRequest(context, button));
        }
        else {
            UIForm form = ComponentTraversalUtils.closestForm(button);
            if (form == null) {
                throw new FacesException("SplitButton : \"" + button.getClientId(context) + "\" must be inside a form element");
            }

            onclick.append(buildNonAjaxRequest(context, button, form, null, false));
        }

        String onclickBehaviors = getEventBehaviors(context, button, "click", null);
        if (onclickBehaviors != null) {
            onclick.append(onclickBehaviors).append(";");
        }

        return onclick.toString();
    }

    protected void encodeMenu(FacesContext context, SplitButton button, String menuId) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String menuStyleClass = button.getMenuStyleClass();
        menuStyleClass = (menuStyleClass == null) ? SplitButton.SPLITBUTTON_CONTAINER_CLASS : SplitButton.SPLITBUTTON_CONTAINER_CLASS + " " + menuStyleClass;

        writer.startElement("div", null);
        writer.writeAttribute("id", menuId, null);
        writer.writeAttribute("class", menuStyleClass, "styleClass");
        writer.writeAttribute(HTML.ARIA_LABELLEDBY, button.getClientId(context), null);
        writer.writeAttribute("tabindex", "-1", null);

        if (button.isFilter()) {
            encodeFilter(context, button);
        }

        writer.startElement("div", null);
        writer.writeAttribute("class", SplitButton.LIST_WRAPPER_CLASS, "styleClass");

        writer.startElement("ul", null);
        writer.writeAttribute("class", MenuButton.LIST_CLASS, "styleClass");
        writer.writeAttribute(HTML.ARIA_ROLE, HTML.ARIA_ROLE_MENU, null);

        encodeElements(context, button, button.getElements(), false);

        writer.endElement("ul");
        writer.endElement("div");

        writer.endElement("div");
    }

    protected void encodeElements(FacesContext context, SplitButton button, List<MenuElement> elements, boolean isSubmenu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        for (MenuElement element : elements) {
            if (element instanceof MenuItem) {
                MenuItem menuItem = (MenuItem) element;
                if (menuItem.isRendered()) {
                    String containerStyle = menuItem.getContainerStyle();
                    String containerStyleClass = menuItem.getContainerStyleClass();
                    containerStyleClass = (containerStyleClass == null) ? Menu.MENUITEM_CLASS : Menu.MENUITEM_CLASS + " " + containerStyleClass;

                    if (isSubmenu) {
                        containerStyleClass = containerStyleClass + " " + Menu.SUBMENU_CHILD_CLASS;
                    }

                    writer.startElement("li", null);
                    writer.writeAttribute("class", containerStyleClass, null);
                    writer.writeAttribute(HTML.ARIA_ROLE, HTML.ARIA_ROLE_NONE, null);
                    if (containerStyle != null) {
                        writer.writeAttribute("style", containerStyle, null);
                    }
                    encodeMenuItem(context, button, menuItem);
                    writer.endElement("li");
                }
            }
            else if (element instanceof Submenu) {
                encodeSubmenu(context, button, (Submenu) element);
            }
            else if (element instanceof Separator) {
                encodeSeparator(context, (Separator) element);
            }
        }
    }

    protected void encodeMenuItem(FacesContext context, SplitButton button, MenuItem menuitem) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String icon = menuitem.getIcon();
        String title = menuitem.getTitle();

        if (menuitem.shouldRenderChildren()) {
            renderChildren(context, (UIComponent) menuitem);
        }
        else {
            boolean disabled = menuitem.isDisabled();

            writer.startElement("a", null);
            writer.writeAttribute("id", menuitem.getClientId(), null);
            writer.writeAttribute("tabindex", "-1", null);
            writer.writeAttribute(HTML.ARIA_ROLE, HTML.ARIA_ROLE_MENUITEM, null);
            if (title != null) {
                writer.writeAttribute("title", title, null);
            }

            String styleClass = menuitem.getStyleClass();
            styleClass = styleClass == null ? AbstractMenu.MENUITEM_LINK_CLASS : AbstractMenu.MENUITEM_LINK_CLASS + " " + styleClass;
            styleClass = disabled ? styleClass + " ui-state-disabled" : styleClass;

            writer.writeAttribute("class", styleClass, null);

            if (menuitem.getStyle() != null) {
                writer.writeAttribute("style", menuitem.getStyle(), null);
            }

            if (disabled) {
                writer.writeAttribute("href", "#", null);
                writer.writeAttribute("onclick", "return false;", null);
            }
            else {
                encodeOnClick(context, button, menuitem);
            }

            if (icon != null) {
                writer.startElement("span", null);
                writer.writeAttribute("class", AbstractMenu.MENUITEM_ICON_CLASS + " " + icon, null);
                writer.writeAttribute(HTML.ARIA_HIDDEN, "true", null);
                writer.endElement("span");
            }

            if (menuitem.getValue() != null) {
                writer.startElement("span", null);
                writer.writeAttribute("class", AbstractMenu.MENUITEM_TEXT_CLASS, null);
                writer.writeText(menuitem.getValue(), "value");
                writer.endElement("span");
            }

            writer.endElement("a");
        }
    }

    protected void encodeSubmenu(FacesContext context, SplitButton button, Submenu submenu) throws IOException {
        if (!submenu.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String label = submenu.getLabel();
        String style = submenu.getStyle();
        String styleClass = submenu.getStyleClass();
        styleClass = styleClass == null ? Menu.SUBMENU_TITLE_CLASS : Menu.SUBMENU_TITLE_CLASS + " " + styleClass;

        writer.startElement("li", null);
        writer.writeAttribute("class", styleClass, null);
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        writer.startElement("h3", null);

        if (label != null) {
            writer.writeText(label, "value");
        }

        writer.endElement("h3");

        writer.endElement("li");

        encodeElements(context, button, submenu.getElements(), true);
    }

    protected void encodeFilter(FacesContext context, SplitButton button) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String id = button.getClientId(context) + "_filter";

        writer.startElement("div", null);
        writer.writeAttribute("class", "ui-splitbuttonmenu-filter-container", null);

        writer.startElement("input", null);
        writer.writeAttribute("class", "ui-splitbuttonmenu-filter ui-inputfield ui-inputtext ui-widget ui-state-default", null);
        writer.writeAttribute("id", id, null);
        writer.writeAttribute("name", id, null);
        writer.writeAttribute("type", "text", null);
        writer.writeAttribute("autocomplete", "off", null);

        if (button.getFilterPlaceholder() != null) {
            writer.writeAttribute("placeholder", button.getFilterPlaceholder(), null);
        }

        writer.startElement("span", null);
        writer.writeAttribute("class", "ui-icon ui-icon-search", id);
        writer.endElement("span");

        writer.endElement("input");

        writer.endElement("div");
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    protected boolean shouldBeRendered(FacesContext context, MenuItemAware container) {
        SplitButton button = (SplitButton) container;
        boolean  rendered = button.getCustomOverlay() != null;
        rendered = rendered || super.shouldBeRendered(context, button);
        return rendered;
    }
}
