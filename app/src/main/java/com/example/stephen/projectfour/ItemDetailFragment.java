package com.example.stephen.projectfour;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link StepDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment implements
        View.OnClickListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_INDEX = "item_index"; // Which list item?
    public static final String ARG_PANE = "pane"; // Single or two pane layout?
    public static final String ARG_OUTPUT = "output"; // Data for steps and ingredients...
    public static final String MY_ID = "my_id";
    public static final String MY_PLAYER_POSITION = "position";
    public int mIndex;
    public String mRecipeName;
    public String mIngredients;
    public String mPaneLayout;
    public String mServings;
    private SimpleExoPlayer mExoPlayer;
    private PlayerView mPlayerView;

    public long mPlayerPosition;

    ArrayList<String> mOutputList;

    /*
     *  Structure for mOutoutList:
     *  mOutputList[0]  --> recipe_name, # of servings
     *  mOutputList[1]  --> ingredients
     *  mOutputList[>1] --> short_description, verbose_description, thumbnail, videoUrl
     *
     *  The comma characters are actually a delimiter that is defined in MainActivity.
     * */

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIndex = getArguments().getInt(ARG_INDEX) - 1;
        mPaneLayout = getArguments().getString(ARG_PANE);
        mOutputList = getArguments().getStringArrayList(ARG_OUTPUT);
        mIngredients = mOutputList.get(1);
        mServings = mOutputList.get(0).split("42069")[1];
        mRecipeName = mOutputList.get(0).split("42069")[0];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // If the user has navigated to a different step in the detail activity,
        // get that step from the savedInstanceState
        if (savedInstanceState != null) {
            Log.d("LOG", "asdf savedInstanceState is not null in onCreateVew");
            mIndex = savedInstanceState.getInt(MY_ID);
            mPlayerPosition = savedInstanceState.getLong(MY_PLAYER_POSITION);
        } else {
            Log.d("LOG", "asdf savedInstanceState is null in onCreateView");
            mPlayerPosition = 0;
        }

        // Depending on which step the user is on, a different view is inflated.
        if (mIndex == 0) {
            // The first step shows the recipe name, # of servings,
            // and has a button to add the ingredients to the widget.
            View rootView = inflater.inflate(R.layout.fragment_ingredients_detail, container, false);
            mPlayerView = rootView.findViewById(R.id.playerViewRecipe);
            // Load the question mark as the background image until the user answers the question.
            mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource(getResources(), R.drawable.question_mark));
            // Initialize the player.
            initializePlayer(Uri.parse("broken"));
            // There is no video for the ingredients, so hide that view by making the height zero.
            mPlayerView.getLayoutParams().height = 0;
            // Set the text views
            ((TextView) rootView.findViewById(R.id.item_detail_recipe)).setText(mIngredients);
            ((TextView) rootView.findViewById(R.id.item_detail_recipe_title)).setText(mRecipeName);
            // Set button click listener
            Button button = (Button) rootView.findViewById(R.id.addButton);
            button.setOnClickListener(this);
            Log.d("LOG", "asdf onCreateView of Fragment " + mRecipeName);
            return rootView;
        }
        // If the user is past the first step, these other view will be inflated.
        else {
            View rootView;
            String text = mOutputList.get(mIndex).split("42069")[1];
            String videoUri = mOutputList.get(mIndex).split("42069")[3];
            // If (App is running on phone && Phone is in landscape orientation){
            if (mPaneLayout.equals("1") &&
                    getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

                hideSystemUI();

                //rootView = inflater.inflate(R.layout.fragment_step_detail, container, false);
                rootView = inflater.inflate(R.layout.exoplayer_fullscreen, container, false);
                mPlayerView = rootView.findViewById(R.id.playerView);

                // Load the question mark as the background image until the user answers the question.
                mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                        (getResources(), R.drawable.question_mark));
                initializePlayer(Uri.parse(videoUri));

                // To get here, the app is running on a phone that is in landscape,
                // so, the video needs to be fullscreen.
                Log.d("LOG", "asdf NEED FULL SCREEN VIDEO HERE");


            }
            // If (App is running on phone && Phone is in portrait orientation){
            else {
                rootView = inflater.inflate(R.layout.fragment_step_detail, container, false);
                //showSystemUI();
                mPlayerView = rootView.findViewById(R.id.playerView);
                // Load the question mark as the background image until the user answers the question.
                mPlayerView.setDefaultArtwork(BitmapFactory.decodeResource
                        (getResources(), R.drawable.question_mark));
                initializePlayer(Uri.parse(videoUri));
                TextView descriptionTextView = (TextView) rootView.findViewById(R.id.item_detail);
                Button nextButton = (Button) rootView.findViewById(R.id.nextStep);
                Button previousButton = (Button) rootView.findViewById(R.id.previousStep);

                // This view is for users that in some configuration other than portrait on a phone.
                nextButton.setOnClickListener(this);
                previousButton.setOnClickListener(this);
                descriptionTextView.setText(text);

                // Hide the navigation buttons on tablets
                if (mPaneLayout.equals("2")){
                    nextButton.setVisibility(View.INVISIBLE);
                    previousButton.setVisibility(View.INVISIBLE);
                }
                // Hide the next button if this is the last step.
                if (mIndex + 1 == mOutputList.size()) {
                    Log.d("LOG", "asdf Last step, hiding the next button. mIndex: " + mIndex + " mOutoutList.size(): " + mOutputList.size());
                    nextButton.setVisibility(View.INVISIBLE);
                }
                // Hide the previous button if this is the first step.
                if (mIndex == 2) {
                    Log.d("LOG", "asdf First step, heading the previous button. mIndex: " + mIndex + " mOutoutList.size(): " + mOutputList.size());
                    previousButton.setVisibility(View.INVISIBLE);
                }
            }
            return rootView;
        }
    }

    /*
     * Override savedInstanceState() and save mIndex.
     *
     * This is necessary because a user may navigate to a different step within the
     * detail activity on when running the app in portrait orientation on a phone.
     * */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(MY_ID, mIndex);
        long position = mExoPlayer.getCurrentPosition();
        savedInstanceState.putLong(MY_PLAYER_POSITION, position);
    }

    // https://developer.android.com/training/system-ui/immersive
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = this.getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * Initialize ExoPlayer.
     * <p>
     * This is from the Udacity Exoplayer Lesson by Nikita
     * <p>
     * and https://codelabs.developers.google.com/codelabs/exoplayer-intro/#2
     *
     * @param mediaUri The URI of the sample to play.
     */
    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(getContext()),
                    new DefaultTrackSelector(),
                    new DefaultLoadControl());
            mPlayerView.setPlayer(mExoPlayer);
            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getContext(), "ClassicalMusicQuiz");
            MediaSource mediaSource = buildMediaSource(mediaUri);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.seekTo(mPlayerPosition);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    /**
     * Release the player when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        Log.d("LOG", "asdf Release exoPlayer.");
    }

    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
    }

    /*
     *  The onClick method handles the button that adds the ingredients for this
     *  recipe to the widget, and the navigation buttons to the previous/next step.
     *
     *  @param mIngredients is already a class variable
     *  @returns nothing, but there is a Toast to the user
     * */
    @Override
    public void onClick(View v) {
        String button_type = v.toString().split("app:id/")[1];
        Log.d("LOG", "asdf widget button " + button_type);
        if (button_type.equals("addButton}")) {
            Log.d("LOG", "asdf update widget");
            // recipe ingredients widget
            String ingredients = mRecipeName + "\n" + mIngredients;
            Context context = getActivity();
            Toast.makeText(context, "Ingredients added to widget", Toast.LENGTH_SHORT).show();
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.recipe_widget);
            ComponentName thisWidget = new ComponentName(context, recipe_widget.class);
            remoteViews.setTextViewText(R.id.appwidget_text, ingredients);
            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }
        /*
         *  Going to either the previous or next step requires that the text view
         *  showing the step description and the video have to be replaced with the
         *  correct content.
         *
         *  First, the mIndex variable is updated.
         *  Next, the text view is reset
         *  Finally, if the user is on the first step, the 'previous' button is hidden.
         *      -->  if the user is on the last step, the 'next' button is hidden.
         *
         * */
        // Go to the previous step
        if (button_type.equals("previousStep}")) {
            mIndex -= 1;
            Log.d("LOG", "asdf previousStep button, mIndex: " + mIndex);
            String text = mOutputList.get(mIndex).split("42069")[1];
            String videoUri = mOutputList.get(mIndex).split("42069")[3];

            View view = getView();
            ((TextView) view.findViewById(R.id.item_detail)).setText(text);
            releasePlayer();
            initializePlayer(Uri.parse(videoUri));
            // Hide the back button if the user is on the first
            if (mIndex == 2) {
                Button previous = (Button) view.findViewById(R.id.previousStep);
                previous.setVisibility(View.INVISIBLE);
            }

        }
        // Go to the next step
        if (button_type.equals("nextStep}")) {
            mIndex += 1;
            Log.d("LOG", "asdf nextStep button, mIndex:  " + mIndex + " mOutoutList.size(): " + mOutputList.size());
            String text = mOutputList.get(mIndex).split("42069")[1];
            String videoUri = mOutputList.get(mIndex).split("42069")[3];
            releasePlayer();
            initializePlayer(Uri.parse(videoUri));
            View view = getView();
            ((TextView) view.findViewById(R.id.item_detail)).setText(text);
            // Hide the next button if the user is on the last step
            if (mIndex + 1 == mOutputList.size()) {
                Log.d("LOG", "asdf Last step, hiding the next button. mIndex: " + mIndex + " mOutoutList.size(): " + mOutputList.size());
                Button nextButton = (Button) view.findViewById(R.id.nextStep);
                nextButton.setVisibility(View.INVISIBLE);
            }
            // Show the previous button of the user is not on the first step
            Button previous = (Button) view.findViewById(R.id.previousStep);
            previous.setVisibility(View.VISIBLE);
        }
    }
}
