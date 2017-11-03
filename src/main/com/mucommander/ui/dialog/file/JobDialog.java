/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog.file;

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.button.CollapseExpandButton;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.layout.AsyncPanel;
import com.mucommander.ui.list.FileList;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.Container;
import java.awt.Window;

/**
 * This abstract dialog is to be sub-classed by job confirmation dialogs and provides helper methods for common
 * components.
 *
 * @author Maxence Bernard
 */
public abstract class JobDialog extends FocusDialog {

    /** Number of files displayed in the 'file details' text area */
    private final static int NB_FILE_DETAILS_ROWS = 10;

    private static boolean lastExpanded = false;

    protected MainFrame mainFrame;
    protected FileSet files;

    private CollapseExpandButton collapseExpandButton;

    JobDialog(MainFrame mainFrame, String title, FileSet files) {
        super(mainFrame, title, mainFrame);

        this.mainFrame = mainFrame;
        this.files = files;
    }


    /**
     * Displays an error dialog with the specified message and title.
     *
     * @param message the error message
     * @param title the error title
     */
    protected void showErrorDialog(String message, String title) {
        InformationDialog.showErrorDialog(mainFrame, title, message);
    }

    /**
     * Displays an error dialog with the specified message and the default error title.
     *
     * @param message the error message
     */
    protected void showErrorDialog(String message) {
        showErrorDialog(message, i18n("error"));
    }


    /**
     * Creates and returns a 'File details' panel, showing details about the files that the job will operate on. The file details
     * are loaded in a separate thread, when the panel becomes visible.
     *
     * @param packOnUpdate pack the window after list loading
     *
     * @return a 'File details' panel, showing details about the files that the job will operate on
     */
    AsyncPanel createFileDetailsPanel(boolean packOnUpdate) {
        return new AsyncPanel() {
            @Override
            public JComponent getTargetComponent(Exception e) {
                FileList fileList = new FileList(files, true);
                fileList.setVisibleRowCount(NB_FILE_DETAILS_ROWS);

                return new JScrollPane(fileList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            }

            @Override
            public void initTargetComponent() throws Exception {
            }

            @Override
            protected void updateLayout() {
                if (packOnUpdate) {
                    Container tla = getTopLevelAncestor();
                    if (tla instanceof Window) {
                        ((Window) tla).pack();
                    }
                }
            }
        };
    }

    protected AsyncPanel createFileDetailsPanel() {
        return createFileDetailsPanel(true);
    }

    /**
     * Creates and returns a button that expands/collapses the specified 'File details' panel.
     * The number of files that the job will operate on are displayed in the button's label.
     *
     * @param detailsPanel the 'File details' panel to expand/collapse
     * @return a button that expands/collapses the specified 'File details' panel
     */
    protected CollapseExpandButton createFileDetailsButton(JPanel detailsPanel) {
        collapseExpandButton = new CollapseExpandButton(i18n("nb_files", String.valueOf(files.size())), detailsPanel, lastExpanded);
        return collapseExpandButton;
    }

    /**
     * Creates a panel where the specified 'File details' button and OK/cancel control buttons are laid out on a single row,
     * with a space separation between the two components.
     *
     * @param fileDetailsButton the button that expands/collapses the 'File details' panel
     * @param buttonsPanel the panel that contains the OK/cancel control buttons
     * @return a panel where the specified 'File details' button and OK/cancel control buttons are laid out on a single row
     */
    protected JPanel createButtonsPanel(JButton fileDetailsButton, JPanel buttonsPanel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        if (fileDetailsButton != null) {
            panel.add(fileDetailsButton);
        }
        panel.add(Box.createVerticalGlue());
        panel.add(buttonsPanel);

        return panel;
    }
    
    /**
     * Sets the list of files used by this job.
     * @param files
     */
    protected void setFiles(FileSet files) {
        this.files = files;
        if (collapseExpandButton != null) {
            collapseExpandButton.setText(i18n("nb_files", Integer.toString(files.size())));
        }
    }

    @Override
    public void dispose() {
        if (collapseExpandButton != null) {
            lastExpanded = collapseExpandButton.getExpandedState();
        }
        super.dispose();
    }
}
