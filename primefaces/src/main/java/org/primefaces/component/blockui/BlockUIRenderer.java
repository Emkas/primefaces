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
package org.primefaces.component.blockui;

import org.primefaces.expression.SearchExpressionUtils;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class BlockUIRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        BlockUI blockUI = (BlockUI) component;

        encodeMarkup(context, component);
        encodeScript(context, blockUI);
    }

    protected void encodeScript(FacesContext context, BlockUI blockUI) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("BlockUI", blockUI);

        wb.attr("block", SearchExpressionUtils.resolveClientIdsForClientSide(context, blockUI, blockUI.getBlock()))
            .attr("triggers", SearchExpressionUtils.resolveClientIdsForClientSide(context, blockUI, blockUI.getTrigger()))
            .attr("blocked", blockUI.isBlocked(), false)
            .attr("animate", blockUI.isAnimate(), true)
            .attr("styleClass", blockUI.getStyleClass(), null)
            .attr("delay", blockUI.getDelay(), 0);

        wb.finish();
    }

    protected void encodeMarkup(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        BlockUI blockUI = (BlockUI) component;
        String clientId = blockUI.getClientId(context);

        writer.startElement("div", blockUI);
        writer.writeAttribute("id", clientId, "id");
        writer.writeAttribute("class", "ui-blockui-content ui-widget ui-widget-content ui-helper-hidden ui-shadow", null);

        renderChildren(context, blockUI);

        writer.endElement("div");
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
