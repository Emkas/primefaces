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
package org.primefaces.component.idlemonitor;

import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class IdleMonitorRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        decodeBehaviors(context, component);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        IdleMonitor idleMonitor = (IdleMonitor) component;

        encodeMarkup(context, idleMonitor);
        encodeScript(context, idleMonitor);
    }

    protected void encodeScript(FacesContext context, IdleMonitor idleMonitor) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("IdleMonitor", idleMonitor)
                .attr("timeout", idleMonitor.getTimeout())
                .attr("multiWindowSupport", idleMonitor.isMultiWindowSupport())
                .callback("onidle", "function()", idleMonitor.getOnidle())
                .callback("onactive", "function()", idleMonitor.getOnactive());

        encodeClientBehaviors(context, idleMonitor);

        wb.finish();
    }

    protected void encodeMarkup(FacesContext context, IdleMonitor idleMonitor) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = idleMonitor.getClientId(context);

        writer.startElement("span", idleMonitor);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("class", "ui-idlemonitor", "styleClass");
        writer.endElement("span");
    }
}
