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
package org.primefaces.component.column;

import org.primefaces.component.api.UIColumn;
import org.primefaces.facelets.MethodRule;
import org.primefaces.model.SortMeta;

import java.util.Locale;

import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;

public class ColumnHandler extends ComponentHandler {

    private static final MetaRule SORT_FUNCTION = new MethodRule(Column.PropertyKeys.sortFunction.name(),
            Integer.class,
            new Class<?>[]{Object.class, Object.class, SortMeta.class});

    private static final MetaRule FILTER_FUNCTION = new MethodRule(Column.PropertyKeys.filterFunction.name(),
            Integer.class,
            new Class<?>[]{Object.class, Object.class, Locale.class});

    private static final MetaRule EXPORT_FUNCTION = new MethodRule(Column.PropertyKeys.exportFunction.name(),
            Integer.class,
            new Class<?>[]{UIColumn.class});

    public ColumnHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset metaRuleset = super.createMetaRuleset(type);

        metaRuleset.addRule(SORT_FUNCTION);
        metaRuleset.addRule(FILTER_FUNCTION);
        metaRuleset.addRule(EXPORT_FUNCTION);

        return metaRuleset;
    }
}
