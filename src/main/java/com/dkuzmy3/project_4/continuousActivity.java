package com.dkuzmy3.project_4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;

// original work by Dmytro Kuzmyk all rights reserved lol
public class continuousActivity extends AppCompatActivity {

    protected String[][] map;           // map grid made of a 2d array

    protected String[][] gopher;        // location of the gopher + history where we've been
    protected int x;                    // variables to assign random numbers
    protected int y;                    // for hopher location

    protected String[] notificationArray = {"Success", "Near miss", "Close Guess",
            "Complete miss", "Disaster"};
    protected int movX;                 // variables to assign random numbers for 1st move
    protected int movY;

    protected String[][] pathArray;     // array for possible locations of gopher (used in search)

    protected String stringMap = "";    // string to fetch with the sum of 2d array map
    TextView map_layer;                 // xml objects
    TextView nameMover;
    TextView notif;
    TextView winnerText;

    private View a;

    Object threadPauseLockOne;
    protected boolean threadOnePaused = false;
    Object threadPauseLockTwo;
    protected boolean threadTwoPaused = false;

    private volatile boolean threadOneMoves = true;    // to make turns for threads
    private volatile boolean threadTwoMoves = false;
    private volatile boolean finished = false;          // game state
    private volatile boolean adjacent8 = true;
    private volatile boolean adjacent2 = true;

    threadOne thread_one;                               // thread references
    threadTwo thread_two;

    private Handler handlerOne = new Handler();         // handler references
    private Handler handlerTwo = new Handler();
    private int searchResult = 0;
    private int movesCount;                             // count of moves for debug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continuous);

        map = new String[10][10];
        gopher = new String[10][10];
        pathArray = new String[10][10];

        thread_one = new threadOne();                   // create objects of threads
        thread_two = new threadTwo();

        map_layer = findViewById(R.id.map_text);
        nameMover = findViewById(R.id.guesser);
        notif = findViewById(R.id.notification);
        winnerText = findViewById(R.id.winner_text);

        initialize();                                   // initialize the grid
        twoDtoOneD();                                   // transform 2d into 1d
        spawnGopher();                                  // spawn the location of gopher on the map
        map_layer.setText(stringMap);                   // display the map on ui
        Log.i("mapString", stringMap);
    }

    public void makeGuess(View w) {                     // button function "GUESS"
        a = w;
        if (threadOneMoves && !finished) {                           // thread one moves
            threadOneMoves = false;
            threadTwoMoves = true;
            if(movesCount==0) thread_one.start();
            else thread_one.onResume();
        } else if (threadTwoMoves && !finished) {                    // thread 2 moves
            thread_one.onPause();
            threadOneMoves = true;
            threadTwoMoves = false;
            if(movesCount==1) thread_two.start();
            else thread_two.onResume();
        } else
            Log.i("makeGuess", "ERROR: neither boolean moves are true, means GAME OVER :)");
    }

    public void spawnGopher() {                         // spawn gopher in a random location
        Random random = new Random();
        //x = 1;                  // debug
        //y = 0;                  // debug
        x = random.nextInt(9);
        y = random.nextInt(9);
        Log.i("spawnGopher", "x: " + x + " y:" + y);
        gopher[y][x] = "G";                             // gopher id
    }

    public void initialize() {                          // spawn map

        Log.i("initialize", "done");
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 9 && j == 9) map[i][j] = "O";
                else map[i][j] = "O ";
                gopher[i][j] = "a";
            }
        }
    }

    public void twoDtoOneD() {                          // turn 2d map into 1d array
        Log.i("conversion", "done");         // so i can use it for the display
        stringMap = "";
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                stringMap += map[i][j];
            }
        }
    }

    public boolean checkDone(int posY, int posX){       // returns true if the position is already
        if(posX>=10 || posY>=10) return false;
        if(gopher[posY][posX] == "X") return true;      // checked
        else return false;
    }

    public void updateMoveToTable(int posY, int posX, int who){  // update arrays with move
        gopher[posY][posX] = "X";
        if (who == 1){map[posY][posX] = "Ø ";}
        else map[posY][posX] = "Θ ";
    }

    public int searchAt(int posY, int posX){
        Log.i("searchingAt", "x:"+posX+" y:"+posY);
        if(posY == y && posX == x){return 1;}   // if gopher location = move location, win

        for (int i = 0; i < 3; i++) {           // search gopher within 2 holes
            if(posY == y && posX+i == x && posX+i<=9) return 3;
            if(posY+i == y && posX == x && posY+i<=9) return 3;
            if(posY+i==y && movX+i==x && posX+i<=9 && posY+i<=9) return 3;
            if(posY-i==y && movX+i==x && posX+i<=9 && posY-i>=0) return 3;
            if(posY+i==y && movX-i==x && posX-i>=0 && posY+i<=9) return 3;
            if(posY-i==y && movX-i==x && posX-i>=0 && posY-i>=0) return 3;
        }
        for (int i = 1; i < 9; i++) {           // search gopher within 8 holes
            if(posY == y && posX+i == x && posX+i<=9) return 2;
            if(posY+i == y && posX == x && posY+i<=9) return 2;
            if(posY+i==y && movX+i==x && posX+i<=9 && posY+i<=9) return 2;
            if(posY-i==y && movX+i==x && posX+i<=9 && posY+i>=0) return 2;
            if(posY+i==y && movX-i==x && posX+i>=0 && posY+i<=9) return 2;
            if(posY-i==y && movX-i==x && posX-i>=0 && posY-i>=0) return 2;
        }// get diagonal top right

        if(checkDone(posY, posX)) return 5;    // if move already made

        else return 4;                              // if nowhere near close
    }
    // creates a path in which the gopher exists, we travel along that path to find it
    public void getAdjacent8(int val){
        if(adjacent8){ // so that when we find a 8 holes far one, we wont overwrite
            for (int i = 0; i < 10; i++) {  // and when we find a 2 holes far, we overwrite once
                for (int j = 0; j < 10; j++) {
                    pathArray[i][j]="a";
                }
            }
            for(int a = 0; a < val+1; a++){    // get the vertical and horizontal paths
                if(!checkDone(movY+a, movX)&& movY+a<=9)pathArray[movY+a][movX] = "p";
                if(!checkDone(movY, movX+a) && movX+a<=9)pathArray[movY][movX+a] = "p";   // create an array with possible moves
                // get diagonal top left
                if(movY-a>=0 && movX-a>=0 && !checkDone(movY-a, movX-a)){
                    pathArray[movY-a][movX-a] = "p";
                }// get diagonal top right
                if(movY-a>=0 && movX+a<=9 && !checkDone(movY-a, movX+a)){
                    pathArray[movY-a][movX+a] = "p";
                }// get diagonal bottom left
                if(movY+a<=9 && movX-a>=0 && !checkDone(movY+a, movX-a)){
                    pathArray[movY+a][movX-a] = "p";
                }// get diagonal bottom right
                if(movY+a<=9 && movX+a<=9 && !checkDone(movY+a, movX+a)) {
                    pathArray[movY + a][movX + a] = "p";
                }
            }
        }
    }

    public void getAdjacent2(int val){
        if(adjacent2){ // so that when we find a 8 holes far one, we wont overwrite
            for (int i = 0; i < 10; i++) {  // and when we find a 2 holes far, we overwrite once
                for (int j = 0; j < 10; j++) {
                    pathArray[i][j]="a";
                }
            }
            for(int a = 0; a < val+1; a++){    // get the vertical and horizontal paths
                if(!checkDone(movY+a, movX) && movY+a<=9)pathArray[movY+a][movX] = "p";
                if(!checkDone(movY, movX+a) && movX+a<=9)pathArray[movY][movX+a] = "p";   // create an array with possible moves
                // get diagonal top left
                if(movY-a>=0 && movX-a>=0 && !checkDone(movY-a, movX-a)){
                    pathArray[movY-a][movX-a] = "p";
                }// get diagonal top right
                if(movY-a>=0 && movX+a<=9 && !checkDone(movY-a, movX+a)){
                    pathArray[movY-a][movX+a] = "p";
                }// get diagonal bottom left
                if(movY+a<=9 && movX-a>=0 && !checkDone(movY+a, movX-a)){
                    pathArray[movY+a][movX-a] = "p";
                }// get diagonal bottom right
                if(movY+a<=9 && movX+a<=9 && !checkDone(movY+a, movX+a)) {
                    pathArray[movY + a][movX + a] = "p";
                }
            }
        }
    }

    class threadOne extends Thread {           // thread one
        threadOne() {
            threadPauseLockOne = new Object();
        }

        @Override
        public void run() {
            Log.i("threadOne", "running");

            while(!finished){                               // LOOPER!

                synchronized (threadPauseLockOne) { // if paused, it waits on an empty object
                    while (threadOnePaused) {
                        try {
                            threadPauseLockOne.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                if (movesCount == 0) {
                    movX = 0;  // thread 1 starts at (0,0)
                    movY = 0;
                    Log.i("firstMove", "moves x: " + movX + " y: " + movY);
                    searchResult = searchAt(movY, movX);        // react to the move
                    updateMoveToTable(movY, movX, 1);              // update table in history
                    twoDtoOneD();                               // convert to 1d and update display
                }

                // search algorithm based on response to move
                else{
                    if(searchResult == 1){
                        Log.i("Thread 1", "run: FINISHED!");
                        map[y][x] = "Ü ";
                        twoDtoOneD();
                        finished = true;} // option 1 -------------------------

                    else if (searchResult == 2){            // option 2 -------------------------
                        Log.i("result2", "running");
                        // find all adjacent locations and move along
                        getAdjacent8(8);
                        adjacent8 = false; // stops the resetting of patharray every time i step there
                        String possible = "";
                        int a1, a2;
                        a1 = 0;
                        a2 = 0;

                        while(possible != "yes"){
                            // next move is in pathArray, find it and return position
                            if(pathArray[a1][a2] == "p"){
                                pathArray[a1][a2]="X";
                                movY=a1;
                                movX=a2;
                                possible="yes";
                            }

                            a2++;   // increment x
                            if(a2 == 9){a2 = 0; a1++;} // if x reaches 10, reset and increase y
                            if(a1 == 9) possible = "p";// if y goes beyond the board, there's an error
                        }
                        Log.i("result2", "moves x: " + movX + " y: " + movY);
                        searchResult = searchAt(movY, movX);
                        updateMoveToTable(movY, movX, 1);
                        twoDtoOneD();
                    }

                    else if(searchResult == 3){         //option 3 -------------------------------
                        // find all adjacent locations and move along
                        Log.i("result3", "running");
                        getAdjacent2(2);
                        adjacent2 = false;  // stop the resetting of patharray every time i step there
                        adjacent8 = false;

                        String possible = "";
                        int a1, a2;
                        a1 = 0;
                        a2 = 0;

                        while(possible != "yes"){
                            // next move is in pathArray, find it and return position
                            if(pathArray[a1][a2] == "p"){
                                pathArray[a1][a2]="X";
                                movY=a1;
                                movX=a2;
                                possible="yes";
                            }
                            a2++;   // increment x
                            if(a2 == 9){a2 = 0; a1++;} // if x reaches 10, reset and increase y
                            if(a1 == 9) possible = "p";// if y goes beyond the board, there's an error
                        }
                        Log.i("result3", "moves x: " + movX + " y: " + movY);
                        searchResult = searchAt(movY, movX);
                        updateMoveToTable(movY, movX, 1);
                        twoDtoOneD();
                    }

                    else if(searchResult == 4 || searchResult == 5){
                        if(!adjacent8 || !adjacent2) {
                            String possible = "";
                            int a1, a2;
                            a1 = a2 = 0;

                            while(possible != "p"){
                                // next move is in pathArray, find it and return position
                                if(pathArray[a1][a2] == "p"){
                                    pathArray[a1][a2]="X";
                                    movY=a1;
                                    movX=a2;
                                    possible="p";
                                }
                                a2++;   // increment x
                                if(a2 == 10){a2 = 0; a1++;} // if x reaches 10, reset and increase y
                                if(a1 == 10) possible = "p";// if y goes beyond the board, there's an error
                            }
                            Log.i("result3", "moves x: " + movX + " y: " + movY);
                            searchResult = searchAt(movY, movX);
                            updateMoveToTable(movY, movX, 1);
                            twoDtoOneD();
                        }
                        else{
                            Log.i("result4_5", "running");
                            // linear search on X axes, reset when hit wall and move down Y by 1 floor

                            if (movX + 1 < 10) movX += 1;  // linear move to right
                            else {
                                if (movY + 1 < 10) {
                                    movY += 1;
                                } else {
                                    movY -= 1;
                                }
                                movX = 0;
                            }
                            pathArray[movX][movY] = "X";
                            Log.i("result4_5", "moves x: " + movX + " y: " + movY);
                            searchResult = searchAt(movY, movX);
                            updateMoveToTable(movY, movX, 1);
                            twoDtoOneD();
                        }
                    }
                }

                try {
                    handlerOne.post(new Runnable() {
                        @Override
                        public void run() {
                            nameMover.setText("Thread 1");
                            notif.setText(notificationArray[searchResult-1]);
                            map_layer.setText(stringMap);
                            if(finished){
                                // reset the map with the gopher showing
                                nameMover.setText("Thread 2");
                                winnerText.setText("Winner Thread 2");
                            }
                        }
                    });
                    Thread.sleep(800);
                } catch (Exception e) {
                    Log.i("threadOne", "not working:");
                    e.printStackTrace();
                }

                movesCount++;    // this is useful to debug who won, odd # t1, even # t2
                Log.i("Thread1", "moves: "+movesCount);
                //twoDtoOneD();
                onPause();
                makeGuess(a);
            }
        }

        public void onPause(){
            synchronized (threadPauseLockOne){
                threadOnePaused = true;
            }
        }

        public void onResume(){
            synchronized (threadPauseLockOne){
                threadOnePaused = false;
                threadPauseLockOne.notifyAll();
            }
        }

    } // end thread 1

    class threadTwo extends Thread {           // thread two
        threadTwo() {
            threadPauseLockTwo = new Object();
        }

        @Override
        public void run() {
            Log.i("threadTwo", "running");

            while(!finished){                               // LOOPER!

                synchronized (threadPauseLockTwo) { // if paused, it waits on an empty object
                    while (threadTwoPaused) {
                        try {
                            threadPauseLockTwo.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }

                // search algorithm based on response to move
                if(searchResult == 1){
                    Log.i("Thread 2", "run: FINISHED!");
                    map[y][x] = "Ü ";
                    twoDtoOneD();
                    finished = true;} // option 1 -------------------------

                else if (searchResult == 2){            // option 2 -------------------------
                    Log.i("result2", "running");
                    // find all adjacent locations and move along
                    getAdjacent8(8);
                    adjacent8 = false; // stop the resetting of patharray every time i step there
                    String possible = "";
                    int a1, a2;
                    a1 = 0;
                    a2 = 0;

                    while(possible != "yes"){
                        // next move is in pathArray, find it and return position
                        if(pathArray[a1][a2] == "p"){
                            pathArray[a1][a2]="X";
                            movY=a1;
                            movX=a2;
                            possible="yes";
                        }

                        a2++;   // increment x
                        if(a2 == 9){a2 = 0; a1++;} // if x reaches 10, reset and increase y
                        if(a1 == 9) possible = "p";// if y goes beyond the board, there's an error
                    }
                    Log.i("result2", "moves x: " + movX + " y: " + movY);
                    searchResult = searchAt(movY, movX);
                    updateMoveToTable(movY, movX, 2);
                    twoDtoOneD();
                }

                else if(searchResult == 3){         //option 3 -------------------------------
                    // find all adjacent locations and move along
                    Log.i("result3", "running");
                    getAdjacent2(2);
                    adjacent2 = false;  // stop the resetting of patharray every time i step there
                    adjacent8 = false;

                    String possible = "";
                    int a1, a2;
                    a1 = 0;
                    a2 = 0;

                    while(possible != "yes"){
                        // next move is in pathArray, find it and return position
                        if(pathArray[a1][a2] == "p"){
                            pathArray[a1][a2]="X";
                            movY=a1;
                            movX=a2;
                            possible="yes";
                        }
                        a2++;   // increment x
                        if(a2 == 9){a2 = 0; a1++;} // if x reaches 10, reset and increase y
                        if(a1 == 9) possible = "p";// if y goes beyond the board, there's an error
                    }
                    Log.i("result3", "moves x: " + movX + " y: " + movY);
                    searchResult = searchAt(movY, movX);
                    updateMoveToTable(movY, movX, 2);
                    twoDtoOneD();
                }

                else if(searchResult == 4 || searchResult == 5){
                    if(!adjacent8 || !adjacent2) {
                        String possible = "";
                        int a1, a2;
                        a1 = a2 = 0;

                        while(possible != "p"){
                            // next move is in pathArray, find it and return position
                            if(pathArray[a1][a2] == "p"){
                                pathArray[a1][a2]="X";
                                movY=a1;
                                movX=a2;
                                possible="p";
                            }
                            a2++;   // increment x
                            if(a2 == 10){a2 = 0; a1++;} // if x reaches 10, reset and increase y
                            if(a1 == 10) possible = "p";// if y goes beyond the board, there's an error
                        }
                        Log.i("result3", "moves x: " + movX + " y: " + movY);
                        searchResult = searchAt(movY, movX);
                        updateMoveToTable(movY, movX, 2);
                        twoDtoOneD();
                    }
                    else{
                        Log.i("result4_5", "running");
                        // linear search on X axes, reset when hit wall and move down Y by 1 floor

                        if (movX + 1 < 10) movX += 1;  // linear move to right
                        else {
                            if (movY + 1 < 10) {
                                movY += 1;
                            } else {
                                movY -= 1;
                            }
                            movX = 0;
                        }
                        pathArray[movX][movY] = "X";
                        Log.i("result4_5", "moves x: " + movX + " y: " + movY);
                        searchResult = searchAt(movY, movX);
                        updateMoveToTable(movY, movX, 2);
                        twoDtoOneD();
                    }
                }

                try {
                    handlerTwo.post(new Runnable() {
                        @Override
                        public void run() {
                            nameMover.setText("Thread 2");
                            notif.setText(notificationArray[searchResult-1]);
                            map_layer.setText(stringMap);
                            if(finished){
                                // reset the map with the gopher showing
                                nameMover.setText("Thread 1");
                                winnerText.setText("Winner Thread 1");
                            }
                        }
                    });
                    Thread.sleep(800);
                } catch (Exception e) {
                    Log.i("threadTwo", "not working:");
                    e.printStackTrace();
                }

                movesCount++;
                Log.i("Thread2", "moves: "+movesCount);
                //twoDtoOneD();
                onPause();
                makeGuess(a);
            }
        }

        public void onPause(){
            synchronized (threadPauseLockTwo){
                threadTwoPaused = true;
            }
        }

        public void onResume(){
            synchronized (threadPauseLockTwo){
                threadTwoPaused = false;
                threadPauseLockTwo.notifyAll();
            }
        }
    }
}