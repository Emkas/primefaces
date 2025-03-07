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
package org.primefaces.showcase.view.df;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("dfLevel1View")
@RequestScoped
public class DFLevel1View {

    private LocalDateTime valueFromFlash;

    @PostConstruct
    public void init() {
        valueFromFlash = (LocalDateTime) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("param1");
    }

    public void openLevel2() {
        Map<String, Object> options = new HashMap<String, Object>();
        options.put("modal", true);
        PrimeFaces.current().dialog().openDynamic("level2", options, null);
    }

    public void onReturnFromLevel2(SelectEvent event) {
        //pass back to root
        PrimeFaces.current().dialog().closeDynamic(event.getObject());
    }

    public LocalDateTime getValueFromFlash() {
        return valueFromFlash;
    }

    public void setValueFromFlash(LocalDateTime valueFromFlash) {
        this.valueFromFlash = valueFromFlash;
    }
}
