// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.commons.ui.swt.advanced.dataeditor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.macrotable.ExtendedTableModel;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.swt.tableviewer.selection.SelectionHelper;
import org.talend.commons.utils.data.list.ListenableListEvent;

/**
 * This class is an abstraction of a group formed by a Label in top, a Table and a Toolbar at bottom. <br/>
 * 
 * $Id$
 * 
 * @param <B> Type of beans
 */
public abstract class AbstractDataTableEditorView<B> {

    private boolean readOnly;

    private Label titleLabel;

    private boolean executeSelectionEvent = true;

    protected boolean forceExecuteSelectionEvent;

    private Composite mainComposite;

    private int mainCompositeStyle;

    private Composite parentComposite;

    private AbstractExtendedTableViewer<B> extendedTableViewer;

    private ExtendedTableModel<B> extendedTableModel;

    private AbstractExtendedToolbar abstractExtendedToolbar;

    private boolean toolbarVisible = true;

    // private IExtendedModelListener modelNameListener = new IExtendedModelListener() {
    //
    // public void handleEvent(ExtendedModelEvent event) {
    // if (AbstractExtendedControlModel.NAME_CHANGED.equals(event.getType())) {
    // titleLabel.setText(extendedTableModel.getName());
    // }
    // }
    //
    // };
    //
    /**
     * 
     * This constructor init graphics components, then load model.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param extendedTableModel
     */
    public AbstractDataTableEditorView(Composite parentComposite, int mainCompositeStyle, ExtendedTableModel<B> extendedTableModel,
            boolean readOnly, boolean toolbarVisible) {
        super();
        this.parentComposite = parentComposite;
        this.mainCompositeStyle = mainCompositeStyle;
        this.extendedTableModel = extendedTableModel;
        this.readOnly = readOnly;
        this.toolbarVisible = toolbarVisible;
        initGraphicComponents();
        setExtendedTableModel(extendedTableModel);
    }

    /**
     * 
     * This constructor init graphics components, then load model.
     * 
     * Table data will be writable and toolbar will be visible.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param extendedTableModel
     */
    public AbstractDataTableEditorView(Composite parentComposite, int mainCompositeStyle, ExtendedTableModel<B> extendedTableModel) {
        this(parentComposite, mainCompositeStyle, extendedTableModel, false, true);
    }

    /**
     * This constructor doesn't initialize graphics components and model.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     */
    public AbstractDataTableEditorView(Composite parentComposite, int mainCompositeStyle) {
        this.parentComposite = parentComposite;
        this.mainCompositeStyle = mainCompositeStyle;
    }

    public void initGraphicComponents() {

        mainComposite = new Composite(parentComposite, mainCompositeStyle);
        GridLayout layout = new GridLayout();
        mainComposite.setLayout(layout);

        titleLabel = new Label(mainComposite, SWT.NONE);
        titleLabel.setText("");
        titleLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        initTable();

        if (toolbarVisible) {
            this.abstractExtendedToolbar = initToolBar();
        }

        addListeners();
    }

    /**
     * DOC amaumont Comment method "initToolBar".
     */
    protected AbstractExtendedToolbar initToolBar() {
        return null;
    }

    /**
     * DOC amaumont Comment method "initTable".
     */
    protected void initTable() {
        this.extendedTableViewer = new AbstractExtendedTableViewer<B>(this.extendedTableModel, mainComposite) {

            @Override
            protected void createColumns(TableViewerCreator<B> tableViewerCreator, Table table) {
                AbstractDataTableEditorView.this.createColumns(tableViewerCreator, table);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#setTableViewerCreatorOptions(org.talend.commons.ui.swt.tableviewer.TableViewerCreator)
             */
            @Override
            protected void setTableViewerCreatorOptions(TableViewerCreator<B> newTableViewerCreator) {
                super.setTableViewerCreatorOptions(newTableViewerCreator);
                AbstractDataTableEditorView.this.setTableViewerCreatorOptions(newTableViewerCreator);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#handleBeforeListenableListOperationEvent(org.talend.commons.utils.data.list.ListenableListEvent)
             */
            @Override
            protected void handleBeforeListenableListOperationEvent(ListenableListEvent event) {
                AbstractDataTableEditorView.this.handleBeforeListenableListOperationEvent(event);
                super.handleBeforeListenableListOperationEvent(event);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#handleAfterListenableListOperationEvent(org.talend.commons.utils.data.list.ListenableListEvent)
             */
            @Override
            protected void handleAfterListenableListOperationEvent(ListenableListEvent event) {
                super.handleAfterListenableListOperationEvent(event);
                AbstractDataTableEditorView.this.handleAfterListenableListOperationEvent(event);
            }

        };
    }

    /**
     * DOC amaumont Comment method "handleBeforeListenableListOperationEvent".
     * 
     * @param event
     */
    protected void handleBeforeListenableListOperationEvent(ListenableListEvent event) {

    }

    /**
     * DOC amaumont Comment method "handleAfterListenableListOperationEvent".
     * 
     * @param event
     */
    protected void handleAfterListenableListOperationEvent(ListenableListEvent event) {

    }

    /**
     * DOC amaumont Comment method "setTableViewerCreatorOptions".
     * 
     * @param newTableViewerCreator
     */
    protected void setTableViewerCreatorOptions(TableViewerCreator<B> newTableViewerCreator) {
    }

    /**
     * DOC amaumont Comment method "addListeners".
     */
    protected void addListeners() {
        initLineSelectionListeners();
    }

    /**
     * DOC amaumont Comment method "initLineSelectionListeners".
     * 
     * @return
     */
    protected void initLineSelectionListeners() {
        final SelectionHelper selectionHelper = getTableViewerCreator().getSelectionHelper();
        final Table table = getTableViewerCreator().getTable();
        final ILineSelectionListener beforeLineSelectionListener = new ILineSelectionListener() {

            public void handle(LineSelectionEvent e) {
                if (e.selectionByMethod && !selectionHelper.isMouseSelectionning() && !forceExecuteSelectionEvent) {
                    executeSelectionEvent = false;
                } else {
                    executeSelectionEvent = true;
                }
            }
        };
        final ILineSelectionListener afterLineSelectionListener = new ILineSelectionListener() {

            public void handle(LineSelectionEvent e) {
                executeSelectionEvent = true;
            }
        };
        selectionHelper.addBeforeSelectionListener(beforeLineSelectionListener);
        selectionHelper.addAfterSelectionListener(afterLineSelectionListener);

        DisposeListener disposeListener = new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                selectionHelper.removeBeforeSelectionListener(beforeLineSelectionListener);
                selectionHelper.removeAfterSelectionListener(afterLineSelectionListener);
                table.removeDisposeListener(this);
            }
        };
        table.addDisposeListener(disposeListener);

        table.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                if (event.character == '\u0001') { // CTRL + A
                    forceExecuteSelectionEvent = true;
                    selectionHelper.selectAll();
                    forceExecuteSelectionEvent = false;
                }
            }

        });
    }

    /**
     * DOC amaumont Comment method "setTableSelection".
     * 
     * @param selectionIndices
     */
    public void setTableSelection(int[] selectionIndices, boolean executeSelectionEvent) {
        this.executeSelectionEvent = executeSelectionEvent;
        getTableViewerCreator().getTable().setSelection(selectionIndices);
        this.executeSelectionEvent = true;

    }

    public boolean isExecuteSelectionEvent() {
        return this.executeSelectionEvent;
    }

    public void setExecuteSelectionEvent(boolean executeSelectionEvent) {
        this.executeSelectionEvent = executeSelectionEvent;
    }

    /**
     * DOC ocarbone Comment method "setGridDataSize".
     * 
     * @param minimumWidth
     * @param minimumHeight
     */
    public void setGridDataSize(final int minimumWidth, final int minimumHeight) {
        mainComposite.setSize(minimumWidth, minimumHeight);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = minimumWidth;
        gridData.minimumHeight = minimumHeight;
        mainComposite.setLayoutData(gridData);

    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (abstractExtendedToolbar != null) {
            abstractExtendedToolbar.setReadOnly(readOnly);
        }
        
        TableViewerCreator<B> tableViewerCreator = extendedTableViewer.getTableViewerCreator();

        List<TableViewerCreatorColumn> columns = tableViewerCreator.getColumns();
        for (TableViewerCreatorColumn column : columns) {
            column.setModifiable(!readOnly);
        }
    }

    /**
     * Getter for composite.
     * 
     * @return the composite
     */
    public Composite getMainComposite() {
        return this.mainComposite;
    }

    protected abstract void createColumns(TableViewerCreator<B> tableViewerCreator, Table table);

    /**
     * @return
     * @see org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#getExtendedTableModel()
     */
    public TableViewerCreator<B> getTableViewerCreator() {
        if (this.extendedTableViewer == null) {
            return null;
        }
        return this.extendedTableViewer.getTableViewerCreator();
    }

    /**
     * Getter for extendedTableModel.
     * 
     * @return the extendedTableModel
     */
    public ExtendedTableModel<B> getExtendedTableModel() {
        if (extendedTableViewer == null) {
            return null;
        }
        return extendedTableViewer.getExtendedTableModel();
    }

    /**
     * Sets the extendedTableModel.
     * 
     * @param extendedTableModel the extendedTableModel to set
     */
    public void setExtendedTableModel(ExtendedTableModel<B> extendedTableModel) {
        extendedTableViewer.setExtendedControlModel(extendedTableModel);
        if (extendedTableModel != null) {
            titleLabel.setText(extendedTableModel.getName() == null ? "" : extendedTableModel.getName());
        }

        // this.extendedTableModel = extendedTableModel;
        // if (extendedTableModel == null) {
        // nameLabel.setText("");
        // executeSelectionEvent = false;
        // getTableViewerCreator().init(new ArrayList());
        // executeSelectionEvent = true;
        // // tableViewerCreator.layout();
        // } else {
        // String text = extendedTableModel.getName();
        // nameLabel.setText(text == null ? "" : text);
        // executeSelectionEvent = false;
        //
        // getTableViewerCreator().init(extendedTableModel.getBeansList());
        // executeSelectionEvent = true;
        // // tableViewerCreator.layout();
        // }
    }

    /**
     * Getter for extendedTableViewer.
     * 
     * @return the extendedTableViewer
     */
    public AbstractExtendedTableViewer<B> getExtendedTableViewer() {
        return this.extendedTableViewer;
    }

    /**
     * Getter for abstractExtendedToolbar.
     * 
     * @return the abstractExtendedToolbar
     */
    public AbstractExtendedToolbar getAbstractExtendedToolbar() {
        return this.abstractExtendedToolbar;
    }

    /**
     * Getter for readOnly.
     * 
     * @return the readOnly
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }


}
