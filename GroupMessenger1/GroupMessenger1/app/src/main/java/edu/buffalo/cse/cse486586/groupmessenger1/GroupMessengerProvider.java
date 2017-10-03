package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.database.MatrixCursor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import android.content.Context;

/**
 * GroupMessengerProvider is a key-value table. Once again, please note that we do not implement
 * full support for SQL as a usual ContentProvider does. We re-purpose ContentProvider's interface
 * to use it as a key-value table.
 * 
 * Please read:
 * 
 * http://developer.android.com/guide/topics/providers/content-providers.html
 * http://developer.android.com/reference/android/content/ContentProvider.html
 * 
 * before you start to get yourself familiarized with ContentProvider.
 * 
 * There are two methods you need to implement---insert() and query(). Others are optional and
 * will not be tested.
 * 
 * @author stevko
 *
 */
public class GroupMessengerProvider extends ContentProvider {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /*
         * TODO: You need to implement this method. Note that values will have two columns (a key
         * column and a value column) and one row that contains the actual (key, value) pair to be
         * inserted.
         * 
         * For actual storage, you can use any option. If you know how to use SQL, then you can use
         * SQLite. But this is not a requirement. You can use other storage options, such as the
         * internal storage option that we used in PA1. If you want to use that option, please
         * take a look at the code for PA1.
         */

        //tariq start

        String filename = values.get("key").toString();
        FileOutputStream outputStream;
        String value = null;

        //key is file name
        try {
            outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            value = values.get("value").toString();
            outputStream.write(value.getBytes());
            outputStream.close();
        }catch (Exception e) {
            Log.e(TAG, "File write failed");
        }
        //tariq end
        Log.v("insert", value);
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */
        //tariq start

        //https://www.youtube.com/watch?v=eNW1d8tiXmQ&t=248s

        FileInputStream inputStream=null;
        BufferedInputStream reader=null;
        int byte_read;
        char char_read;

        String Value_read__from_file="";
        try {
            inputStream = getContext().openFileInput(selection); //selection is 'where' clause => key name
            reader= new BufferedInputStream(inputStream);

            //Read Character one by one from this file
            while((byte_read= reader.read())!=-1){
                char_read = (char)byte_read;
                Value_read__from_file+=char_read;
            }
            reader.close();
        }catch (Exception e) {
            Log.e(TAG, "File Read failed");
        }


        //Write read data to cursor and return
        MatrixCursor matrixCursor = new MatrixCursor(new String[] { "key", "value"});
        matrixCursor.addRow(new String[]{selection, Value_read__from_file});


        Log.v("query", selection);
        return matrixCursor;

        //tariq end
    }
}
