/*
 * Copyright (C) 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.dao.helpers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import org.bcss.collect.android.application.Collect;
import org.bcss.collect.android.logic.FormInfo;
import org.bcss.collect.android.provider.FormsProviderAPI;
import org.bcss.collect.android.provider.InstanceProviderAPI;

public final class ContentResolverHelper {

    private ContentResolverHelper() {

    }

    private static ContentResolver getContentResolver() {
        return Collect.getInstance().getContentResolver();
    }

    public static FormInfo getFormDetails(Uri uri) {
        FormInfo formInfo = null;

        try (Cursor instanceCursor = getContentResolver().query(uri, null, null, null, null)) {
            if (instanceCursor != null && instanceCursor.getCount() > 0) {
                instanceCursor.moveToFirst();
                String instancePath = instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(
                                        InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH));

                String jrFormId = instanceCursor
                        .getString(instanceCursor
                                .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID));
                int idxJrVersion = instanceCursor
                        .getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION);

                String jrVersion = instanceCursor.isNull(idxJrVersion) ? null
                        : instanceCursor
                        .getString(idxJrVersion);
                formInfo = new FormInfo(instancePath, jrFormId, jrVersion);
            }
        }
        return formInfo;
    }

    public static String getFormPath(Uri uri) {
        String formPath = null;
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.getCount() == 1) {
                c.moveToFirst();
                formPath = c.getString(c.getColumnIndex(FormsProviderAPI.FormsColumns.FORM_FILE_PATH));
            }
        }
        return formPath;
    }


    /**
     * Using contentResolver to get a file's extension by the uri
     *
     * @param fileUri Whose name we want to get
     * @return The file's extension
     */
    public static String getFileExtensionFromUri(Uri fileUri) {
        try (Cursor returnCursor = getContentResolver().query(fileUri, null, null, null, null)) {
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                String filename = returnCursor.getString(nameIndex);
                // If the file's name contains extension , we cut it down for latter use (copy a new file).
                if (filename.lastIndexOf('.') != -1) {
                    return filename.substring(filename.lastIndexOf('.'));
                }
            }
        }
        return "";
    }
}
