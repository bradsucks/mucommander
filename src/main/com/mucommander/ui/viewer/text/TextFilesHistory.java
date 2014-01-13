/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.ui.viewer.text;

import com.mucommander.PlatformManager;
import com.mucommander.commons.file.AbstractFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores history for viewed and edited files - last position
 */
public class TextFilesHistory {

    public static class FileRecord {
        private String fileName;
        private int scrollPosition;
        private int line, column;
        private FileType fileType;
        private String encoding;

        public FileRecord(String fileName, int firstLine, int row, int column,  FileType fileType, String encoding) {
            this.fileName = fileName;
            update(firstLine, row, column, fileType, encoding);
        }

        public FileRecord(String fileName) {
            this.fileName = fileName;
        }

        public void update(int firstLine, int line, int column,  FileType fileType, String encoding) {
            setScrollPosition(firstLine);
            setLine(line);
            setColumn(column);
            setFileType(fileType);
            setEncoding(encoding);
        }

        public void update(FileRecord source) {
            this.scrollPosition = source.scrollPosition;
            this.line = source.line;
            this.column = source.column;
            this.fileType = source.fileType;
            this.encoding = source.encoding;
        }

        public int getLine() {
            return line <= 0 ? 1 : line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public FileType getFileType() {
            return fileType;
        }

        public void setFileType(FileType fileType) {
            this.fileType = fileType;
        }

        public int getScrollPosition() {
            return scrollPosition;
        }

        public void setScrollPosition(int scrollPosition) {
            this.scrollPosition = scrollPosition;
        }

        public int getColumn() {
            return column > 0 ? column : 1;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = "null".equals(encoding) ? null : encoding;
        }

        @Override
        public String toString() {
            return new StringBuilder(fileName).append('=').append(scrollPosition).append(',').append(getLine()).append(',').append(getColumn()).
                    append(',').append(getFileType()).append(',').append(getEncoding()).toString();
        }
    }

    /** Default text file history file name */
    private static final String DEFAULT_HISTORY_FILE_NAME = "textfiles.history";

    private static final int MAX_NUMBER_OF_RECORDS = 100;

    private static TextFilesHistory instance;


    private List<FileRecord> records = new ArrayList<>();

    public static TextFilesHistory getInstance() {
        if (instance == null) {
            instance = new TextFilesHistory();
            try {
                instance.load();
            } catch (IOException e) {
            }
        }
        return instance;
    }

    // - History file access --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the path to the history file.
     * <p>
     * Will return the default, system dependant bookmarks file.
     * </p>
     * @return             the path to the bookmark file.
     * @throws IOException if there was a problem locating the default history file.
     */
    private static synchronized AbstractFile getHistoryFile() throws IOException {
        return PlatformManager.getPreferencesFolder().getChild(DEFAULT_HISTORY_FILE_NAME);
    }


    public void load() throws IOException {
        load(getHistoryFile());
    }

    public void load(AbstractFile file) throws IOException {
        BufferedReader reader = null;
        records.clear();
        try {
            reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
            String line;
            while( (line = reader.readLine() ) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                FileRecord rec = parseRecord(line);
                if (rec != null) {
                    records.add(rec);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static FileRecord parseRecord(String s) {
        int index = s.indexOf('=');
        if (index < 0) {
            return null;
        }
        String fileName = s.substring(0, index);
        String[] props = s.substring(index + 1).split(",");
        try {
            for (int i = 0; i < props.length; i++) {
                props[i] = props[i].trim();
            }
            FileType type;
            try {
                type = FileType.valueOf(props[3]);
            } catch (Exception e) {
                type = null;
            }
            return new FileRecord(fileName, Integer.parseInt(props[0]), Integer.parseInt(props[1]), Integer.parseInt(props[2]),
                    type, props[4]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void save() {
        try {
            save(getHistoryFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(AbstractFile file) throws  IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
            for (FileRecord rec : records) {
                writer.write(rec.toString());
                writer.write('\n');
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }


    public FileRecord get(AbstractFile file) {
        return get(file.getAbsolutePath());
    }


    public FileRecord get(String fileName) {
        int index = findRecord(fileName);
        return index >= 0 ? records.get(index) : new FileRecord(fileName);
    }

    private int findRecord(String fileName) {
        for (int i = 0; i < records.size(); i++) {
            FileRecord rec = records.get(i);
            if (rec.fileName.equals(fileName)) {
                return i;
            }
        }
        return -1;
    }


    public void updateRecord(FileRecord record) {
        int index = findRecord(record.fileName);
        if (index >= 0) {
            records.remove(index);
        }
        records.add(0, record);
        while (records.size() > MAX_NUMBER_OF_RECORDS) {
            records.remove(records.size()-1);
        }
    }

}