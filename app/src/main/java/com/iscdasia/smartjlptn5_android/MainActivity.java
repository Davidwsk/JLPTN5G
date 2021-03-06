package com.iscdasia.smartjlptn5_android;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.iscdasia.smartjlptn5_android.model.Question;
import com.iscdasia.smartjlptn5_android.model.UserInformation;
import com.iscdasia.smartjlptn5_android.model.UserQuestionStatistic;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.iscdasia.smartjlptn5_android.CurrentApp.SKU_VALUE;
import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.*;

public class MainActivity extends AppCompatActivity
        //Note : OnFragmentInteractionListener of all the fragments
        implements
        QuestionListFragment.OnListFragmentInteractionListener,
        QuestionPage.OnFragmentInteractionListener,
        QuestionPage.OnFragmentUpdateUserQuestionStatistic,
        OptionFragment.OnFragmentInteractionListener,
        OptionFragment.OnFragmentUpdateNoOfQuestionListener,
        AboutFragment.OnFragmentInteractionListener,
        FeatureSoonFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    /**
     * Mobile Service Client reference
     */
    private MobileServiceClient mClient;

    /**
     * Mobile Service Table used to access data
     */
    //private MobileServiceTable<Question> mToDoTable;

    private MobileServiceTable<Question> mServerQuetionTable;

    //Offline Sync
    /**
     * Mobile Service Table used to access and Sync data
     */
    private MobileServiceSyncTable<Question> mLocalQuestionTable;

    private MobileServiceSyncTable<UserQuestionStatistic> mLocalUserQuestionStatisticTable;

    private MobileServiceSyncTable<UserInformation> mLocalUserInformationTable;

    /**
     * Adapter to sync the items list with the view
     */
    private ToDoItemAdapter mAdapter;

    /**
     * Progress spinner to use for table operations
     */
    private ProgressBar mProgressBar;

    private RecyclerView recyclerView;


    private final static String TAG = MainActivity.class.getSimpleName();

    private NavigationView navigationView;

    private ActionBarDrawerToggle toggle;

    private boolean mToolBarNavigationListenerIsRegistered = false;

    private Menu menu;

    private AdView mAdView;

    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-5096742480218992~6281084271");

        setContentView(R.layout.activity_main);

        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://jlptn5g.azurewebsites.net",
                    this).withFilter(new ProgressFilter());

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            // Get the Mobile Service Table instance to use

            //mToDoTable = mClient.getTable(ToDoItem.class);
            mServerQuetionTable = mClient.getTable(Question.class);

            // Offline Sync
            mLocalQuestionTable = mClient.getSyncTable(Question.class);
            mLocalUserQuestionStatisticTable = mClient.getSyncTable(UserQuestionStatistic.class);
            mLocalUserInformationTable = mClient.getSyncTable(UserInformation.class);


            //Init local storage
            initLocalStore().get();

            // Create an adapter to bind the items with the view
            //mAdapter = new ToDoItemAdapter(this, R.layout.fragment_questionlist);
//            ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
//            listViewToDo.setAdapter(mAdapter);

            boolean IsFirstTimeUse = setUserInformation();

            // Load the items from the Mobile Service
            refreshItemsFromTable(CurrentApp.QUESTION_GROUP_ID, CurrentApp.NO_OF_QUESTION,IsFirstTimeUse);

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //drawer.setDrawerListener(toggle);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //NOTE:  Checks first item in the navigation drawer initially
        //navigationView.setCheckedItem(R.id.nav_question_list);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFrame);
            if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
                finish();
            } else {

                getSupportFragmentManager().popBackStackImmediate();
                currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainFrame);
                refreshSelectedMenuItem(currentFragment);
                //toggle.setDrawerIndicatorEnabled(true);
            }
            //super.onBackPressed();
        }
    }

    /**
     * To be semantically or contextually correct, maybe change the name
     * and signature of this function to something like:
     * <p>
     * private void showBackButton(boolean show)
     * Just a suggestion.
     */
    private void enableViews(boolean enable) {

        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if (enable) {
            // Remove hamburger
            toggle.setDrawerIndicatorEnabled(false);
            // Show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (menu != null)
                menu.setGroupVisible(R.id.main_menu_group, false);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            if (!mToolBarNavigationListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Doesn't have to be onBackPressed
                        onBackPressed();
                    }
                });

                mToolBarNavigationListenerIsRegistered = true;
            }

        } else {
            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Show hamburger
            toggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            toggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
            if (menu != null)
                menu.setGroupVisible(R.id.main_menu_group, true);
        }

        // So, one may think "Hmm why not simplify to:
        // .....
        // getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
        // mDrawer.setDrawerIndicatorEnabled(!enable);
        // ......
        // To re-iterate, the order in which you enable and disable views IS important #dontSimplify.
    }

    private void enableMainMenuGroup(boolean enable) {
        if (menu != null) {
            menu.setGroupVisible(R.id.main_menu_group, enable);
        }
    }

    /**
     * Method that refreshes the selected menu item on back
     *
     * @param currentFragment The currentFragment
     */
    private final void refreshSelectedMenuItem(Fragment currentFragment) {

        if (currentFragment != null) {
            if (getSupportActionBar() != null) {
                enableViews(currentFragment instanceof QuestionPage == true);
                enableMainMenuGroup(currentFragment instanceof QuestionListFragment == true);
                //getSupportActionBar().setDisplayHomeAsUpEnabled(currentFragment instanceof QuestionPage == true);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_getQuestion) {
            refreshItemsFromTable(CurrentApp.QUESTION_GROUP_ID, CurrentApp.NO_OF_QUESTION,false);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
            recyclerView.getAdapter().notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //NOTE: creating fragment object
        //Fragment fragment = null;
        if (id == R.id.nav_question_list) {
            replaceFragment(QuestionListFragment.class);
        } else if (id == R.id.nav_option) {
            replaceFragment(OptionFragment.class);
            //fragment = new QuestionListFragment();
        } else if (id == R.id.nav_exam_1) {
            replaceFragment(FeatureSoonFragment.class);
            setActionBarTitle("Exam section 1");
        } else if (id == R.id.nav_exam_2) {
            replaceFragment(FeatureSoonFragment.class);
            setActionBarTitle("Exam section 2");
        } else if (id == R.id.nav_exam_3) {
            replaceFragment(FeatureSoonFragment.class);
            setActionBarTitle("Exam section 3");
        } else if (id == R.id.nav_about) {
            replaceFragment(AboutFragment.class);

        }

//        //NOTE: Fragment changing code
//        if (fragment != null) {
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.replace(R.id.mainFrame, fragment);
//            ft.commit();
//
//            setTitle("");
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    /**
     * Method that replaces the current fragment
     * <p>
     * //* @param item          The new menu item selected
     *
     * @param fragmentClass The new fragment class
     */
    private final void replaceFragment(@NonNull Class<? extends Fragment> fragmentClass) {
        // Hide the in-app layout if it's visible
        //hideNotificationOverlay();

        //updateSelection(item);

        try {
            final Fragment fragment = fragmentClass.newInstance();

            if (getSupportActionBar() != null) {
                enableViews(fragment instanceof QuestionPage == true);
                enableMainMenuGroup(fragment instanceof QuestionListFragment == true);
            }

            final String backStateName = fragment.getClass().getName();

            final FragmentManager fragmentManager = getSupportFragmentManager();
            final boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);

            if (fragmentPopped == false) {
                //fragment not in back stack, create it.
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFrame, fragment);
                fragmentTransaction.addToBackStack(backStateName);
                fragmentTransaction.commit();
            }
        } catch (Exception exception) {
            Log.e(MainActivity.TAG, "Unable to instantiate the fragment with class '" + fragmentClass.getSimpleName() + "'");
        }
    }

    private final void replaceFragment2(@NonNull Class<? extends Fragment> fragmentClass) {
        // Hide the in-app layout if it's visible
        //hideNotificationOverlay();

        //updateSelection(item);

        try {
            final Fragment fragment = fragmentClass.newInstance();

            if (getSupportActionBar() != null) {
                enableViews(fragment instanceof QuestionPage == true);
                enableMainMenuGroup(fragment instanceof QuestionListFragment == true);
            }

            final String backStateName = fragment.getClass().getName();

            final FragmentManager fragmentManager = getSupportFragmentManager();
            final boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStateName, 0);

            if (fragmentPopped == false) {
                //fragment not in back stack, create it.
                final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFrame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        } catch (Exception exception) {
            Log.e(MainActivity.TAG, "Unable to instantiate the fragment with class '" + fragmentClass.getSimpleName() + "'");
        }
    }

    @Override
    public void onListFragmentInteraction(Question item) {
        CurrentApp.CURRENT_QUESTION_POSITION_ID = DataAccess.QUESTION_ARRAY_LIST.indexOf(item);
        replaceFragment(QuestionPage.class);
    }

    @Override
    public void onFragmentInteraction(String title) {
        // NOTE:  Code to replace the toolbar title based current visible fragment
        if (title == "RefreshQuestionPage") {
            replaceFragment2(QuestionPage.class);
        } else if (title == "ShowQuestionListPage") {
            replaceFragment(QuestionListFragment.class);
        } else if (title == "Purchase") {
            //queryPurchase();
            if(restorePurchase()) {
                createAndShowDialog("You have purchased this item before.\nSystem restore purchase with no cost.","Purchase Successful With Restore","OK");
            }
            else{
                purchaseInApp();
            }

        }
        else if (title == "RestorePurchase") {
            //queryPurchase();
            if(restorePurchase()) {
                createAndShowDialog("Restore purchase complete successfully.","Restore Successful","OK");
            }
            else {
                createAndShowDialog("No purchase item to restore.","Restore Failed","OK");
            }
        }else {
            getSupportActionBar().setTitle(title);
        }


    }


    /**
     * Initialize local storage
     *
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("questionText", ColumnDataType.String);
                    tableDefinition.put("deleted", ColumnDataType.Boolean);
                    tableDefinition.put("questionGroupId", ColumnDataType.String);
                    tableDefinition.put("correctAnswer", ColumnDataType.String);
                    tableDefinition.put("wrongAnswer1", ColumnDataType.String);
                    tableDefinition.put("wrongAnswer2", ColumnDataType.String);
                    tableDefinition.put("wrongAnswer3", ColumnDataType.String);
                    tableDefinition.put("description", ColumnDataType.String);

                    localStore.defineTable("Question", tableDefinition);

                    Map<String, ColumnDataType> uQStatisticDefinition = new HashMap<String, ColumnDataType>();
                    uQStatisticDefinition.put("id", ColumnDataType.String);
                    uQStatisticDefinition.put("UserId", ColumnDataType.String);
                    uQStatisticDefinition.put("QuestionId", ColumnDataType.String);
                    uQStatisticDefinition.put("lastUpdate", ColumnDataType.DateTimeOffset);
                    uQStatisticDefinition.put("Round1Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round1Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round2Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round2Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round3Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round3Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round4Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round4Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round5Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round5Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round6Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round6Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round7Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round7Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round8Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round8Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round9Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round9Answer", ColumnDataType.String);
                    uQStatisticDefinition.put("Round10Result", ColumnDataType.String);
                    uQStatisticDefinition.put("Round10Answer", ColumnDataType.String);

                    localStore.defineTable("UserQuestionStatistic", uQStatisticDefinition);

                    Map<String, ColumnDataType> userInformationDefinition = new HashMap<String, ColumnDataType>();
                    userInformationDefinition.put("id", ColumnDataType.String);
                    userInformationDefinition.put("UserName", ColumnDataType.String);
                    userInformationDefinition.put("Password", ColumnDataType.String);
                    userInformationDefinition.put("NoOfQuestion", ColumnDataType.String);
                    userInformationDefinition.put("lastUpdate", ColumnDataType.DateTimeOffset);
                    userInformationDefinition.put("IsPurchased_1", ColumnDataType.Boolean);

                    localStore.defineTable("UserInformation", userInformationDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    /**
     * Refresh the list with the items in the Table
     */
    private void refreshItemsFromTable(final String questionGroupId, final int noOfQuestions, final boolean isFirstTimeUse) {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<Question> results = refreshItemsFromLocalMobileServiceTable(questionGroupId);

                    //Offline Sync
                    //final List<Question> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mAdapter.clear();
                            DataAccess.QUESTION_ARRAY_LIST.clear();
                            DataAccess.QUESTION_ANSWER_ARRAY_LIST.clear();
                            DataAccess.USER_QUESTION_STATISTIC_RESULT_ARRAY_LIST.clear();
                            CurrentApp.IsFinished = false;
                            CurrentApp.CURRENT_QUESTION_POSITION_ID = 0;
                            Random rnd = new Random();
                            int count = 0;

                            int canBeSelectedCount = results.size();
                            while (count < noOfQuestions && count < canBeSelectedCount) {
                                int iRnd = rnd.nextInt(canBeSelectedCount);
                                Question question = results.get(iRnd);
                                //if (result.Contains(xx))
                                if (DataAccess.QUESTION_ARRAY_LIST.contains(question))
                                    continue;
                                DataAccess.QUESTION_ARRAY_LIST.add(question);


                                count++;
                            }

//                            for (Question item : results) {
//                                //mAdapter.add(item);
//                                DataAccess.QUESTION_ARRAY_LIST.add(item);
//                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if(isFirstTimeUse)
                {
                    replaceFragment(AboutFragment.class);
                }
                else
                {
                    replaceFragment(QuestionListFragment.class);
                }



//                //NOTE:  Open fragment1 initially.
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                ft.replace(R.id.mainFrame, new QuestionListFragment());
//                ft.commit();

//                if(recyclerView != null)
//                    recyclerView.getAdapter().notifyDataSetChanged();
            }
        };

        runAsyncTask(task);
    }

    private Boolean setUserInformation() {

        String android_id = "12345";
        Boolean IsFirstTimeUse = false;
        try {
            android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Exception ex) {
        }

        try {

            ArrayList<UserInformation> userInformationArray = mLocalUserInformationTable.read(null).get();
            if (userInformationArray.size() == 0) {
                UserInformation newUserInformation = new UserInformation();
                newUserInformation.setUserName(android_id);
                newUserInformation.setPassword(android_id);
                newUserInformation.setPurchased_1(false);
                mLocalUserInformationTable.insert(newUserInformation);
                userInformationArray.add(newUserInformation);
                IsFirstTimeUse = true;
            }

            CurrentApp.CURRENT_USER_ID = userInformationArray.get(0).getUserName();
            CurrentApp.NO_OF_QUESTION = Integer.parseInt(userInformationArray.get(0).getNoOfQuestion());
            if (userInformationArray.get(0).getPurchased_1()) {
                DisableAd(true);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return IsFirstTimeUse;
    }

    /**
     * Refresh the list with the items in the Mobile Service Table
     */

    private List<Question> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return mServerQuetionTable.where().field("deleted").
                eq(val(false)).execute().get();
    }

    private List<Question> refreshItemsFromLocalMobileServiceTable(String questionGroupId) throws ExecutionException, InterruptedException {


        List<Question> localQuestionList = mLocalQuestionTable.read(null).get();
        if (localQuestionList.size() == 0) {
            try {
                List<Question> serverQuestionList = mServerQuetionTable.where().field("deleted").
                        eq(val(false)).execute().get();

                for (Question item : serverQuestionList) {
                    mLocalQuestionTable.insert(item);
                }

            } catch (Exception ex) {
                Log.d("TAG", ex.getMessage());
            }
        }

        //Query query = QueryOperations.field("deleted").eq(val(false)) .field("questionGroupId").eq(val(questionGroupId));
        Query query = QueryOperations.field("questionGroupId").eq(val(questionGroupId));
        return mLocalQuestionTable.read(query).get();
    }

    //Offline Sync
    /**
     * Refresh the list with the items in the Mobile Service Sync Table
     */
//    private List<Question> refreshItemsFromMobileServiceTableSyncTable() throws ExecutionException, InterruptedException {
//        //sync the data
//        sync().get();
//        Query query = QueryOperations.field("deleted").
//                eq(val(false));
//
//        return mLocalQuestionTable.read(query).get();
//    }

    //Offline Sync
    /**
     * Sync the current context and the Mobile Service Sync Table
     * @return
     */
//    private AsyncTask<Void, Void, Void> sync() {
//        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
//                    syncContext.push().get();
//                    mLocalQuestionTable.pull(null).get();
//                } catch (final Exception e) {
//                    //createAndShowDialogFromTask(e, "Error");
//                }
//                return null;
//            }
//        };
//        return runAsyncTask(task);
//    }

    /**
     * Run an ASync task on the corresponding executor
     *
     * @param task
     * @return
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }


    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message The dialog message
     * @param title   The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private void createAndShowDialog(final String message, final String title, String btnOKText) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setNeutralButton(btnOKText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.create().show();
    }

    @Override
    public String onFragmentUpdateUserQuestionStatistic(String userId, String questionId, String result, String selectedValue) {
        String returnResult = "";
        try {
            UserQuestionStatistic currentUserQuestionStatistic = null;

            List<UserQuestionStatistic> userQuestionStatisticsList = mLocalUserQuestionStatisticTable.read(null).get();
            for (UserQuestionStatistic userQuestionStatistic :
                    userQuestionStatisticsList) {
                if (userQuestionStatistic.getQuestionId().equals(questionId)) {
                    currentUserQuestionStatistic = userQuestionStatistic;
                    break;
                }
            }

            String resultValue = "" + (result == CurrentApp.CHECK_MARK ? 1 : -1);
            String answer = selectedValue;

            if (currentUserQuestionStatistic == null) {
                currentUserQuestionStatistic = new UserQuestionStatistic(userId, questionId);
                currentUserQuestionStatistic.setRound1Answer(answer);
                currentUserQuestionStatistic.setRound1Result(resultValue);
                mLocalUserQuestionStatisticTable.insert(currentUserQuestionStatistic);
                returnResult = result;
                return returnResult;
            } else {
                returnResult = currentUserQuestionStatistic.getRound1Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound2Result())) {
                currentUserQuestionStatistic.setRound2Answer(answer);
                currentUserQuestionStatistic.setRound2Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound2Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound3Result())) {
                currentUserQuestionStatistic.setRound3Answer(answer);
                currentUserQuestionStatistic.setRound3Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound3Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound3Result())) {
                currentUserQuestionStatistic.setRound3Answer(answer);
                currentUserQuestionStatistic.setRound3Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound3Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound4Result())) {
                currentUserQuestionStatistic.setRound4Answer(answer);
                currentUserQuestionStatistic.setRound4Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound4Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound5Result())) {
                currentUserQuestionStatistic.setRound5Answer(answer);
                currentUserQuestionStatistic.setRound5Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound5Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound6Result())) {
                currentUserQuestionStatistic.setRound6Answer(answer);
                currentUserQuestionStatistic.setRound6Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound6Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound7Result())) {
                currentUserQuestionStatistic.setRound7Answer(answer);
                currentUserQuestionStatistic.setRound7Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound7Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound8Result())) {
                currentUserQuestionStatistic.setRound8Answer(answer);
                currentUserQuestionStatistic.setRound8Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound8Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound9Result())) {
                currentUserQuestionStatistic.setRound9Answer(answer);
                currentUserQuestionStatistic.setRound9Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound9Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

            if (TextUtils.isEmpty(currentUserQuestionStatistic.getRound10Result())) {
                currentUserQuestionStatistic.setRound10Answer(answer);
                currentUserQuestionStatistic.setRound10Result(resultValue);
                mLocalUserQuestionStatisticTable.update(currentUserQuestionStatistic);

                returnResult += " ";
                returnResult += result;
                return returnResult;
            } else {
                returnResult += currentUserQuestionStatistic.getRound10Result() == "1" ? CurrentApp.CHECK_MARK : CurrentApp.CROSS_MARK;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return returnResult;
    }

    @Override
    public void onFragmentUpdateNoOfQuestion(int noOfQuestion) {
        try {
            ArrayList<UserInformation> userInformationArrayList = mLocalUserInformationTable.read(null).get();
            UserInformation userInformation = userInformationArrayList.get(0);
            userInformation.setNoOfQuestion("" + noOfQuestion);
            mLocalUserInformationTable.update(userInformation);
            CurrentApp.NO_OF_QUESTION = noOfQuestion;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    private Boolean isAvailablePurchase(String skuString) {
        try {

            ArrayList<String> skuList = new ArrayList<String>();
//        skuList.add("premiumUpgrade");
//        skuList.add("gas");
            skuList.add(skuString);

            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);

            Bundle skuDetails = mService.getSkuDetails(3,
                    getPackageName(), "inapp", querySkus);

            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {

                ArrayList<String> responseList
                        = skuDetails.getStringArrayList("DETAILS_LIST");

                for (String thisResponse : responseList) {
                    JSONObject object = null;
                    try {

                        object = new JSONObject(thisResponse);
                        String sku = object.getString("productId");
                        String price = object.getString("price");

//                        if (sku.equals("premiumUpgrade")) mPremiumUpgradePrice = price;
//                        else if (sku.equals("gas")) mGasPrice = price;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void purchaseInApp() {
        try {
            //String sku ="com.appcrabs.jlptn5g.removeads";

            //consumePurchase(SKU_VALUE);
//            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
//                    sku, "inapp", "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");

            Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                    SKU_VALUE, "inapp", "iscdasiapayload");

            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

            try {
                startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0));

            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }


        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                createAndShowDialog("Purchase complete successfully.", "Purchase Successful", "OK");
                DisableAd(true);
            } else {
                createAndShowDialog("Failed to purchase", "Purchase Failed", "OK");
            }
//                try {
//                    JSONObject jo = new JSONObject(purchaseData);
//                    String sku = jo.getString("productId");
//                    //alert("You have bought the " + sku + ". Excellent choice, adventurer!");
//
//
//                }
//                catch (JSONException e) {
//                    //alert("Failed to parse purchase data.");
//                    createAndShowDialog("Failed to purchase","Purchase Failed","OK");
//                    e.printStackTrace();
//                }

        }
    }

    private void consumePurchase(String skuString) {
        String purchaseToken = "inapp:" + getPackageName() + ":" + skuString;

        try {
            int response = mService.consumePurchase(3, getPackageName(), purchaseToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void queryPurchase() {
        try {

            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList =
                        ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                String continuationToken =
                        ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);

                    // do something with this purchase information
                    // e.g. display the updated list of products owned by user
                }

                // if continuationToken != null, call getPurchases again
                // and pass in the token to retrieve more items
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> queryPurchase(InAppType inAppType) {
        ArrayList<String> result = new ArrayList<>();
        try {

            Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                result = ownedItems.getStringArrayList(inAppType.toString());
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Boolean restorePurchase() {

        ArrayList<String> queryResult = queryPurchase(InAppType.INAPP_PURCHASE_ITEM_LIST);
        if(queryResult.contains(SKU_VALUE))
        {
            try {

                UserInformation firstUserInformation = mLocalUserInformationTable.read(null).get().get(0);
                if(firstUserInformation != null)
                {
                    firstUserInformation.setPurchased_1(true);
                    mLocalUserInformationTable.update(firstUserInformation);
                    DisableAd(true);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;

    }

    private void DisableAd(boolean b) {
        mAdView.setVisibility(View.GONE);
    }

    public enum InAppType {
        INAPP_PURCHASE_ITEM_LIST,
        INAPP_PURCHASE_DATA_LIST,
        INAPP_DATA_SIGNATURE_LIST

    }
}

