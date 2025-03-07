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
package org.primefaces.component.photocam;

import org.primefaces.event.CaptureEvent;
import org.primefaces.expression.SearchExpressionUtils;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.WidgetBuilder;

import java.io.IOException;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.PhaseId;
import jakarta.xml.bind.DatatypeConverter;

public class PhotoCamRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        PhotoCam cam = (PhotoCam) component;
        String dataParam = cam.getClientId(context) + "_data";
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        if (params.containsKey(dataParam)) {
            String image = params.get(dataParam);
            image = image.substring(22);

            CaptureEvent event = new CaptureEvent(cam, DatatypeConverter.parseBase64Binary(image), image);
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);

            cam.queueEvent(event);
        }
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        PhotoCam cam = (PhotoCam) component;

        encodeMarkup(context, cam);
        encodeScript(context, cam);
    }

    protected void encodeMarkup(FacesContext context, PhotoCam cam) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = cam.getClientId(context);
        String style = cam.getStyle();
        String styleClass = cam.getStyleClass();

        writer.startElement("div", null);
        writer.writeAttribute("id", clientId, null);
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, null);
        }

        writer.endElement("div");
    }

    protected void encodeScript(FacesContext context, PhotoCam cam) throws IOException {

        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("PhotoCam", cam)
                .attr("width", cam.getWidth(), 320)
                .attr("height", cam.getHeight(), 240)
                .attr("photoWidth", cam.getPhotoWidth(), 320)
                .attr("photoHeight", cam.getPhotoHeight(), 240)
                .attr("format", cam.getFormat(), null)
                .attr("jpegQuality", cam.getJpegQuality(), 90)
                .attr("autoStart", cam.isAutoStart(), true)
                .attr("device", cam.getDevice(), null)
                .callback("onCameraError", "function(errorObj)", cam.getOnCameraError());

        if (cam.getUpdate() != null) {
            wb.attr("update", SearchExpressionUtils.resolveClientIdsForClientSide(context, cam, cam.getUpdate()));
        }
        if (cam.getProcess() != null) {
            wb.attr("process", SearchExpressionUtils.resolveClientIdsForClientSide(context, cam, cam.getProcess()));
        }

        wb.finish();
    }

}
