/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import org.bcss.collect.android.application.Collect;
import org.bcss.collect.android.dto.Instance;
import org.bcss.collect.android.provider.InstanceProviderAPI;
import org.bcss.collect.naxa.common.Constant;
import org.bcss.collect.naxa.common.FieldSightUserSession;
import org.bcss.collect.naxa.network.APIEndpoint;
import org.bcss.collect.naxa.site.db.SiteUploadHistoryLocalSource;
import org.odk.collect.android.utilities.ApplicationConstants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import timber.log.Timber;

import static org.bcss.collect.naxa.common.Constant.FormDeploymentFrom.PROJECT;
import static org.bcss.collect.naxa.common.Constant.FormDeploymentFrom.SITE;

/**
 * This class is used to encapsulate all access to the {@link org.bcss.collect.android.provider.InstanceProvider#DATABASE_NAME}
 * For more information about this pattern go to https://en.wikipedia.org/wiki/Data_access_object
 */
public class InstancesDao {

    public Cursor getSentInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " =? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getSentInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getSentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " =? and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader getSentInstancesCursorLoaderSite(String siteId, String sortOrder) {
        CursorLoader cursorLoader;
        if (siteId.length() == 0) {
            cursorLoader = getSentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " =? and "
                            + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + "  =";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    siteId};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader getSentInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " =? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public Cursor getUnsentInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " !=? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.STATUS + " DESC, " + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getUnsentInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + " !=? ";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_SUBMITTED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getUnsentInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getUnsentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " !=? and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader getUnsentInstancesCursorLoaderBySite(String siteId, String sortOrder) {
        CursorLoader cursorLoader;
        if (siteId.length() == 0) {
            cursorLoader = getUnsentInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.STATUS + " !=? and "
                            + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + " = ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    siteId};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public Cursor getSavedInstancesCursor(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL ";

        return getInstancesCursor(null, selection, null, sortOrder);
    }

    public CursorLoader getSavedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL ";

        return getInstancesCursorLoader(null, selection, null, sortOrder);
    }

    public CursorLoader getSavedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getSavedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {"%" + charSequence + "%"};
            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader getSavedInstancesCursorLoaderSite(String siteId, String sortOrder) {
        CursorLoader cursorLoader;
        if (siteId.length() == 0) {
            cursorLoader = getSavedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and "
                            + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + " = ?";
            String[] selectionArgs = {siteId};
            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public Cursor getFinalizedInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + "=? or " + InstanceProviderAPI.InstanceColumns.STATUS + "=?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE, InstanceProviderAPI.STATUS_SUBMISSION_FAILED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getFinalizedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.STATUS + "=? or " + InstanceProviderAPI.InstanceColumns.STATUS + "=?";
        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE, InstanceProviderAPI.STATUS_SUBMISSION_FAILED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getFinalizedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getFinalizedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    "(" + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                            + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                            + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }

    public CursorLoader getFinalizedInstancesCursorLoaderBySite(String siteId, String sortOrder) {
        CursorLoader cursorLoader;
        if (siteId.length() == 0) {
            cursorLoader = getFinalizedInstancesCursorLoader(sortOrder);
        } else {
            String selection =
                    "(" + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                            + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                            + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + " = ?";
            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                    siteId};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }

        return cursorLoader;
    }


    public Cursor getInstancesCursorForFilePath(String path) {
        String selection = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?";
        String[] selectionArgs = {path};

        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getAllCompletedUndeletedInstancesCursor() {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=?)";

        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                InstanceProviderAPI.STATUS_SUBMITTED};
        String sortOrder = InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " ASC";

        return getInstancesCursor(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getAllCompletedUndeletedInstancesCursorLoader(String sortOrder) {
        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=?)";

        String[] selectionArgs = {InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                InstanceProviderAPI.STATUS_SUBMITTED};

        return getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getCompletedUndeletedInstancesCursorLoader(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;
        if (charSequence.length() == 0) {
            cursorLoader = getAllCompletedUndeletedInstancesCursorLoader(sortOrder);
        } else {
            String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                    + InstanceProviderAPI.InstanceColumns.DISPLAY_NAME + " LIKE ?";

            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    "%" + charSequence + "%"};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }
        return cursorLoader;
    }

    public CursorLoader getCompletedUndeletedInstancesCursorLoaderHideOfflineSite(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;

        String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                + "("
                + "length(" + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + ")" + " < 12"
                + "OR " + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + " NOT LIKE '%fake%'"
                + ")";

        String[] selectionArgs = {
                InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                InstanceProviderAPI.STATUS_SUBMITTED
        };

        cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);

        return cursorLoader;
    }

    public CursorLoader getFinalizedInstancesCursorLoaderHideOfflineSite(CharSequence charSequence, String sortOrder) {
        CursorLoader cursorLoader;


        String selection =
                "(" + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                        + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                        + "("
                        + "length(" + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + ")" + " < 12 "
                        + "OR " + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + " NOT LIKE '%fake%'"
                        + ")";

        String[] selectionArgs = {
                InstanceProviderAPI.STATUS_COMPLETE,
                InstanceProviderAPI.STATUS_SUBMISSION_FAILED};

        cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);

        return cursorLoader;
    }

    public CursorLoader getCompletedUndeletedInstancesCursorLoaderBySite(String siteId, String
            sortOrder) {
        CursorLoader cursorLoader;
        if (siteId.length() == 0) {
            cursorLoader = getAllCompletedUndeletedInstancesCursorLoader(sortOrder);
        } else {
            String selection = InstanceProviderAPI.InstanceColumns.DELETED_DATE + " IS NULL and ("
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=? or "
                    + InstanceProviderAPI.InstanceColumns.STATUS + "=?) and "
                    + InstanceProviderAPI.InstanceColumns.FS_SITE_ID + " = ?";


            String[] selectionArgs = {
                    InstanceProviderAPI.STATUS_COMPLETE,
                    InstanceProviderAPI.STATUS_SUBMISSION_FAILED,
                    InstanceProviderAPI.STATUS_SUBMITTED,
                    siteId};

            cursorLoader = getInstancesCursorLoader(null, selection, selectionArgs, sortOrder);
        }
        return cursorLoader;
    }


    public Cursor getInstancesCursorForId(String id) {
        String selection = InstanceProviderAPI.InstanceColumns._ID + "=?";
        String[] selectionArgs = {id};

        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getInstancesCursor(String selection, String[] selectionArgs) {
        return getInstancesCursor(null, selection, selectionArgs, null);
    }

    public Cursor getInstanceCursor() {
        return getInstancesCursor(null, null, null, null);
    }

    public Cursor getInstancesCursor(String[] projection, String selection, String[]
            selectionArgs, String sortOrder) {
        return Collect.getInstance().getContentResolver()
                .query(InstanceProviderAPI.InstanceColumns.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public CursorLoader getInstancesCursorLoader(String[] projection, String selection, String[]
            selectionArgs, String sortOrder) {
        return new CursorLoader(
                Collect.getInstance(),
                InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    public Uri saveInstance(ContentValues values) {
        return Collect.getInstance().getContentResolver().insert(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values);
    }

    public int updateInstance(ContentValues values, String where, String[] whereArgs) {
        return Collect.getInstance().getContentResolver().update(InstanceProviderAPI.InstanceColumns.CONTENT_URI, values, where, whereArgs);
    }

    public void deleteInstancesDatabase() {
        Collect.getInstance().getContentResolver().delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI, null, null);
    }

    public int updateSiteId(String newSiteId, String oldSiteId) {


        ContentValues contentValues = new ContentValues();
        contentValues.put(InstanceProviderAPI.InstanceColumns.FS_SITE_ID, newSiteId);
        String where = InstanceProviderAPI.InstanceColumns.FS_SITE_ID + "=?";

        String[] whereArgs = {
                oldSiteId
        };

        return updateInstance(contentValues, where, whereArgs);
    }

    public void deleteInstancesFromIDs(List<String> ids) {
        int count = ids.size();
        int counter = 0;
        while (count > 0) {
            String[] selectionArgs = null;
            if (count > ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER) {
                selectionArgs = new String[
                        ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER];
            } else {
                selectionArgs = new String[count];
            }

            StringBuilder selection = new StringBuilder();
            selection.append(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + " IN (");
            int j = 0;
            while (j < selectionArgs.length) {
                selectionArgs[j] = ids.get(
                        counter * ApplicationConstants.SQLITE_MAX_VARIABLE_NUMBER + j);
                selection.append('?');

                if (j != selectionArgs.length - 1) {
                    selection.append(',');
                }
                j++;
            }
            counter++;
            count -= selectionArgs.length;
            selection.append(')');
            Collect.getInstance().getContentResolver()
                    .delete(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                            selection.toString(), selectionArgs);

        }
    }

    /**
     * Returns all instances available through the cursor and closes the cursor.
     */
    public List<Instance> getInstancesFromCursor(Cursor cursor) {
        List<Instance> instances = new ArrayList<>();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    int displayNameColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME);
                    int submissionUriColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI);
                    int canEditWhenCompleteIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE);
                    int instanceFilePathIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH);
                    int jrFormIdColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_FORM_ID);
                    int jrVersionColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.JR_VERSION);
                    int statusColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.STATUS);
                    int lastStatusChangeDateColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE);
                    int displaySubtextColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT);
                    int deletedDateColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.DELETED_DATE);
                    int fsSiteColumnIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns.FS_SITE_ID);

                    int databaseIdIndex = cursor.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID);


                    Instance instance = new Instance.Builder()
                            .displayName(cursor.getString(displayNameColumnIndex))
                            .submissionUri(fixUploadUrl(cursor.getString(submissionUriColumnIndex)))
                            .canEditWhenComplete(cursor.getString(canEditWhenCompleteIndex))
                            .instanceFilePath(cursor.getString(instanceFilePathIndex))
                            .jrFormId(cursor.getString(jrFormIdColumnIndex))
                            .jrVersion(cursor.getString(jrVersionColumnIndex))
                            .status(cursor.getString(statusColumnIndex))
                            .lastStatusChangeDate(cursor.getLong(lastStatusChangeDateColumnIndex))
                            .displaySubtext(cursor.getString(displaySubtextColumnIndex))
                            .deletedDate(cursor.getLong(deletedDateColumnIndex))
                            .fieldSightSiteId(cursor.getString(fsSiteColumnIndex))
                            .databaseId(cursor.getLong(databaseIdIndex))
                            .build();

                    instances.add(instance);
                }
            } finally {
                cursor.close();
            }
        }
        return instances;
    }


    private String fixUploadUrl(String url) {
        try {
            if (checkContainsFakeSiteID(url)) {
                String mockedSiteId = getSiteIdFromUrl(url);
                String fsFormId = getFsFormIdFromUrl(url);
                String deployedFrom = getFormDeployedFrom(url);

                url = generateSubmissionUrl(deployedFrom, mockedSiteId.split("-")[0], fsFormId);

                String siteId = SiteUploadHistoryLocalSource.getInstance().getById(mockedSiteId).getNewSiteId();
                url = generateSubmissionUrl(deployedFrom, siteId, fsFormId);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            Timber.e("Failed to fix url");
        }
        return url;
    }

    public static boolean checkContainsFakeSiteID(String url) {
        String[] split = url.split("/");
        String siteId = split[split.length - 1];
        return siteId.contains("fake");

    }


    /**
     * Returns the values of an instance as a ContentValues object for use with
     * {@link #saveInstance(ContentValues)} or {@link #updateInstance(ContentValues, String, String[])}
     * <p>
     * Does NOT include the database ID.
     */
    public ContentValues getValuesFromInstanceObject(Instance instance) {
        ContentValues values = new ContentValues();
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_NAME, instance.getDisplayName());
        values.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, instance.getSubmissionUri());
        values.put(InstanceProviderAPI.InstanceColumns.CAN_EDIT_WHEN_COMPLETE, instance.getCanEditWhenComplete());
        values.put(InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH, instance.getInstanceFilePath());
        values.put(InstanceProviderAPI.InstanceColumns.JR_FORM_ID, instance.getJrFormId());
        values.put(InstanceProviderAPI.InstanceColumns.JR_VERSION, instance.getJrVersion());
        values.put(InstanceProviderAPI.InstanceColumns.STATUS, instance.getStatus());
        values.put(InstanceProviderAPI.InstanceColumns.LAST_STATUS_CHANGE_DATE, instance.getLastStatusChangeDate());
        values.put(InstanceProviderAPI.InstanceColumns.DISPLAY_SUBTEXT, instance.getDisplaySubtext());
        values.put(InstanceProviderAPI.InstanceColumns.DELETED_DATE, instance.getDeletedDate());
        values.put(InstanceProviderAPI.InstanceColumns.FS_SITE_ID, instance.getFieldSightSiteId());

        return values;
    }

    public static String generateSubmissionUrl(String formDeployedFrom, String siteId, String fsFormId) {

        String submissionUrl = FieldSightUserSession.getServerUrl(Collect.getInstance()) + APIEndpoint.FORM_SUBMISSION_PAGE;

        switch (formDeployedFrom) {
            case PROJECT:
                submissionUrl += "project/" + fsFormId + "/" + siteId;
                break;
            case SITE:
                submissionUrl += fsFormId + "/" + siteId;
                break;
            default:
                throw new RuntimeException("Unknown form deployed");
        }

        return submissionUrl;

    }

    private String getSiteIdFromUrl(String url) {
        String[] split = url.split("/");
        return split[split.length - 1];
    }

    private String getFormDeployedFrom(String url) {
        String[] split = url.split("/");
        if (Constant.FormDeploymentFrom.PROJECT.equals(split[split.length - 3])) {
            return Constant.FormDeploymentFrom.PROJECT;
        } else {
            return Constant.FormDeploymentFrom.SITE;
        }
    }


    public Observable<Integer> cascadedSiteIds(String oldId, String newId) {
        return Observable.just(getBySiteId(oldId))
                .flatMapIterable((Function<List<Instance>, Iterable<Instance>>) instances -> instances)
                .map(new Function<Instance, Integer>() {
                    @Override
                    public Integer apply(Instance instance) {
                        String url = instance.getSubmissionUri();
                        String deployedFrom = getFormDeployedFrom(url);
                        String fsFormId = getFsFormIdFromUrl(url);
                        String newUrl = generateSubmissionUrl(deployedFrom, newId, fsFormId);


                        ContentValues contentValues = new ContentValues();
                        contentValues.put(InstanceProviderAPI.InstanceColumns.SUBMISSION_URI, newUrl);
                        contentValues.put(InstanceProviderAPI.InstanceColumns.FS_SITE_ID, newId);
                        String selection = InstanceProviderAPI.InstanceColumns.FS_SITE_ID + "=?";
                        String[] selectionArgs = new String[]{oldId};

                        return updateInstance(contentValues, selection, selectionArgs);
                    }
                });


    }

    public String getFsFormIdFromUrl(String url) {
        String[] split = url.split("/");
        return split[split.length - 2];
    }

    public List<Instance> getBySiteId(String siteId) {

        Cursor cursor;
        String selection = InstanceProviderAPI.InstanceColumns.FS_SITE_ID + "=?";

        String[] selectionArgs = new String[]{siteId};

        cursor = getInstancesCursor(selection, selectionArgs);
        List<Instance> list = getInstancesFromCursor(cursor);
        return list;
    }

}

