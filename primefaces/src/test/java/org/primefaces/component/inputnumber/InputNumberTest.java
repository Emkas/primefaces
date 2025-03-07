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
package org.primefaces.component.inputnumber;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class InputNumberTest {

    private InputNumberRenderer renderer;
    private FacesContext context;
    private ExternalContext externalContext;
    private ELContext elContext;
    private InputNumber inputNumber;
    private ValueExpression valueExpression;

    @BeforeEach
    void setup() {
        renderer = spy(new InputNumberRenderer());
        context = mock(FacesContext.class);
        externalContext = mock(ExternalContext.class);
        elContext = mock(ELContext.class);
        when(context.getExternalContext()).thenReturn(externalContext);
        when(context.getELContext()).thenReturn(elContext);
        inputNumber = mock(InputNumber.class);
        when(inputNumber.getClientId(context)).thenReturn("");
        when(inputNumber.isValid()).thenReturn(true);
        when(inputNumber.getSubmittedValue()).thenCallRealMethod();
        doCallRealMethod().when(inputNumber).setSubmittedValue(anyString());
    }

    @AfterEach
    void teardown() {
        valueExpression = null;
        inputNumber = null;
        elContext = null;
        externalContext = null;
        context = null;
        renderer = null;
    }

    private void setupValues(String submittedValue, boolean disabled, String minValue, String maxValue, boolean primitiveValueBinding) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("_hinput", submittedValue);
        when(externalContext.getRequestParameterMap()).thenReturn(requestParams);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("readonly", disabled);
        attributes.put("disabled", disabled);
        when(inputNumber.getAttributes()).thenReturn(attributes);
        when(inputNumber.isDisabled()).thenReturn(disabled);
        when(inputNumber.isReadonly()).thenReturn(disabled);
        when(inputNumber.getMinValue()).thenReturn(minValue);
        when(inputNumber.getMaxValue()).thenReturn(maxValue);
        valueExpression = mock(ValueExpression.class);
        when(inputNumber.getValueExpression(anyString())).thenReturn(valueExpression);
        if (primitiveValueBinding) {
            when(valueExpression.getType(elContext)).thenReturn((Class) double.class);
        }
    }

    @Test
    void decodeNegativeWithinDefaultRange() {
        setupValues("-999999999.99", false, null, null, false);
        renderer.decode(context, inputNumber);
        assertEquals("-999999999.99", inputNumber.getSubmittedValue());
    }

    @Test
    void decodeZeroWithinDefaultRange() {
        setupValues("0", false, null, null, false);
        renderer.decode(context, inputNumber);
        assertEquals("0", inputNumber.getSubmittedValue());
    }

    @Test
    void decodePositiveWithinDefaultRange() {
        setupValues("999999999.99", false, null, null, false);
        renderer.decode(context, inputNumber);
        assertEquals("999999999.99", inputNumber.getSubmittedValue());
    }

    @Test
    void decodeWithinRange() {
        setupValues("3.14", false, "-1", "5", false);
        renderer.decode(context, inputNumber);
        assertEquals("3.14", inputNumber.getSubmittedValue());
    }

    @Test
    void decodeBelowRange() {

        setupValues("-1", false, "0.0", null, false);
        renderer.decode(context, inputNumber);
        assertEquals("0.0", inputNumber.getSubmittedValue());
    }

    @Test
    void decodeAboveRange() {
        setupValues("2", false, "0.0", "1.0", false);
        renderer.decode(context, inputNumber);
        assertEquals("1.0", inputNumber.getSubmittedValue());
    }

    @Test
    void decodeNegativeBelowDefaultRange() {
        setupValues("-90000000000000", false, null, null, false);
        renderer.decode(context, inputNumber);
        assertEquals("-10000000000000", inputNumber.getSubmittedValue());
    }

    @Test
    void decodePositiveAboveDefaultRange() {
        setupValues("90000000000000", false, null, null, false);
        renderer.decode(context, inputNumber);
        assertEquals("10000000000000", inputNumber.getSubmittedValue());
    }

    @Test
    void decodeInvalidNumber() {
        setupValues("crash", false, null, null, false);

        // Act
        FacesException thrown = assertThrows(FacesException.class, () -> {
            renderer.decode(context, inputNumber);
        });

        // Assert (expected exception)
        assertEquals("Invalid number", thrown.getMessage());

    }

    @Test
    void decodeEmptyNonPrimitive() {
        setupValues("", false, "1", "10", false);
        renderer.decode(context, inputNumber);
        assertEquals("", inputNumber.getSubmittedValue());
    }

    @Test
    void decodeEmptyPrimitive() {
        setupValues("", false, "1", "10", true);
        renderer.decode(context, inputNumber);
        double submittedValue = Double.parseDouble(inputNumber.getSubmittedValue().toString());
        assertTrue(submittedValue >= 1 && submittedValue <= 10);
    }

    @Test
    void decodeEmptyPrimitiveWithoutMinValue() {
        setupValues("", false, null, "10.0", true);
        renderer.decode(context, inputNumber);
        double submittedValue = Double.parseDouble(inputNumber.getSubmittedValue().toString());
        assertEquals("10.0", String.valueOf(submittedValue));
    }

    @Test
    void decodeDisabled() {
        setupValues("1", true, "0", "2", false);
        renderer.decode(context, inputNumber);
        assertNull(inputNumber.getSubmittedValue());
    }

    @Test
    void isIntegral() {
        // values
        doReturn(BigDecimal.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertFalse(renderer.isIntegral(context, inputNumber, BigDecimal.valueOf(5.5)));
        doReturn(Number.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertFalse(renderer.isIntegral(context, inputNumber, BigDecimal.valueOf(5.5)));
        doReturn(Number.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertTrue(renderer.isIntegral(context, inputNumber, Integer.valueOf(3)));

        // nulls
        doReturn(Integer.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertTrue(renderer.isIntegral(context, inputNumber, null));
        doReturn(Short.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertTrue(renderer.isIntegral(context, inputNumber, null));
        doReturn(Byte.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertTrue(renderer.isIntegral(context, inputNumber, null));
        doReturn(BigInteger.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertTrue(renderer.isIntegral(context, inputNumber, null));
        doReturn(Long.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertTrue(renderer.isIntegral(context, inputNumber, null));
        doReturn(BigDecimal.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertFalse(renderer.isIntegral(context, inputNumber, null));
        // GitHub #11791
        doReturn(Number.class).when(renderer).getTypeFromValueExpression(context, inputNumber);
        assertFalse(renderer.isIntegral(context, inputNumber, null));
    }

}
