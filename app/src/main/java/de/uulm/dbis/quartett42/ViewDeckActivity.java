package de.uulm.dbis.quartett42;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.uulm.dbis.quartett42.data.Card;
import de.uulm.dbis.quartett42.data.Deck;
import de.uulm.dbis.quartett42.data.Property;

public class ViewDeckActivity extends AppCompatActivity {
    public static final String TAG = "ViewDeckActivity";

    String chosenDeck = "";
    int srcMode = -1;
    int currentCardIndex = 0;
    Deck deck = null;

    ContentLoadingProgressBar spinner; //Spinner fuer Ladezeiten
    ViewPager viewPager;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_deck);

        spinner = (ContentLoadingProgressBar) findViewById(R.id.progressBar1);
        spinner.show();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //JSON-String auslesen:
        Intent intent = getIntent();
        chosenDeck = intent.getStringExtra("chosen_deck");
        srcMode = intent.getIntExtra("srcMode", -1);

        // use AsyncTask to load Deck from JSON
        new LoadDeckTask().execute();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        //Intent intent = new Intent(this, GalleryActivity.class);
        //intent.putExtra("json_string", jsonString);
        //startActivity(intent);
        finish();
        return true;
    }

    public void showNextCard(View view) {
        currentCardIndex = (currentCardIndex +1) % deck.getCardList().size();  // wrap around positives
        updateView();
    }

    public void showPreviousCard(View view) {
        int deckSize = deck.getCardList().size();
        currentCardIndex = ((currentCardIndex -1) % deckSize + deckSize) % deckSize;   // wrap around negatives
        updateView();
    }

    public void goToNewGame(View view) {
        //Gucken, ob ein laufendes Spiel vorhanden:
        //runningGame Value lesen, 1 falls Spiel pausiert, 0 wenn nicht:
        if(sharedPref.getInt("runningGame", 0) == 1){
            //Fragen, ob vorhandenes Spiel fortgesetzt werden soll oder neues begonnen werden soll
            String[] aktionen = {"Ja, Spiel fortsetzen", "Nein, dieses Deck spielen"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Soll ein pausiertes Spiel fortgesetzt werden?")
                    .setItems(aktionen, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // The 'which' argument contains the index position of the selected item
                            if(which == 0){
                                // vorhandenes Spiel laden:
                                loadGame();
                            }else if(which == 1){
                                //alte gespeichertes Spiel loeschen
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putInt("runningGame", 0);
                                editor.putInt("currentRoundsLeft", 0);
                                editor.putInt("currentPointsPlayer", 0);
                                editor.putInt("currentPointsComputer", 0);
                                editor.putString("currentCardsPlayer", "");
                                editor.putString("currentCardsComputer", "");
                                editor.commit();
                                //Zu NewGameActivity weiter leiten
                                startNewGame();
                            }
                        }
                    });
            AlertDialog alert =  builder.create();
            alert.show();
        }else{
            //Zu NewGameActivity weiter leiten
            startNewGame();
        }
    }

    public void updateView() {
        // get all the view elements
        TextView cardTitleTextView = (TextView) findViewById(R.id.cardTitleTextView);
        ListView cardAttributeListView = (ListView) findViewById(R.id.cardAttributeListView);
        viewPager = (ViewPager) findViewById(R.id.cardImageViewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(viewPager, true);
        // get the current card
        Card card = deck.getCardList().get(currentCardIndex);

        ArrayList<Property> attrList = Util.buildAttrList(deck.getPropertyList(), card);
        // feed attribute list to array adapter
        ArrayAdapter<Property> attrListAdapter = new AttributeItemAdapter(
                false,
                false,
                false,
                this,
                R.layout.attr_list_item,
                attrList
        );
        cardAttributeListView.setAdapter(attrListAdapter);

        // update the image viewPager
        PagerAdapter pagerAdapter = new ImageSlidePagerAdapter(
                getSupportFragmentManager(), card.getImageList(), deck.getName(), srcMode);

        viewPager.setAdapter(pagerAdapter);

        // set title with number and name, e.g. "(1/32) cardName"
        cardTitleTextView.setText("[" + (currentCardIndex+1) + "/" + deck.getCardList().size() + "] "
                + card.getName());

    }

    private class LoadDeckTask extends AsyncTask<Void, Void, Deck> {

        @Override
        protected Deck doInBackground(Void... voids) {
            LocalJSONHandler localJsonHandler = new LocalJSONHandler(ViewDeckActivity.this, srcMode);
            return localJsonHandler.getDeck(chosenDeck);
        }

        @Override
        protected void onPostExecute(Deck deck) {
            ViewDeckActivity.this.deck = deck; // is this ok?
            updateView();
            spinner.hide();
        }
    }

    public void startNewGame(){
        Intent intent = new Intent(this, NewGameActivity.class);
        intent.putExtra("chosen_deck", chosenDeck);
        intent.putExtra("srcMode", srcMode);
        intent.putExtra("new_game_source", "view_deck_activity");
        startActivity(intent);
    }

    public void loadGame(){
        spinner.show();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("chosen_deck", sharedPref.getString("currentChosenDeck", "Sesamstrasse"));
        intent.putExtra("srcMode", sharedPref.getInt("srcMode", -1));
        startActivity(intent);
    }

}
