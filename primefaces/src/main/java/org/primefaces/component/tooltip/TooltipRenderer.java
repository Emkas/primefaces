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
package org.primefaces.component.tooltip;

import org.primefaces.expression.SearchExpressionUtils;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class TooltipRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        Tooltip tooltip = (Tooltip) component;
        String target = SearchExpressionUtils.resolveClientIdsForClientSide(
                context, component, tooltip.getFor());

        encodeMarkup(context, tooltip, target);
        encodeScript(context, tooltip, target);
    }

    protected void encodeMarkup(FacesContext context, Tooltip tooltip, String target) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (target != null) {
            String styleClass = tooltip.getStyleClass();
            styleClass = styleClass == null ? Tooltip.CONTAINER_CLASS : Tooltip.CONTAINER_CLASS + " " + styleClass;
            styleClass = styleClass + " ui-tooltip-" + tooltip.getPosition();

            writer.startElement("div", tooltip);
            writer.writeAttribute("id", tooltip.getClientId(context), null);
            writer.writeAttribute("class", styleClass, "styleClass");
            writer.writeAttribute("role", "tooltip", null);

            if (tooltip.getStyle() != null) {
                writer.writeAttribute("style", tooltip.getStyle(), "style");
            }

            writer.startElement("div", tooltip);
            writer.writeAttribute("class", "ui-tooltip-arrow", null);
            writer.endElement("div");

            writer.startElement("div", tooltip);
            writer.writeAttribute("class", "ui-tooltip-text ui-shadow", null);

            if (tooltip.getChildCount() > 0) {
                renderChildren(context, tooltip);
            }
            else {
                String valueToRender = ComponentUtils.getValueToRender(context, tooltip);
                if (valueToRender != null) {
                    if (tooltip.isEscape()) {
                        writer.writeText(valueToRender, "value");
                    }
                    else {
                        writer.write(valueToRender);
                    }
                }
            }

            writer.endElement("div");

            writer.endElement("div");
        }
    }

    protected void encodeScript(FacesContext context, Tooltip tooltip, String target) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("Tooltip", tooltip)
                .attr("showEvent", tooltip.getShowEvent(), null)
                .attr("hideEvent", tooltip.getHideEvent(), null)
                .attr("showEffect", tooltip.getShowEffect(), null)
                .attr("hideEffect", tooltip.getHideEffect(), null)
                .attr("showDelay", tooltip.getShowDelay(), 150)
                .attr("hideDelay", tooltip.getHideDelay(), 0)
                .attr("target", target, null)
                .attr("globalSelector", tooltip.getGlobalSelector(), null)
                .attr("escape", tooltip.isEscape(), true)
                .attr("trackMouse", tooltip.isTrackMouse(), false)
                .attr("position", tooltip.getPosition(), "right")
                .attr("myPos", tooltip.getMy(), null)
                .attr("atPos", tooltip.getAt(), null)
                .attr("delegate", tooltip.isDelegate(), false)
                .attr("styleClass", tooltip.getStyleClass(), null)
                .attr("autoHide", tooltip.isAutoHide(), true)
                .returnCallback("beforeShow", "function()", tooltip.getBeforeShow())
                .callback("onShow", "function()", tooltip.getOnShow())
                .callback("onHide", "function()", tooltip.getOnHide());

        wb.finish();
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Rendering happens on encodeEnd
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
