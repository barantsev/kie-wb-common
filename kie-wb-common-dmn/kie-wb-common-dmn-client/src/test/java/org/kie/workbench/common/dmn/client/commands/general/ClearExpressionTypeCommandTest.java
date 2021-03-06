/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.dmn.client.commands.general;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.v1_1.Expression;
import org.kie.workbench.common.dmn.client.commands.VetoExecutionCommand;
import org.kie.workbench.common.dmn.client.commands.VetoUndoCommand;
import org.kie.workbench.common.dmn.client.widgets.grid.model.BaseUIModelMapper;
import org.kie.workbench.common.dmn.client.widgets.grid.model.GridCellTuple;
import org.kie.workbench.common.dmn.client.widgets.layer.DMNGridLayer;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandResultBuilder;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandResultBuilder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.wires.core.grids.client.model.GridCell;
import org.uberfire.ext.wires.core.grids.client.model.GridCellValue;
import org.uberfire.ext.wires.core.grids.client.model.GridData;
import org.uberfire.ext.wires.core.grids.client.widget.grid.GridWidget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClearExpressionTypeCommandTest {

    private static final int ROW_INDEX = 0;

    private static final int COLUMN_INDEX = 1;

    @Mock
    private DMNGridLayer gridLayer;

    @Mock
    private GridWidget gridWidget;

    @Mock
    private GridData gridData;

    @Mock
    private GridCell gridCell;

    @Mock
    private GridCellValue gridCellValue;

    @Mock
    private AbstractCanvasHandler canvasHandler;

    @Mock
    private GraphCommandExecutionContext graphCommandExecutionContext;

    @Mock
    private HasExpression hasExpression;

    @Mock
    private Expression expression;

    @Mock
    private BaseUIModelMapper<?> uiModelMapper;

    private ClearExpressionTypeCommand command;

    @SuppressWarnings("unchecked")
    private void makeCommand() {
        when(gridWidget.getModel()).thenReturn(gridData);
        when(gridData.getCell(eq(ROW_INDEX), eq(COLUMN_INDEX))).thenReturn(gridCell);
        when(gridCell.getValue()).thenReturn(gridCellValue);

        this.command = new ClearExpressionTypeCommand(new GridCellTuple(ROW_INDEX,
                                                                        COLUMN_INDEX,
                                                                        gridWidget),
                                                      hasExpression,
                                                      uiModelMapper,
                                                      gridLayer::batch);
    }

    @Test
    public void checkGraphCommand() {
        makeCommand();

        assertEquals(GraphCommandResultBuilder.SUCCESS,
                     command.getGraphCommand(canvasHandler).allow(graphCommandExecutionContext));

        verifyZeroInteractions(uiModelMapper);
    }

    @Test
    public void executeGraphCommand() {
        makeCommand();

        assertEquals(GraphCommandResultBuilder.SUCCESS,
                     command.getGraphCommand(canvasHandler).execute(graphCommandExecutionContext));

        verify(hasExpression).setExpression(eq(null));

        verifyZeroInteractions(uiModelMapper);
    }

    @Test
    public void undoGraphCommand() {
        when(hasExpression.getExpression()).thenReturn(expression);

        makeCommand();

        //Execute then undo
        assertEquals(GraphCommandResultBuilder.SUCCESS,
                     command.getGraphCommand(canvasHandler).execute(graphCommandExecutionContext));
        verify(hasExpression).setExpression(eq(null));

        assertEquals(GraphCommandResultBuilder.SUCCESS,
                     command.getGraphCommand(canvasHandler).undo(graphCommandExecutionContext));
        verify(hasExpression).setExpression(eq(expression));

        verifyZeroInteractions(uiModelMapper);
    }

    @Test
    public void allowCanvasCommand() {
        makeCommand();

        assertEquals(CanvasCommandResultBuilder.SUCCESS,
                     command.getCanvasCommand(canvasHandler).allow(canvasHandler));

        verifyZeroInteractions(uiModelMapper);
    }

    @Test
    public void executeCanvasCommand() {
        makeCommand();

        assertEquals(CanvasCommandResultBuilder.SUCCESS,
                     command.getCanvasCommand(canvasHandler).execute(canvasHandler));

        verify(uiModelMapper).fromDMNModel(eq(ROW_INDEX),
                                           eq(COLUMN_INDEX));

        verify(gridLayer).batch();
    }

    @Test
    public void undoCanvasCommand() {
        makeCommand();

        assertEquals(CanvasCommandResultBuilder.SUCCESS,
                     command.getCanvasCommand(canvasHandler).undo(canvasHandler));

        verify(gridData).setCellValue(eq(ROW_INDEX),
                                      eq(COLUMN_INDEX),
                                      eq(gridCellValue));

        verify(gridLayer).batch();
    }

    @Test
    public void checkCommandDefinition() {
        makeCommand();

        assertTrue(command instanceof VetoExecutionCommand);
        assertTrue(command instanceof VetoUndoCommand);
    }
}
