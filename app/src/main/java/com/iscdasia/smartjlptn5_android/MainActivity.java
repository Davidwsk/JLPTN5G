package com.iscdasia.smartjlptn5_android;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewFragment;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.*;

public class MainActivity extends AppCompatActivity
        //Note : OnFragmentInteractionListener of all the fragments
        implements
        QuestionListFragment.OnListFragmentInteractionListener,
        QuestionPage.OnFragmentInteractionListener,
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);

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

            //Init local storage
            initLocalStore().get();

            // Create an adapter to bind the items with the view
            //mAdapter = new ToDoItemAdapter(this, R.layout.fragment_questionlist);
//            ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
//            listViewToDo.setAdapter(mAdapter);

            // Load the items from the Mobile Service
            refreshItemsFromTable(CurrentApp.QUESTION_GROUP_ID, CurrentApp.NO_OF_QUESTION);

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
                toggle.setDrawerIndicatorEnabled(true);
                ;


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

    /**
     * Method that refreshes the selected menu item on back
     *
     * @param currentFragment The currentFragment
     */
    private final void refreshSelectedMenuItem(Fragment currentFragment) {

        if (currentFragment != null) {
            if (getSupportActionBar() != null) {
                enableViews(currentFragment instanceof QuestionPage == true);
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
            refreshItemsFromTable(CurrentApp.QUESTION_GROUP_ID, CurrentApp.NO_OF_QUESTION);
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
            //fragment = new QuestionListFragment();
        } else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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

    @Override
    public void onListFragmentInteraction(Question item) {
        CurrentApp.CURRENT_QUESTION_POSITION_ID = DataAccess.QUESTION_ARRAY_LIST.indexOf(item);
        replaceFragment(QuestionPage.class);
    }

    @Override
    public void onFragmentInteraction(String title) {
        // NOTE:  Code to replace the toolbar title based current visible fragment
        getSupportActionBar().setTitle(title);
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
    private void refreshItemsFromTable(final String questionGroupId, final int noOfQuestions) {

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

                replaceFragment(QuestionListFragment.class);

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
}

