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
package org.primefaces.component.subtable;

import org.primefaces.component.api.UIColumn;
import org.primefaces.component.column.Column;
import org.primefaces.component.columngroup.ColumnGroup;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.row.Row;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.FacetUtils;
import org.primefaces.util.LangUtils;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

public class SubTableRenderer extends CoreRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        SubTable table = (SubTable) component;
        int rowCount = table.getRowCount();

        encodeHeader(context, table);

        for (int i = 0; i < rowCount; i++) {
            encodeRow(context, table, i);
        }

        encodeFooter(context, table);
    }

    public void encodeHeader(FacesContext context, SubTable table) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent header = table.getFacet("header");

        if (FacetUtils.shouldRenderFacet(header)) {
            writer.startElement("tr", null);
            writer.writeAttribute("class", "ui-widget-header", null);

            writer.startElement("td", null);
            writer.writeAttribute("class", DataTable.SUBTABLE_HEADER, null);
            writer.writeAttribute("colspan", table.getColumns().size(), null);

            header.encodeAll(context);

            writer.endElement("td");
            writer.endElement("tr");
        }

        ColumnGroup group = table.getColumnGroup("header");
        if (group != null && group.isRendered()) {
            for (UIComponent child : group.getChildren()) {
                if (child.isRendered() && child instanceof Row) {
                    Row headerRow = (Row) child;
                    String styleClass = "ui-widget-header";
                    if (LangUtils.isNotBlank(headerRow.getStyleClass())) {
                        styleClass = styleClass + " " + headerRow.getStyleClass();
                    }

                    writer.startElement("tr", null);
                    writer.writeAttribute("class", styleClass, null);
                    if (LangUtils.isNotBlank(headerRow.getStyle())) {
                        writer.writeAttribute("style", headerRow.getStyle(), null);
                    }

                    for (UIComponent headerRowChild : headerRow.getChildren()) {
                        if (headerRowChild.isRendered() && headerRowChild instanceof Column) {
                            Column footerColumn = (Column) headerRowChild;
                            encodeFacetColumn(context, table, footerColumn, "header", DataTable.COLUMN_HEADER_CLASS, footerColumn.getHeaderText());
                        }
                    }

                    writer.endElement("tr");
                }
            }
        }
    }

    public void encodeRow(FacesContext context, SubTable table, int rowIndex) throws IOException {
        table.setRowIndex(rowIndex);
        if (!table.isRowAvailable()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String clientId = table.getClientId(context);

        writer.startElement("tr", null);
        writer.writeAttribute("id", clientId + "_row_" + rowIndex, null);
        writer.writeAttribute("class", DataTable.ROW_CLASS, null);

        for (UIColumn column : table.getColumns()) {
            if (column.isRendered() && column instanceof Column) { //Columns are not supported yet
                String style = column.getStyle();
                String styleClass = column.getStyleClass();
                int colspan = column.getColspan();
                int rowspan = column.getRowspan();

                writer.startElement("td", null);
                if (style != null) {
                    writer.writeAttribute("style", style, null);
                }
                if (styleClass != null) {
                    writer.writeAttribute("class", styleClass, null);
                }
                if (colspan != 1) {
                    writer.writeAttribute("colspan", colspan, null);
                }
                if (rowspan != 1) {
                    writer.writeAttribute("rowspan", rowspan, null);
                }

                column.encodeAll(context);

                writer.endElement("td");
            }
        }

        writer.endElement("tr");
    }

    public void encodeFooter(FacesContext context, SubTable table) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIComponent footer = table.getFacet("footer");

        if (FacetUtils.shouldRenderFacet(footer)) {
            writer.startElement("tr", null);
            writer.writeAttribute("class", "ui-widget-header", null);

            writer.startElement("td", null);
            writer.writeAttribute("class", DataTable.SUBTABLE_FOOTER, null);
            writer.writeAttribute("colspan", table.getColumns().size(), null);

            footer.encodeAll(context);

            writer.endElement("td");
            writer.endElement("tr");
        }

        ColumnGroup group = table.getColumnGroup("footer");

        if (group == null || !group.isRendered()) {
            return;
        }

        for (UIComponent child : group.getChildren()) {
            if (child.isRendered() && child instanceof Row) {
                Row footerRow = (Row) child;
                String styleClass = "ui-widget-header";
                if (LangUtils.isNotBlank(footerRow.getStyleClass())) {
                    styleClass = styleClass + " " + footerRow.getStyleClass();
                }

                writer.startElement("tr", null);
                writer.writeAttribute("class", styleClass, null);
                if (LangUtils.isNotBlank(footerRow.getStyle())) {
                    writer.writeAttribute("style", footerRow.getStyle(), null);
                }

                for (UIComponent footerRowChild : footerRow.getChildren()) {
                    if (footerRowChild.isRendered() && footerRowChild instanceof Column) {
                        Column footerColumn = (Column) footerRowChild;
                        encodeFacetColumn(context, table, footerColumn, "footer", DataTable.COLUMN_FOOTER_CLASS, footerColumn.getFooterText());
                    }
                }

                writer.endElement("tr");
            }
        }
    }

    protected void encodeFacetColumn(FacesContext context, SubTable table, Column column, String facetName, String styleClass, String text) throws IOException {
        if (!column.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        String style = column.getStyle();
        String columnClass = column.getStyleClass();
        columnClass = (columnClass == null) ? styleClass : styleClass + " " + columnClass;

        writer.startElement("td", null);
        writer.writeAttribute("class", columnClass, null);
        if (column.getRowspan() != 1) {
            writer.writeAttribute("rowspan", column.getRowspan(), null);
        }
        if (column.getColspan() != 1) {
            writer.writeAttribute("colspan", column.getColspan(), null);
        }
        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        // Footer content
        UIComponent facet = column.getFacet(facetName);
        if (FacetUtils.shouldRenderFacet(facet)) {
            facet.encodeAll(context);
        }
        else if (text != null) {
            writer.write(text);
        }

        writer.endElement("td");
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        // Rendering happens on encodeEnd
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
