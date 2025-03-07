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
package org.primefaces.csp;

import org.primefaces.context.PrimePartialResponseWriter;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.util.EscapeUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialResponseWriter;
import jakarta.faces.context.ResponseWriter;

public class CspPartialResponseWriter extends PrimePartialResponseWriter {

    private CspResponseWriter cspResponseWriter;
    private PrimeRequestContext requestContext;
    private CspState cspState;
    private StringBuilder sb;

    public CspPartialResponseWriter(PartialResponseWriter wrapped, FacesContext context, CspState cspState) {
        super(wrapped);
        this.cspResponseWriter = new CspResponseWriter(wrapped, cspState);
        this.requestContext = PrimeRequestContext.getCurrentInstance(context);
        this.cspState = cspState;
    }

    @Override
    public void startInsertAfter(String targetId) throws IOException {
        cspResponseWriter.reset();
        super.startInsertAfter(targetId);
    }

    @Override
    public void startInsertBefore(String targetId) throws IOException {
        cspResponseWriter.reset();
        super.startInsertBefore(targetId);
    }

    @Override
    public void endInsert() throws IOException {
        super.endInsert();
        writeJavascriptHandlers();
    }

    @Override
    public void startUpdate(String targetId) throws IOException {
        cspResponseWriter.reset();
        super.startUpdate(targetId);
    }

    @Override
    public void endUpdate() throws IOException {
        super.endUpdate();
        writeJavascriptHandlers();
    }

    @Override
    public void startEval() throws IOException {
        cspResponseWriter.reset();
        super.startEval();
    }

    @Override
    public void endEval() throws IOException {
        cspResponseWriter.reset();
        super.endEval();
    }

    @Override
    public void startExtension(Map<String, String> attributes) throws IOException {
        cspResponseWriter.reset();
        super.startExtension(attributes);
    }

    @Override
    public void endExtension() throws IOException {
        cspResponseWriter.reset();
        super.endExtension();
    }

    @Override
    public void startError(String errorName) throws IOException {
        cspResponseWriter.reset();
        super.startError(errorName);
    }

    @Override
    public void endError() throws IOException {
        cspResponseWriter.reset();
        super.endError();
    }

    @Override
    public void startElement(String name, UIComponent component) throws IOException {
        cspResponseWriter.startElement(name, component);
    }

    @Override
    public void writeAttribute(String name, Object value, String property) throws IOException {
        cspResponseWriter.writeAttribute(name, value, property);
    }

    @Override
    public void writeURIAttribute(String name, Object value, String property) throws IOException {
        cspResponseWriter.writeURIAttribute(name, value, property);
    }

    @Override
    public void endElement(String name) throws IOException {
        cspResponseWriter.endElement(name);
    }

    @Override
    public void flush() throws IOException {
        cspResponseWriter.flush();
    }

    @Override
    public void writeComment(Object comment) throws IOException {
        cspResponseWriter.writeComment(comment);
    }

    @Override
    public void writeText(Object text, String property) throws IOException {
        cspResponseWriter.writeText(text, property);
    }

    @Override
    public void writeText(Object text, UIComponent component, String property) throws IOException {
        cspResponseWriter.writeText(text, component, property);
    }

    @Override
    public void writeText(char[] text, int off, int len) throws IOException {
        cspResponseWriter.writeText(text, off, len);
    }

    @Override
    public void close() throws IOException {
        cspResponseWriter.close();
    }

    @Override
    public ResponseWriter cloneWithWriter(Writer writer) {
        return new CspResponseWriter(getWrapped().cloneWithWriter(writer), this.cspState);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        cspResponseWriter.write(cbuf, off, len);
    }

    void writeJavascriptHandlers() throws IOException {
        if (cspState.getEventHandlers() == null || cspState.getEventHandlers().isEmpty()) {
            return;
        }

        if (sb == null) {
            sb = new StringBuilder(cspState.getEventHandlers().size() * 25);
        }
        else {
            sb.setLength(0);
        }

        for (Map.Entry<String, Map<String, String>> elements : cspState.getEventHandlers().entrySet()) {
            String id = elements.getKey();

            for (Map.Entry<String, String> events : elements.getValue().entrySet()) {
                String event = events.getKey();
                String javascript = events.getValue();

                sb.append("PrimeFaces.csp.register('");
                sb.append(EscapeUtils.forJavaScript(id));
                sb.append("','");
                sb.append(event);
                sb.append("',function(event){");
                sb.append(javascript);
                sb.append("});");
            }
        }

        // GitHub #9368 all register calls must be before ajax.executeScript calls
        ArrayList<String> scripts = (ArrayList<String>) requestContext.getScriptsToExecute();
        scripts.add(0, sb.toString());

        cspState.getEventHandlers().clear();
    }
}