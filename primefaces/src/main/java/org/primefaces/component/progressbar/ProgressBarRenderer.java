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
package org.primefaces.component.progressbar;

import org.primefaces.PrimeFaces;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class ProgressBarRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        ProgressBar progressBar = (ProgressBar) component;
        String clientId = progressBar.getClientId(context);
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        if (params.containsKey(clientId)) {
            PrimeFaces.current().ajax().addCallbackParam(progressBar.getClientId(context) + "_value", progressBar.getValue());
        }

        decodeBehaviors(context, progressBar);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ProgressBar progressBar = (ProgressBar) component;

        encodeMarkup(context, progressBar);

        if (!progressBar.isDisplayOnly()) {
            encodeScript(context, progressBar);
        }
    }

    protected void encodeMarkup(FacesContext context, ProgressBar progressBar) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String mode = progressBar.getMode();
        int value = progressBar.getValue();
        String labelTemplate = progressBar.getLabelTemplate();
        String title = progressBar.getTitle();
        String style = progressBar.getStyle();
        String severity = progressBar.getSeverity();
        String styleClass = getStyleClassBuilder(context)
                .add(ProgressBar.CONTAINER_CLASS, progressBar.getStyleClass())
                .add("determinate".equals(mode), ProgressBar.DETERMINATE_CLASS, ProgressBar.INDETERMINATE_CLASS)
                .add(progressBar.isDisabled(), "ui-state-disabled")
                .add("info".equals(severity), ProgressBar.SEVERITY_INFO_CLASS)
                .add("success".equals(severity), ProgressBar.SEVERITY_SUCCESS_CLASS)
                .add("warning".equals(severity), ProgressBar.SEVERITY_WARNING_CLASS)
                .add("danger".equals(severity), ProgressBar.SEVERITY_DANGER_CLASS)
                .build();

        writer.startElement("div", progressBar);
        writer.writeAttribute("id", progressBar.getClientId(context), "id");
        writer.writeAttribute("class", styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute("style", style, "style");
        }
        if (title != null) {
            writer.writeAttribute("title", title, null);
        }

        //value
        writer.startElement("div", progressBar);
        writer.writeAttribute("class", ProgressBar.VALUE_CLASS, null);
        if (value != 0) {
            writer.writeAttribute("style", "display:block;width:" + value + "%", style);
        }
        //label
        writer.startElement("div", progressBar);
        writer.writeAttribute("class", ProgressBar.LABEL_CLASS, null);
        if (labelTemplate != null) {
            writer.writeAttribute("style", "display:block", style);
            writer.writeText(labelTemplate.replace("{value}", String.valueOf(value)), null);
        }
        writer.endElement("div"); // label end

        writer.endElement("div"); // value end


        writer.endElement("div");
    }

    protected void encodeScript(FacesContext context, ProgressBar progressBar) throws IOException {
        boolean isAjax = progressBar.isAjax();

        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("ProgressBar", progressBar)
                .attr("initialValue", progressBar.getValue())
                .attr("ajax", isAjax)
                .attr("labelTemplate", progressBar.getLabelTemplate(), null)
                .attr("animationDuration", progressBar.getAnimationDuration())
                .attr("global", progressBar.isGlobal(), true);

        if (isAjax) {
            wb.attr("interval", progressBar.getInterval());

            encodeClientBehaviors(context, progressBar);
        }

        wb.finish();
    }
}
