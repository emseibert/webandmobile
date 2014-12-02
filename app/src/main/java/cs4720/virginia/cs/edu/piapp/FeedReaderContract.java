package cs4720.virginia.cs.edu.piapp;

import android.provider.BaseColumns;

/**
 * Created by emilyseibert on 11/11/14.
 */

public final class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_JSON = "entryid";
        public static final String COLUMN_NAME_TITLE = "title";
    }
}

