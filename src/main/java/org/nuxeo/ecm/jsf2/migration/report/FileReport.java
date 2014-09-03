/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:glefevre@nuxeo.com">Gildas</a>
 */
package org.nuxeo.ecm.jsf2.migration.report;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.jsf2.migration.enumeration.EnumTypeMigration;

/**
 * Object containing the data for the report after analyzing a file.
 *
 * @since 5.9.6
 */
public class FileReport {

    private File attachedFile;

    private Map<EnumTypeMigration, Integer> listMigrations;

    private Map<EnumTypeMigration, List<String>> listParams;

    public FileReport(File attachedFile) {
        this.attachedFile = attachedFile;
        listMigrations = new HashMap<EnumTypeMigration, Integer>();
        listParams = new HashMap<EnumTypeMigration, List<String>>();
    }

    public File getAttachedFile() {
        return attachedFile;
    }

    public void setAttachedFile(
            File attachedFile) {
        this.attachedFile = attachedFile;
    }

    public Map<EnumTypeMigration, Integer> getListMigrations() {
        return listMigrations;
    }

    public Map<EnumTypeMigration, List<String>> getListParams() {
        return listParams;
    }
}
