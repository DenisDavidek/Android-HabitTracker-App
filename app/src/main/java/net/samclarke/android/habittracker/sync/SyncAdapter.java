package net.samclarke.android.habittracker.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import net.samclarke.android.habittracker.R;
import net.samclarke.android.habittracker.util.UIUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();

    // 60 seconds (1 minute) * 60 minutes (1 hour) * 12 = 12 hours
    private static final int SYNC_INTERVAL = 60 * 60 * 24;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 24;

    private static final String QUOTE_API_URI =
            "http://api.forismatic.com/api/1.0/?method=getQuote&format=json&lang=en";


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        final String QUOTE = "quoteText";
        final String AUTHOR = "quoteAuthor";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(QUOTE_API_URI);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                return;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }

            if (builder.length() == 0) {
                Log.e(LOG_TAG, "Quote JSON was empty.");
                return;
            }

            JSONObject json = new JSONObject(builder.toString());

            String quote = json.getString(QUOTE).trim() + "\n- " + json.getString(AUTHOR).trim();

            UIUtils.setQuoteOfDay(getContext(), quote);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error fetching quote", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error parsing quote JSON", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private static void configurePeriodicSync(Context context, Account account) {
        String authority = context.getString(R.string.authority);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(SYNC_INTERVAL, SYNC_FLEXTIME)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle())
                    .build();

            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), SYNC_INTERVAL);
        }
    }

    private static void syncImmediately(Context context, Account account) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account, context.getString(R.string.authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        if (accountManager.getPassword(newAccount) == null) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.e(LOG_TAG, "Couldn't create sync account");
                return;
            }

            configurePeriodicSync(context, newAccount);

            ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.authority), true);

            syncImmediately(context, newAccount);
        }
    }
}
