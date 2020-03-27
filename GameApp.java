/*
TODO CONTEXT:
    You are helmet Bro --> 4 activate-able upgrades (Q W E R)

    Prestige to earn ip, and xp --> runes and master-ies system

    RP is special currency --> ads, micro-transactions

    Tap Damage = Auto Attack, DPS = Minion Damage

TODO FOR CERTAIN:
    Boss Timer Text position is off

    implement onUpgrade method in database class, when changing db ---> temp, copy,
        recreate OR db.insert, db.update, db.delete
        --> const database version, compare to value in input file, if different copy
        -->need id key for the number it was added as
        --> same for input file changes, if file does not exist create it...

    Cleanup / Organization:
        --> Cleanup databaseHelper class
        --> Comment Code
        --> Functions to simplify writing multi-line actions

    butterflies / fairies / chests / etc

    Deleting data in settings deletes save data

    Achievements
        --> One Million Dps Achievement --> Fast Damage

    Statistics: total lifetime taps, damage dealt, time played?

TODO (POSSIBLY LOOK INTO LATER):
    Push notifications?

    Customize alert dialogs for reset

    SQLite database for upgrades, list of dps-ers, lists of their upgrades

    show "BOSS" message ??

    Gain ip on prestige

    Buying Multiple ends up being too slow
 */

package kitchen.korey.idleclicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements SettingsFragment.OnFragmentInteractionListener,
        MainFragment.OnFragmentInteractionListener,
        UpgradesFragment.OnFragmentInteractionListener,
        OptionsFragment.OnFragmentInteractionListener,
        DescriptionFragment.OnFragmentInteractionListener{
//----------------------------------------Constants-------------------------------------------------
    final int ABILITY_ONE_DURATION = 30000;
    final int ABILITY_ONE_COOLDOWN = 600000;
    final int ABILITY_TWO_DURATION = 30000;
    final int ABILITY_TWO_COOLDOWN = 1200000;
    final int ABILITY_THREE_DURATION = 30000;
    final int ABILITY_THREE_COOLDOWN = 1800000;
    final int ABILITY_FOUR_DURATION = 30000;
    final int ABILITY_FOUR_COOLDOWN = 3600000;
//--------------------------------------------------------------------------------------------------

//--------------------------------Declaration of Variables------------------------------------------
    enum ENEMY_TYPE {MINION, BOSS}
    enum DIRECTION {LEFT, RIGHT}
    enum ABILITY_STATUS {READY, ACTIVE, COOLDOWN}

    final int MY_SCREEN_HEIGHT = 1920;
    final int MY_SCREEN_WIDTH = 1080;
    final int MY_SCREEN_DENSITY = 480;

    float SCREEN_HEIGHT;
    float SCREEN_WIDTH;
    float SCREEN_DENSITY;

    BigInteger gold = new BigInteger("0");
    BigInteger goldMultiplier = new BigInteger("1");
    BigInteger baseTapDamage = new BigInteger("1");
    BigInteger tapDamageMultiplier = new BigInteger("1");
    BigInteger totalTapDamage = new BigInteger("1");
    BigInteger baseDps = new BigInteger("0");
    BigInteger dpsMultiplier = new BigInteger("1");
    BigInteger totalDps = new BigInteger("0");
    BigInteger timeOfLastExit =
            new BigInteger(Long.toString(Calendar.getInstance().getTimeInMillis()));
    BigInteger distanceLevel = new BigInteger("1");
    BigInteger maximumHealth = calculateMaximumHealth(ENEMY_TYPE.MINION);
    BigInteger currentHealth = maximumHealth;
    BigInteger xp = new BigInteger("0");
    BigInteger globalDamageMultiplier = calculateGlobalMultiplier();


    boolean debugReset = false;
    boolean enemyTimerActive = false;
    boolean cantBeatBoss = false;
    boolean minionSpellActive = false;
    boolean increaseTapButtonPressed = false;
    boolean increaseDpsButtonPressed = false;

    Timer dpsTimer = new Timer();
    Timer enemyTimer = new Timer();
    Timer bossTimer = new Timer();
    Timer increaseTapTimer = new Timer();
    Timer increaseDpsTimer = new Timer();
    Timer activeAbilityOneTimer = new Timer();
    Timer activeAbilityTwoTimer = new Timer();
    Timer activeAbilityThreeTimer = new Timer();
    Timer activeAbilityFourTimer = new Timer();

    DatabaseHelper databaseHelper;

    ProgressBar healthBar;
    ProgressBar bossTimerProgressBar;

    DIRECTION direction = DIRECTION.RIGHT;
    ABILITY_STATUS abilityOneStatus = ABILITY_STATUS.READY;
    ABILITY_STATUS abilityTwoStatus = ABILITY_STATUS.READY;
    ABILITY_STATUS abilityThreeStatus = ABILITY_STATUS.READY;
    ABILITY_STATUS abilityFourStatus = ABILITY_STATUS.READY;
//--------------------------------------------------------------------------------------------------

//------------------------------------App Flow Stuff------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        SCREEN_HEIGHT = metrics.heightPixels;
        SCREEN_WIDTH = metrics.widthPixels;
        SCREEN_DENSITY = metrics.densityDpi;

        //Will initialize "myFile.txt" if needed
        if (debugReset) {
            resetSaveDataFile();
        }

        initializeDatabase();

        MainFragment mainFrag = new MainFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentFrame, mainFrag).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onStart() {
        super.onStart();

        resetAllTimers();

        loadBoughtUpgrades();

        loadDataFromFile();

        setupTapButton();

        setupDpsButton();

        maximumHealth = calculateMaximumHealth(ENEMY_TYPE.MINION);
        currentHealth = maximumHealth;

        setupProgressBars();

        awardAwayTimeGold();

        updateAllMainFragmentText();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentFrame);
        if ((f instanceof OptionsFragment) || (f instanceof SettingsFragment) || (f instanceof DescriptionFragment))
        {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setEnabled(false);
            }
        }

        startDpsTimer();
    }

    @Override
    public void onStop() {

        resetAllTimers();

        resetMinionImage();

        resetMultipliers();

        saveDataToFile();

        removeAllAddedButtons();

        TextView t = (TextView) findViewById(R.id.bossTimerText);
        t.setText("");

        increaseTapButtonPressed = false;
        increaseDpsButtonPressed = false;

        super.onStop();
    }

    @Override
    public void onDestroy() {
        //
        super.onDestroy();
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getActionMasked();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragmentFrame);

        if ((f instanceof MainFragment) || (f instanceof UpgradesFragment)) {
            switch (eventAction) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    startTapSpellAnimation();
                    break;

                default:
                    break;
            }
        }
        return true;
    }
//--------------------------------------------------------------------------------------------------

//--------------------------------------Button Stuff------------------------------------------------
    public void increaseTap(View view) {
        BigInteger cost = baseTapDamage.multiply(baseTapDamage.multiply(baseTapDamage
                .multiply(new BigInteger("10"))));

        if ((gold.compareTo(cost) == 0) || (gold.compareTo(cost) == 1)) {
            baseTapDamage = baseTapDamage.add(new BigInteger("1"));
            totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
            gold = gold.subtract(cost);

            updateGoldText();

            updateTapText();
        }
    }

    public void increaseDps(View view) {
        BigInteger cost = (baseDps.add(new BigInteger("1")))
                .multiply((baseDps.add(new BigInteger("1"))))
                .multiply((baseDps.add(new BigInteger("1")))).multiply(new BigInteger("10"));

        if ((gold.compareTo(cost) == 0) || (gold.compareTo(cost) == 1)) {
            baseDps = baseDps.add(new BigInteger("1"));
            totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
            gold = gold.subtract(cost);

            updateGoldText();

            updateDpsText();
        }
    }

    public void resetGold(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset Gold");
        b.setMessage("Are you sure you want to reset the total gold?");
        b.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                gold = new BigInteger("0");

                updateGoldText();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Total gold reset...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.show();
    }

    public void resetTapDamage(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset Tap Damage");
        b.setMessage("Are you sure you want to reset tap damage?");
        b.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                baseTapDamage = new BigInteger("1");
                totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);

                updateTapText();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Tap damage reset...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.show();
    }

    public void resetDps(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset DPS");
        b.setMessage("Are you sure you want to reset dps?");
        b.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                baseDps = new BigInteger("0");
                totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);

                updateDpsText();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Dps reset...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.show();
    }

    public void resetUpgrades(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset Upgrades");
        b.setMessage("Are you sure you want to reset all upgrades?");
        b.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.resetAllBought();

                activeAbilityOneTimer = resetTimer(activeAbilityOneTimer);
                removeActiveAbilityOneButton();

                activeAbilityTwoTimer = resetTimer(activeAbilityTwoTimer);
                removeActiveAbilityTwoButton();

                activeAbilityThreeTimer = resetTimer(activeAbilityThreeTimer);
                removeActiveAbilityThreeButton();

                activeAbilityFourTimer = resetTimer(activeAbilityFourTimer);
                removeActiveAbilityFourButton();

                tapDamageMultiplier = new BigInteger("1");
                totalTapDamage = baseTapDamage.multiply(globalDamageMultiplier);
                dpsMultiplier = new BigInteger("1");
                totalDps = baseDps.multiply(globalDamageMultiplier);

                updateTapText();

                updateDpsText();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Upgrades reset...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.show();
    }

    public void resetDistanceLevel(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset Distance Level");
        b.setMessage("Are you sure you want to reset distance level?");
        b.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                distanceLevel = new BigInteger("1");
                maximumHealth = calculateMaximumHealth(ENEMY_TYPE.MINION);
                currentHealth = maximumHealth;
                healthBar.setProgress(bigIntToProgressBar(currentHealth, maximumHealth));

                cantBeatBoss = false;

                removeFightBossButton();
                removeLeaveBossButton();

                TextView t = (TextView) findViewById(R.id.bossTimerText);
                t.setText("");

                bossTimerProgressBar.setProgress(0);
                if (bossTimer != null) {
                    bossTimer = resetTimer(bossTimer);
                }

                updateDistanceText();

                resetMinionImage();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Distance Level reset...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.show();
    }

    public void resetAbilityCooldowns(View view) {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        Button button = (Button) layout.findViewWithTag("Active Ability 1");
        if (button != null)
        {
            removeActiveAbilityOneButton();
            addActiveAbilityOneButton();
        }

        button = (Button) layout.findViewWithTag("Active Ability 2");
        if (button != null)
        {
            removeActiveAbilityTwoButton();
            addActiveAbilityTwoButton();
        }

        button = (Button) layout.findViewWithTag("Active Ability 3");
        if (button != null)
        {
            removeActiveAbilityThreeButton();
            addActiveAbilityThreeButton();
        }

        button = (Button) layout.findViewWithTag("Active Ability 4");
        if (button != null)
        {
            removeActiveAbilityFourButton();
            addActiveAbilityFourButton();
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                "Ability cooldowns reset...", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    public void resetXP(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset XP");
        b.setMessage("Are you sure you want to reset xp?");
        b.setPositiveButton("RESET", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                xp = new BigInteger("0");

                globalDamageMultiplier = calculateGlobalMultiplier();

                totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);

                updateAllMainFragmentText();

                Toast toast = Toast.makeText(getApplicationContext(),
                        "XP reset...", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.show();
    }

    public void resetAll(View view) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Reset All");
        b.setMessage("Are you sure you want to reset everything?");
        b.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                gold = new BigInteger("0");
                baseDps = new BigInteger("0");
                baseTapDamage = new BigInteger("1");
                tapDamageMultiplier = new BigInteger("1");
                dpsMultiplier = new BigInteger("1");
                totalTapDamage = new BigInteger("1");
                totalDps = new BigInteger("0");

                TextView t = (TextView) findViewById(R.id.bossTimerText);
                t.setText("");

                bossTimerProgressBar.setProgress(0);
                bossTimer = resetTimer(bossTimer);
                activeAbilityOneTimer = resetTimer(activeAbilityOneTimer);
                activeAbilityTwoTimer = resetTimer(activeAbilityTwoTimer);
                activeAbilityThreeTimer = resetTimer(activeAbilityThreeTimer);
                activeAbilityFourTimer = resetTimer(activeAbilityFourTimer);

                databaseHelper.resetAllBought();

                cantBeatBoss = false;

                removeFightBossButton();
                removeLeaveBossButton();
                removeActiveAbilityOneButton();
                removeActiveAbilityTwoButton();
                removeActiveAbilityThreeButton();
                removeActiveAbilityFourButton();

                distanceLevel = new BigInteger("1");
                maximumHealth = calculateMaximumHealth(ENEMY_TYPE.MINION);
                currentHealth = maximumHealth;

                healthBar.setProgress(bigIntToProgressBar(currentHealth, maximumHealth));

                xp = new BigInteger("0");

                globalDamageMultiplier = calculateGlobalMultiplier();

                updateAllMainFragmentText();

                Toast toast = Toast.makeText(getApplicationContext(), "Everything reset...",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //Do Nothing
            }
        });
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.show();
    }

    public void resetSaveDataFile() {
        File file = new File(getFilesDir(), "myFile.txt");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null) {
                Calendar time = Calendar.getInstance();
                BigInteger t = new BigInteger(Long.toString(time.getTimeInMillis()));

                fos.write(Integer.toString(0).getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(0).getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(1).getBytes());
                fos.write("\n".getBytes());
                fos.write(t.toString().getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(1).getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(0).getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(0).getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(0).getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(0).getBytes());
                fos.write("\n".getBytes());
                fos.write(Integer.toString(0).getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null) {
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startSettings(View view) {
        SettingsFragment settingsFrag = new SettingsFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.fragmentFrame, settingsFrag);
        t.addToBackStack(null);
        t.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    public void onSettingsExit(View view) {
        //...
        getSupportFragmentManager().popBackStack();
    }

    public void startDescription(View view) {
        DescriptionFragment descriptionFrag = new DescriptionFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.fragmentFrame, descriptionFrag);
        t.addToBackStack(null);
        t.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    public void onDescriptionExit(View view) {
        //...
        getSupportFragmentManager().popBackStack();
    }

    public void startOptions(View view) {
        OptionsFragment optionsFrag = new OptionsFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.fragmentFrame, optionsFrag);
        t.addToBackStack(null);
        t.commit();
        getSupportFragmentManager().executePendingTransactions();

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }

        layout.setAlpha(0.5F);
    }

    public void onOptionsExit(View view) {
        getSupportFragmentManager().popBackStack();

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(true);
        }

        if((abilityOneStatus == ABILITY_STATUS.COOLDOWN) || (abilityOneStatus == ABILITY_STATUS.ACTIVE))
        {
            layout.findViewWithTag("Active Ability 1").setEnabled(false);
        }

        if((abilityTwoStatus == ABILITY_STATUS.COOLDOWN) || (abilityTwoStatus == ABILITY_STATUS.ACTIVE))
        {
            layout.findViewWithTag("Active Ability 2").setEnabled(false);
        }

        if((abilityThreeStatus == ABILITY_STATUS.COOLDOWN) || (abilityThreeStatus == ABILITY_STATUS.ACTIVE))
        {
            layout.findViewWithTag("Active Ability 3").setEnabled(false);
        }

        if((abilityFourStatus == ABILITY_STATUS.COOLDOWN) || (abilityFourStatus == ABILITY_STATUS.ACTIVE))
        {
            layout.findViewWithTag("Active Ability 4").setEnabled(false);
        }

        layout.setAlpha(1F);
    }

    public void startUpgrades(View view) {
        UpgradesFragment upgradesFrag = new UpgradesFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.add(R.id.fragmentFrame, upgradesFrag);
        t.addToBackStack(null);
        t.commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    public void onUpgradesExit(View view) {
        //
        getSupportFragmentManager().popBackStack();

    }

    public void onBuyUpgradeClick(View v) {
        Cursor c = databaseHelper.getUpgrade(v.getId(), "id");
        c.moveToFirst();

        if ((gold.compareTo(customStringToBigInt(c.getString(3))) == 0) ||
                (gold.compareTo(customStringToBigInt(c.getString(3))) == 1)) {
            gold = gold.subtract(customStringToBigInt(c.getString(3)));

            databaseHelper.updateBought(v.getId());

            updateGoldText();

            switch (Integer.parseInt(c.getString(5))) {
                case 1:
                    tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("2"));
                    totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                    updateTapText();
                    break;

                case 2:
                    tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("3"));
                    totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                    updateTapText();
                    break;

                case 3:
                    tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("4"));
                    totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                    updateTapText();
                    break;

                case 4:
                    tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("5"));
                    totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                    updateTapText();
                    break;

                case 5:
                    dpsMultiplier = dpsMultiplier.multiply(new BigInteger("2"));
                    totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                    updateDpsText();
                    break;

                case 6:
                    dpsMultiplier = dpsMultiplier.multiply(new BigInteger("3"));
                    totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                    updateDpsText();
                    break;

                case 7:
                    dpsMultiplier = dpsMultiplier.multiply(new BigInteger("4"));
                    totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                    updateDpsText();
                    break;

                case 8:
                    dpsMultiplier = dpsMultiplier.multiply(new BigInteger("5"));
                    totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                    updateDpsText();
                    break;

                case 9:
                    addActiveAbilityOneButton();
                    break;

                case 10:
                    addActiveAbilityTwoButton();
                    break;

                case 11:
                    addActiveAbilityThreeButton();
                    break;

                case 12:
                    addActiveAbilityFourButton();
                    break;

                default:
                    break;
            }

            ((ViewManager) v.getParent().getParent()).removeView((View) (v.getParent()));
        }

        c.close();
    }

    public void prestige(View v) {
        if(distanceLevel.compareTo(new BigInteger("100")) == 1) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Prestige?");
            b.setMessage("Are you sure you want to prestige? You will lose all progress, but gain XP and IP...");
            b.setPositiveButton("PRESTIGE!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    gold = new BigInteger("0");
                    baseDps = new BigInteger("0");
                    baseTapDamage = new BigInteger("1");
                    tapDamageMultiplier = new BigInteger("1");
                    dpsMultiplier = new BigInteger("1");
                    totalTapDamage = new BigInteger("1");
                    totalDps = new BigInteger("0");

                    TextView t = (TextView) findViewById(R.id.bossTimerText);
                    t.setText("");

                    bossTimerProgressBar.setProgress(0);
                    bossTimer = resetTimer(bossTimer);
                    activeAbilityOneTimer = resetTimer(activeAbilityOneTimer);
                    activeAbilityTwoTimer = resetTimer(activeAbilityTwoTimer);
                    activeAbilityThreeTimer = resetTimer(activeAbilityThreeTimer);
                    activeAbilityFourTimer = resetTimer(activeAbilityFourTimer);

                    databaseHelper.resetAllBought();

                    cantBeatBoss = false;

                    removeFightBossButton();
                    removeLeaveBossButton();
                    removeActiveAbilityOneButton();
                    removeActiveAbilityTwoButton();
                    removeActiveAbilityThreeButton();
                    removeActiveAbilityFourButton();

                    xp = xp.add(distanceLevel);
                    globalDamageMultiplier = calculateGlobalMultiplier();

                    distanceLevel = new BigInteger("1");
                    maximumHealth = calculateMaximumHealth(ENEMY_TYPE.MINION);
                    currentHealth = maximumHealth;

                    healthBar.setProgress(bigIntToProgressBar(currentHealth, maximumHealth));

                    totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                    totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);

                    updateAllMainFragmentText();

                    Toast toast = Toast.makeText(getApplicationContext(), "Prestige Successful!!",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
            });
            b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //Do Nothing
                }
            });
            b.setIcon(android.R.drawable.ic_dialog_alert);
            b.show();
        }

        else
        {
            String k = "Must reach 101 steps to prestige...";
            Toast toast = Toast.makeText(this, k, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }
//--------------------------------------------------------------------------------------------------

//---------------------------------------Timer Stuff------------------------------------------------
    public void startDpsTimer() {
        dpsTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (totalDps.compareTo(new BigInteger("1000000")) == 1)
                        {
                            if (!minionSpellActive)
                            {
                                startMinionSpellAnimation(500);
                            }
                        }
                        else if (totalDps.compareTo(new BigInteger("0")) == 1)
                        {
                            if (!minionSpellActive)
                            {
                                startMinionSpellAnimation(1000);
                            }
                        }
                    }
                });
            }
        }, 10, 10);
    }

    public void enemyKilledTimer(final boolean legit) {
        enemyTimerActive = true;

        if ((enemyIsBoss())) {
            ImageView minionImage = (ImageView) findViewById(R.id.minionImage);
            minionImage.setImageResource(R.drawable.boss_dead);
        } else {
            ImageView minionImage = (ImageView) findViewById(R.id.minionImage);
            minionImage.setImageResource(R.drawable.dead_minion);
        }

        enemyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (legit) {
                            ImageView minionImage = (ImageView) findViewById(R.id.minionImage);
                            minionImage.setImageResource(R.drawable.poof_image);
                        }
                    }
                });
            }
        }, 500);

        enemyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (legit) {
                            gold = gold.add(maximumHealth.multiply(goldMultiplier));
                        }

                        updateGoldText();

                        if (enemyIsBoss()) {
                            bossTimer = resetTimer(bossTimer);

                            cantBeatBoss = false;

                            removeLeaveBossButton();
                        }

                        if (!cantBeatBoss) {
                            distanceLevel = distanceLevel.add(new BigInteger("1"));
                        }

                        if (enemyIsBoss()) {
                            maximumHealth = calculateMaximumHealth(ENEMY_TYPE.BOSS);
                            currentHealth = maximumHealth;
                            bossTimerProgressBar.setProgress(30);

                            TextView t = (TextView) findViewById(R.id.bossTimerText);
                            if(bossTimerProgressBar.getProgress() == 0)
                            {
                                t.setText("");
                            }

                            else {
                                t.setText(String.format(Locale.US, "%d", bossTimerProgressBar.getProgress()));
                            }

                            addLeaveBossButton();

                            bossTimer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run()
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            bossTimerProgressBar.setProgress(bossTimerProgressBar
                                                    .getProgress() - 1);

                                            TextView t = (TextView) findViewById(R.id.bossTimerText);
                                            if(bossTimerProgressBar.getProgress() == 0)
                                            {
                                                t.setText("");
                                            }

                                            else {
                                                t.setText(String.format(Locale.US, "%d", bossTimerProgressBar.getProgress()));
                                            }
                                        }
                                    });
                                }
                            }, 1000, 1000);
                            bossTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            bossTimerProgressBar.setProgress(0);

                                            bossTimer = resetTimer(bossTimer);

                                            cantBeatBoss = true;

                                            resetMinionImage();

                                            addFightBossButton();

                                            removeLeaveBossButton();

                                            distanceLevel = distanceLevel
                                                    .subtract(new BigInteger("1"));
                                            maximumHealth =
                                                    calculateMaximumHealth(ENEMY_TYPE.MINION);
                                            currentHealth = maximumHealth;

                                            updateDistanceText();

                                            TextView t = (TextView) findViewById(R.id.bossTimerText);
                                            t.setText("");
                                        }
                                    });
                                }
                            }, 30000);

                            ImageView minionImage = (ImageView) findViewById(R.id.minionImage);
                            minionImage.setImageResource(R.drawable.boss_alive);
                        } else {
                            maximumHealth = calculateMaximumHealth(ENEMY_TYPE.MINION);
                            currentHealth = maximumHealth;
                            bossTimerProgressBar.setProgress(0);

                            TextView t = (TextView) findViewById(R.id.bossTimerText);
                            t.setText("");

                            resetMinionImage();
                        }

                        updateDistanceText();

                        healthBar.setProgress(bigIntToProgressBar(currentHealth, maximumHealth));

                        updateEnemyHealthText();

                        enemyTimerActive = false;
                        enemyTimer = resetTimer(enemyTimer);
                    }
                });
            }
        }, 1000);
    }

    public Timer resetTimer(Timer t) {
        if (t != null) {
            t.cancel();
            t.purge();
        }

        return new Timer();
    }

    public void resetAllTimers() {
        dpsTimer = resetTimer(dpsTimer);

        enemyTimer = resetTimer(enemyTimer);

        bossTimer = resetTimer(bossTimer);

        increaseTapTimer = resetTimer(increaseTapTimer);

        increaseDpsTimer = resetTimer(increaseDpsTimer);

        activeAbilityOneTimer = resetTimer(activeAbilityOneTimer);

        activeAbilityTwoTimer = resetTimer(activeAbilityTwoTimer);

        activeAbilityThreeTimer = resetTimer(activeAbilityThreeTimer);

        activeAbilityFourTimer = resetTimer(activeAbilityFourTimer);
    }
//--------------------------------------------------------------------------------------------------

//------------------------------Activity / Fragment Interaction Stuff-------------------------------
    public void onSettingsFragmentInteraction(whatToDoEnumSettingsFragment whatToDo) {
        switch(whatToDo)
        {
            case UPDATE_SIZES:
                setupSettingsFragSizes();
                break;
        }
    }

    public void onDescriptionFragmentInteraction(whatToDoEnumDescriptionFragment whatToDo) {
        switch(whatToDo)
        {
            case UPDATE_SIZES:
                setupDescriptionFragSizes();
                break;
        }
    }

    public void onOptionsFragmentInteraction(whatToDoEnumOptionsFragment whatToDo) {
        switch(whatToDo)
        {
            case ON_CLOSE:
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    child.setEnabled(true);
                }

                if((abilityOneStatus == ABILITY_STATUS.COOLDOWN) || (abilityOneStatus == ABILITY_STATUS.ACTIVE))
                {
                    layout.findViewWithTag("Active Ability 1").setEnabled(false);
                }

                if((abilityTwoStatus == ABILITY_STATUS.COOLDOWN) || (abilityTwoStatus == ABILITY_STATUS.ACTIVE))
                {
                    layout.findViewWithTag("Active Ability 2").setEnabled(false);
                }

                if((abilityThreeStatus == ABILITY_STATUS.COOLDOWN) || (abilityThreeStatus == ABILITY_STATUS.ACTIVE))
                {
                    layout.findViewWithTag("Active Ability 3").setEnabled(false);
                }

                if((abilityFourStatus == ABILITY_STATUS.COOLDOWN) || (abilityFourStatus == ABILITY_STATUS.ACTIVE))
                {
                    layout.findViewWithTag("Active Ability 4").setEnabled(false);
                }

                layout.setAlpha(1F);
                break;
            case UPDATE_SIZES:
                setupOptionsFragSizes();
                break;
            default:
                break;
        }
    }

    public void onUpgradesFragmentInteraction(whatToDoEnumUpgradesFragment whatToDo) {
        switch (whatToDo) {
            case LOAD_DATABASE:
                LinearLayout outerLayout =
                        (LinearLayout) findViewById(R.id.upgradesFragOuterLinearLayout);

                if (outerLayout.getChildCount() == 0) {
                    Cursor cs = databaseHelper.getAllUpgrades();

                    int margin = scaleHeightToScreen(5);

                    LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, scaleHeightToScreen(50));

                    l.setMargins(margin, margin, margin, margin);

                    LinearLayout.LayoutParams l2 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    l2.setMargins(margin, margin, margin, margin);

                    RelativeLayout.LayoutParams l3 = new RelativeLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);

                    l3.setMargins(margin, margin, margin, margin);

                    RelativeLayout.LayoutParams l4 = new RelativeLayout.LayoutParams(
                            scaleWidthToScreen(pxToDp(400, MY_SCREEN_DENSITY)), LinearLayout.LayoutParams.WRAP_CONTENT);

                    cs.moveToFirst();

                    while (!cs.isAfterLast()) {
                        int bought = Integer.parseInt(cs.getString(4));

                        if (bought == 0) {
                            RelativeLayout innerLayout = new RelativeLayout(this);
                            innerLayout.setBackgroundResource(R.drawable.background_border);
                            innerLayout.setPadding(margin, margin, margin, margin);
                            innerLayout.setLayoutParams(l);

                            TextView txtView = new TextView(this);

                            String name = cs.getString(1);

                            txtView.setText(name);
                            txtView.setTextSize(pxToDp(scaleHeightToScreen(15), SCREEN_DENSITY));
                            txtView.setLayoutParams(l2);

                            innerLayout.addView(txtView);

                            l3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            l3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                            TextView txtView2 = new TextView(this);

                            String description = cs.getString(2);

                            txtView2.setText(description);
                            txtView2.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));
                            txtView2.setLayoutParams(l3);

                            innerLayout.addView(txtView2);

                            l4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            l4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                            Button button = new Button(this);

                            BigInteger cost = customStringToBigInt(cs.getString(3));

                            button.setText(bigIntToCustomString(cost));
                            button.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));
                            button.setAllCaps(false);
                            button.setLayoutParams(l4);
                            button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                                    R.color.buttonBackgroundColor));
                            button.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                    R.color.buttonTextColor));
                            button.setId(Integer.parseInt(cs.getString(0)));
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    onBuyUpgradeClick(v);
                                }
                            });

                            innerLayout.addView(button);

                            outerLayout.addView(innerLayout);
                        }

                        cs.moveToNext();
                    }

                    cs.close();
                }
                break;

            case UPDATE_SIZES:
                setupUpgradesFragSizes();
                break;

            default:

                break;
        }
    }

    public void onMainFragmentInteraction(whatToDoEnumMainFragment whatToDo) {
        switch (whatToDo) {
            case SET_TEXT:
                updateAllMainFragmentText();
                break;
            case UPDATE_SIZES:
                setupMainFragSizes();
                break;
        }
    }
//--------------------------------------------------------------------------------------------------

//----------------------------------Random Helper Functions-----------------------------------------
    public String bigIntToCustomString(BigInteger b) {
        String returnString;
        String intString = b.toString();
        int length = intString.length();

        if (length < 4) {
            returnString = b.toString();
        } else if (length < 7) {
            if (length == 6) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + intString.charAt(2)
                        + "." + intString.charAt(3) + intString.charAt(4) + intString.charAt(5);
            } else if (length == 5) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + "."
                        + intString.charAt(2) + intString.charAt(3) + intString.charAt(4);
            } else {
                returnString = "" + intString.charAt(0) + "." + intString.charAt(1)
                        + intString.charAt(2) + intString.charAt(3);
            }

            returnString += "K";
        } else if (length < 10) {
            if (length == 9) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + intString.charAt(2)
                        + "." + intString.charAt(3) + intString.charAt(4) + intString.charAt(5);
            } else if (length == 8) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + "."
                        + intString.charAt(2) + intString.charAt(3) + intString.charAt(4);
            } else {
                returnString = "" + intString.charAt(0) + "." + intString.charAt(1)
                        + intString.charAt(2) + intString.charAt(3);
            }

            returnString += "M";
        } else if (length < 13) {
            if (length == 12) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + intString.charAt(2)
                        + "." + intString.charAt(3) + intString.charAt(4) + intString.charAt(5);
            } else if (length == 11) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + "."
                        + intString.charAt(2) + intString.charAt(3) + intString.charAt(4);
            } else {
                returnString = "" + intString.charAt(0) + "." + intString.charAt(1)
                        + intString.charAt(2) + intString.charAt(3);
            }

            returnString += "B";
        } else if (length < 16) {
            if (length == 15) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + intString.charAt(2)
                        + "." + intString.charAt(3) + intString.charAt(4) + intString.charAt(5);
            } else if (length == 14) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + "."
                        + intString.charAt(2) + intString.charAt(3) + intString.charAt(4);
            } else {
                returnString = "" + intString.charAt(0) + "." + intString.charAt(1)
                        + intString.charAt(2) + intString.charAt(3);
            }

            returnString += "T";
        } else {
            int lengthCalc = (length - 1) / 3;

            if (length == ((lengthCalc * 3) + 3)) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + intString.charAt(2)
                        + "." + intString.charAt(3) + intString.charAt(4) + intString.charAt(5);
            } else if (length == ((lengthCalc * 3) + 2)) {
                returnString = "" + intString.charAt(0) + intString.charAt(1) + "."
                        + intString.charAt(2) + intString.charAt(3) + intString.charAt(4);
            } else {
                returnString = "" + intString.charAt(0) + "." + intString.charAt(1)
                        + intString.charAt(2) + intString.charAt(3);
            }

            returnString = returnString + ((char) (lengthCalc + 60)) + ((char) (lengthCalc + 92));
        }

        return returnString;
    }

    public BigInteger customStringToBigInt(String s) {
        String builderString = "";

        if (s.charAt(1) == '.') {
            builderString = builderString + s.charAt(0) + s.charAt(2) + s.charAt(3)
                    + s.charAt(4);
        } else if (s.charAt(2) == '.') {
            builderString = builderString + s.charAt(0) + s.charAt(1) + s.charAt(3)
                    + s.charAt(4) + s.charAt(5);
        } else if (s.charAt(3) == '.') {
            builderString = builderString + s.charAt(0) + s.charAt(1) + s.charAt(2)
                    + s.charAt(4) + s.charAt(5) + s.charAt(6);
        } else {
            builderString = s;
        }

        if (s.charAt((s.length() - 1)) == 'M') {
            builderString = builderString + "000";
        } else if (s.charAt((s.length() - 1)) == 'B') {
            builderString = builderString + "000000";
        } else if (s.charAt((s.length() - 1)) == 'T') {
            builderString = builderString + "000000000";
        } else if (s.charAt((s.length() - 1)) != 'K') {
            builderString = builderString + "000000000000";

            char lastChar = s.charAt((s.length() - 1));

            for (char testChar = 'a'; testChar < lastChar; testChar++) {
                builderString = builderString + "000";
            }
        }

        return new BigInteger(builderString);
    }

    public String bigIntToTimeString(BigInteger seconds) {
        String returnString;

        BigInteger minutes = seconds.divide(new BigInteger("60"));
        seconds = seconds.remainder(new BigInteger("60"));

        BigInteger hours = minutes.divide(new BigInteger("60"));
        minutes = minutes.remainder(new BigInteger("60"));

        BigInteger days = hours.divide(new BigInteger("24"));
        hours = hours.remainder(new BigInteger("24"));

        if (days.compareTo(new BigInteger("0")) == 1) {
            returnString = days.toString() + " days, "
                    + hours.toString() + " hours, "
                    + minutes.toString() + " minutes, "
                    + seconds.toString() + " seconds";
        } else if (hours.compareTo(new BigInteger("0")) == 1) {
            returnString = hours.toString() + " hours, "
                    + minutes.toString() + " minutes, "
                    + seconds.toString() + " seconds";
        } else if (minutes.compareTo(new BigInteger("0")) == 1) {
            returnString = minutes.toString() + " minutes, "
                    + seconds.toString() + " seconds";
        } else {
            returnString = seconds.toString() + " seconds";
        }

        return returnString;
    }

    public int bigIntToProgressBar(BigInteger current, BigInteger maximum) {
        return current.multiply(new BigInteger("1000000")).divide(maximum).intValue();
    }

    public void updateDistanceText() {
        TextView txtView = (TextView) findViewById(R.id.distanceText);
        String s2 = "Walked " + bigIntToCustomString(distanceLevel) + " steps...";
        txtView.setText(s2);
    }

    public void updateEnemyHealthText() {
        TextView txtView = (TextView) findViewById(R.id.healthText);
        txtView.setText(bigIntToCustomString(currentHealth));
    }

    public void updateGoldText() {
        TextView txtView = (TextView) findViewById(R.id.goldText);
        txtView.setText(bigIntToCustomString(gold));
    }

    public void updateTapText() {
        TextView txtView = (TextView) findViewById(R.id.tapDamageText);
        String s = "Current Tap Damage: ";
        s += bigIntToCustomString(totalTapDamage);
        txtView.setText(s);

        s = "TAP +" + bigIntToCustomString(tapDamageMultiplier.multiply(globalDamageMultiplier)) + ": \n";
        BigInteger tempCalc = baseTapDamage.multiply(baseTapDamage.multiply(baseTapDamage
                .multiply(new BigInteger("10"))));
        s += bigIntToCustomString(tempCalc);
        Button buttonView = (Button) findViewById(R.id.increaseTapButton);
        buttonView.setText(s);
    }

    public void updateDpsText() {
        TextView txtView = (TextView) findViewById(R.id.dpsText);
        String s = "Current Dps: ";
        s += bigIntToCustomString(totalDps);
        txtView.setText(s);

        s = "DPS +" + bigIntToCustomString(dpsMultiplier.multiply(globalDamageMultiplier)) + ": \n";
        BigInteger tempCalc2 = (baseDps.add(new BigInteger("1"))).multiply((baseDps
                .add(new BigInteger("1")))).multiply((baseDps.add(new BigInteger("1"))))
                .multiply(new BigInteger("10"));
        s += bigIntToCustomString(tempCalc2);
        Button buttonView = (Button) findViewById(R.id.increaseDpsButton);
        buttonView.setText(s);
    }

    public void updateGlobalDamageLvlText() {
        TextView t = (TextView) findViewById(R.id.globalMultiplierText);

        String s = "Global\nMultiplier :\nx" + globalDamageMultiplier.toString();

        t.setText(s);
    }

    public void updateAllMainFragmentText() {
        updateDistanceText();

        updateEnemyHealthText();

        updateGoldText();

        updateTapText();

        updateDpsText();

        updateGlobalDamageLvlText();
    }

    public void addFightBossButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
        Button button = new Button(getApplicationContext());
        button.setText(R.string.bossButton);
        button.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));
        button.setAllCaps(false);
        button.setTag("FightBossButton");

        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                scaleWidthToScreen(pxToDp(200, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(200, MY_SCREEN_DENSITY)));

        int margin = scaleHeightToScreen(5);

        l.setMargins(margin, margin, margin, margin);

        l.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        l.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        button.setLayoutParams(l);
        button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.buttonBackgroundColor));
        button.setTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.buttonTextColor));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cantBeatBoss = false;
                ((ViewManager) v.getParent()).removeView(v);

                enemyKilledTimer(false);
            }
        });

        layout.addView(button);
    }

    public void removeFightBossButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
        Button buttonToRemove = (Button) layout.findViewWithTag("FightBossButton");
        if (buttonToRemove != null) {
            ((ViewManager) buttonToRemove.getParent()).removeView(buttonToRemove);
        }
    }

    public void addLeaveBossButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
        Button button = new Button(getApplicationContext());
        button.setText(R.string.leaveBossButton);
        button.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));
        button.setAllCaps(false);
        button.setTag("LeaveBossButton");

        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                scaleWidthToScreen(pxToDp(200, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(200, MY_SCREEN_DENSITY)));

        int margin = scaleHeightToScreen(5);

        l.setMargins(margin, margin, margin, margin);

        l.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        l.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        button.setLayoutParams(l);
        button.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                R.color.buttonBackgroundColor));
        button.setTextColor(ContextCompat.getColor(getApplicationContext(),
                R.color.buttonTextColor));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewManager) v.getParent()).removeView(v);

                bossTimerProgressBar.setProgress(0);
                bossTimer = resetTimer(bossTimer);

                TextView t = (TextView) findViewById(R.id.bossTimerText);
                t.setText("");

                cantBeatBoss = true;

                resetMinionImage();

                addFightBossButton();

                removeLeaveBossButton();

                distanceLevel = distanceLevel.subtract(new BigInteger("1"));
                maximumHealth = calculateMaximumHealth(ENEMY_TYPE.MINION);
                currentHealth = maximumHealth;

                updateDistanceText();
            }
        });

        layout.addView(button);
    }

    public void removeLeaveBossButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        Button buttonToRemove = (Button) layout.findViewWithTag("LeaveBossButton");
        if (buttonToRemove != null) {
            ((ViewManager) buttonToRemove.getParent()).removeView(buttonToRemove);
        }
    }

    public void removeActiveAbilityOneButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        Button buttonToRemove = (Button) layout.findViewWithTag("Active Ability 1");
        if (buttonToRemove != null) {
            ((ViewManager) buttonToRemove.getParent()).removeView(buttonToRemove);
        }

        ProgressBar progress = (ProgressBar) layout.findViewWithTag("Ability 1 Progress");
        if (progress != null) {
            ((ViewManager) progress.getParent()).removeView(progress);
        }

        activeAbilityOneTimer = resetTimer(activeAbilityOneTimer);

        if(abilityOneStatus == ABILITY_STATUS.ACTIVE)
        {
            tapDamageMultiplier = tapDamageMultiplier
                    .divide(new BigInteger("5"));
            totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);

            updateTapText();
        }

        abilityOneStatus = ABILITY_STATUS.READY;
    }

    public void removeActiveAbilityTwoButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        Button buttonToRemove = (Button) layout.findViewWithTag("Active Ability 2");
        if (buttonToRemove != null) {
            ((ViewManager) buttonToRemove.getParent()).removeView(buttonToRemove);
        }

        ProgressBar progress = (ProgressBar) layout.findViewWithTag("Ability 2 Progress");
        if (progress != null) {
            ((ViewManager) progress.getParent()).removeView(progress);
        }

        activeAbilityTwoTimer = resetTimer(activeAbilityTwoTimer);

        if(abilityTwoStatus == ABILITY_STATUS.ACTIVE)
        {
            dpsMultiplier = dpsMultiplier
                    .divide(new BigInteger("5"));
            totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);

            updateDpsText();
        }

        abilityTwoStatus = ABILITY_STATUS.READY;
    }

    public void removeActiveAbilityThreeButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        Button buttonToRemove = (Button) layout.findViewWithTag("Active Ability 3");
        if (buttonToRemove != null) {
            ((ViewManager) buttonToRemove.getParent()).removeView(buttonToRemove);
        }

        ProgressBar progress = (ProgressBar) layout.findViewWithTag("Ability 3 Progress");
        if (progress != null) {
            ((ViewManager) progress.getParent()).removeView(progress);
        }

        activeAbilityThreeTimer = resetTimer(activeAbilityThreeTimer);

        if(abilityThreeStatus == ABILITY_STATUS.ACTIVE)
        {
            goldMultiplier = goldMultiplier
                    .divide(new BigInteger("5"));
        }

        abilityThreeStatus = ABILITY_STATUS.READY;
    }

    public void removeActiveAbilityFourButton() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        Button buttonToRemove = (Button) layout.findViewWithTag("Active Ability 4");
        if (buttonToRemove != null) {
            ((ViewManager) buttonToRemove.getParent()).removeView(buttonToRemove);
        }

        ProgressBar progress = (ProgressBar) layout.findViewWithTag("Ability 4 Progress");
        if (progress != null) {
            ((ViewManager) progress.getParent()).removeView(progress);
        }

        activeAbilityFourTimer = resetTimer(activeAbilityFourTimer);

        abilityFourStatus = ABILITY_STATUS.READY;
    }

    public void removeAllAddedButtons() {
        removeLeaveBossButton();
        removeFightBossButton();
        removeActiveAbilityOneButton();
        removeActiveAbilityTwoButton();
        removeActiveAbilityThreeButton();
        removeActiveAbilityFourButton();
    }

    public boolean enemyIsBoss() {
        return (distanceLevel.remainder(new BigInteger("10"))).equals(new BigInteger("0"));
    }

    public BigInteger calculateMaximumHealth(ENEMY_TYPE type) {
        if (type == ENEMY_TYPE.MINION) {
            return distanceLevel.divide(new BigInteger("10")).add(new BigInteger("1"))
                    .pow(distanceLevel.divide(new BigInteger("10")).add(new BigInteger("1"))
                            .intValue());
        }

        if (type == ENEMY_TYPE.BOSS) {
            return distanceLevel.divide(new BigInteger("10")).pow(distanceLevel
                    .divide(new BigInteger("10")).intValue()).multiply(new BigInteger("10"));
        } else {
            return null;
        }
    }

    public boolean healthIsLessThanZero() {
        return (currentHealth.compareTo(new BigInteger("0")) == -1) ||
                (currentHealth.compareTo(new BigInteger("0")) == 0);
    }

    public void saveDataToFile() {
        Calendar timeOnExit = Calendar.getInstance();
        BigInteger tOE = new BigInteger(Long.toString(timeOnExit.getTimeInMillis()));

        File file = new File(getFilesDir(), "myFile.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null) {
                fos.write(gold.toString().getBytes());
                fos.write("\n".getBytes());
                fos.write(baseDps.toString().getBytes());
                fos.write("\n".getBytes());
                fos.write(baseTapDamage.toString().getBytes());
                fos.write("\n".getBytes());
                fos.write(tOE.toString().getBytes());
                fos.write("\n".getBytes());

                if (!cantBeatBoss) {
                    fos.write(distanceLevel.toString().getBytes());
                } else {
                    fos.write(distanceLevel.add(new BigInteger("1")).toString().getBytes());
                }
                fos.write("\n".getBytes());

                RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

                ProgressBar bar = (ProgressBar) layout.findViewWithTag("Ability 1 Progress");
                if(bar != null)
                {
                    String s = String.format(Locale.US, "%d", getAbilityCooldown(1));
                    fos.write(s.getBytes());
                }
                else
                {
                    fos.write("0".getBytes());
                }
                fos.write("\n".getBytes());

                bar = (ProgressBar) layout.findViewWithTag("Ability 2 Progress");
                if(bar != null)
                {
                    String s = String.format(Locale.US, "%d", getAbilityCooldown(2));
                    fos.write(s.getBytes());
                }
                else
                {
                    fos.write("0".getBytes());
                }
                fos.write("\n".getBytes());

                bar = (ProgressBar) layout.findViewWithTag("Ability 3 Progress");
                if(bar != null)
                {
                    String s = String.format(Locale.US, "%d", getAbilityCooldown(3));
                    fos.write(s.getBytes());
                }
                else
                {
                    fos.write("0".getBytes());
                }
                fos.write("\n".getBytes());

                bar = (ProgressBar) layout.findViewWithTag("Ability 4 Progress");
                if(bar != null)
                {
                    String s = String.format(Locale.US, "%d", getAbilityCooldown(4));
                    fos.write(s.getBytes());
                }
                else
                {
                    fos.write("0".getBytes());
                }
                fos.write("\n".getBytes());

                fos.write(xp.toString().getBytes());
                fos.write("\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null) {
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetMinionImage() {
        ImageView minionImage = (ImageView) findViewById(R.id.minionImage);
        minionImage.setImageResource(R.drawable.alive_minion);
    }

    public void loadBoughtUpgrades() {
        Cursor cs = databaseHelper.getAllUpgrades();

        cs.moveToFirst();

        while (!cs.isAfterLast()) {
            int bought = Integer.parseInt(cs.getString(4));

            if (bought == 1) {
                switch (Integer.parseInt(cs.getString(5))) {
                    case 1:
                        tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("2"));
                        totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 2:
                        tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("3"));
                        totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 3:
                        tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("4"));
                        totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 4:
                        tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("5"));
                        totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 5:
                        dpsMultiplier = dpsMultiplier.multiply(new BigInteger("2"));
                        totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 6:
                        dpsMultiplier = dpsMultiplier.multiply(new BigInteger("3"));
                        totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 7:
                        dpsMultiplier = dpsMultiplier.multiply(new BigInteger("4"));
                        totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 8:
                        dpsMultiplier = dpsMultiplier.multiply(new BigInteger("5"));
                        totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);
                        break;

                    case 9:
                        addActiveAbilityOneButton();
                        break;

                    case 10:
                        addActiveAbilityTwoButton();
                        break;

                    case 11:
                        addActiveAbilityThreeButton();
                        break;

                    case 12:
                        addActiveAbilityFourButton();
                        break;

                    default:
                        break;
                }
            }

            cs.moveToNext();
        }
    }

    public void loadDataFromFile() {
        Scanner scanner;
        File file = new File(getFilesDir(), "myFile.txt");
        try {
            scanner = new Scanner(file);
            String s = scanner.nextLine();
            gold = new BigInteger(s);
            s = scanner.nextLine();
            baseDps = new BigInteger(s);
            s = scanner.nextLine();
            baseTapDamage = new BigInteger(s);
            s = scanner.nextLine();
            timeOfLastExit = new BigInteger(s);
            s = scanner.nextLine();
            distanceLevel = new BigInteger(s);

            RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

            s = scanner.nextLine();
            ProgressBar bar = (ProgressBar) layout.findViewWithTag("Ability 1 Progress");
            if(bar != null)
            {
                Calendar timeOnLoad = Calendar.getInstance();
                BigInteger tOL = new BigInteger(Long.toString(timeOnLoad.getTimeInMillis()));

                BigInteger timeGone = tOL.subtract(timeOfLastExit);

                int cooldown = Integer.parseInt(s) - timeGone.intValue();
                if(cooldown > 0)
                {
                    applyAbilityCooldown(1, cooldown);
                }
            }

            s = scanner.nextLine();
            bar = (ProgressBar) layout.findViewWithTag("Ability 2 Progress");
            if(bar != null)
            {
                Calendar timeOnLoad = Calendar.getInstance();
                BigInteger tOL = new BigInteger(Long.toString(timeOnLoad.getTimeInMillis()));

                BigInteger timeGone = tOL.subtract(timeOfLastExit);

                int cooldown = Integer.parseInt(s) - timeGone.intValue();
                if(cooldown > 0)
                {
                    applyAbilityCooldown(2, cooldown);
                }
            }

            s = scanner.nextLine();
            bar = (ProgressBar) layout.findViewWithTag("Ability 3 Progress");
            if(bar != null)
            {
                Calendar timeOnLoad = Calendar.getInstance();
                BigInteger tOL = new BigInteger(Long.toString(timeOnLoad.getTimeInMillis()));

                BigInteger timeGone = tOL.subtract(timeOfLastExit);

                int cooldown = Integer.parseInt(s) - timeGone.intValue();
                if(cooldown > 0)
                {
                    applyAbilityCooldown(3, cooldown);
                }
            }

            s = scanner.nextLine();
            bar = (ProgressBar) layout.findViewWithTag("Ability 4 Progress");
            if(bar != null)
            {
                Calendar timeOnLoad = Calendar.getInstance();
                BigInteger tOL = new BigInteger(Long.toString(timeOnLoad.getTimeInMillis()));

                BigInteger timeGone = tOL.subtract(timeOfLastExit);

                int cooldown = Integer.parseInt(s) - timeGone.intValue();
                if(cooldown > 0)
                {
                    applyAbilityCooldown(4, cooldown);
                }
            }

            s = scanner.nextLine();
            xp = new BigInteger(s);

            globalDamageMultiplier = calculateGlobalMultiplier();

            cantBeatBoss = false;

            if ((enemyIsBoss())) {
                distanceLevel = distanceLevel.subtract(new BigInteger("1"));
                cantBeatBoss = true;

                addFightBossButton();

                removeLeaveBossButton();

                updateDistanceText();
            }

            totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);
            totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setupTapButton() {
        Button button = (Button) findViewById(R.id.increaseTapButton);
        button.setTextSize(pxToDp(scaleHeightToScreen(10), SCREEN_DENSITY));
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        increaseTapButtonPressed = true;
                        BigInteger cost = baseTapDamage.multiply(baseTapDamage
                                .multiply(baseTapDamage
                                        .multiply(new BigInteger("10"))));
                        if ((gold.compareTo(cost) == 0) ||
                                (gold.compareTo(cost) == 1)) {
                            increaseTap(v);
                            scheduleTapPurchaseTimer(100, v);
                        }

                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        if (increaseTapTimer != null) {
                            increaseTapButtonPressed = false;
                            increaseTapTimer = resetTimer(increaseTapTimer);
                        }
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (!stillPressedInButton(event, v)) {
                            if (increaseTapTimer != null) {
                                increaseTapButtonPressed = false;
                                increaseTapTimer = resetTimer(increaseTapTimer);
                            }
                        }
                        break;
                    }

                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void scheduleTapPurchaseTimer(final int delay, final View v) {
        increaseTapTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BigInteger cost = baseTapDamage.multiply(baseTapDamage
                                .multiply(baseTapDamage
                                        .multiply(new BigInteger("10"))));
                        if (((gold.compareTo(cost) == 0) ||
                                (gold.compareTo(cost) == 1)) && increaseTapButtonPressed)
                        {
                            increaseTap(v);

                            increaseTapTimer = resetTimer(increaseTapTimer);

                            if(delay == 1)
                            {
                                scheduleTapPurchaseTimer(delay, v);
                            }

                            else if(delay < 11)
                            {
                                scheduleTapPurchaseTimer(delay - 1, v);
                            }

                            else
                            {
                                scheduleTapPurchaseTimer(delay - 5, v);
                            }
                        }
                        else
                        {
                            increaseTapTimer = resetTimer(increaseTapTimer);
                        }
                    }
                });
            }
        }, delay);
    }

    public void setupDpsButton() {
        Button button = (Button) findViewById(R.id.increaseDpsButton);
        button.setTextSize(pxToDp(scaleHeightToScreen(10), SCREEN_DENSITY));
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        increaseDpsButtonPressed = true;

                        BigInteger cost = baseDps.multiply(baseDps.multiply(baseDps
                                .multiply(new BigInteger("10"))));
                        if ((gold.compareTo(cost) == 0) ||
                                (gold.compareTo(cost) == 1))
                        {
                            increaseDps(v);
                            scheduleDpsPurchaseTimer(100, v);
                        }

                        break;
                    }

                    case MotionEvent.ACTION_UP: {
                        if (increaseDpsTimer != null) {
                            increaseDpsButtonPressed = false;
                            increaseDpsTimer = resetTimer(increaseDpsTimer);
                        }
                        break;
                    }

                    case MotionEvent.ACTION_MOVE: {
                        if (!stillPressedInButton(event, v)) {
                            if (increaseDpsTimer != null) {
                                increaseDpsButtonPressed = false;
                                increaseDpsTimer = resetTimer(increaseDpsTimer);
                            }
                        }
                        break;
                    }

                    default:
                        break;
                }
                return true;
            }
        });
    }

    public void scheduleDpsPurchaseTimer(final int delay, final View v) {
        increaseDpsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BigInteger cost = baseDps.multiply(baseDps.multiply(baseDps
                                .multiply(new BigInteger("10"))));
                        if (((gold.compareTo(cost) == 0) ||
                                (gold.compareTo(cost) == 1)) && increaseDpsButtonPressed)
                        {
                            increaseDps(v);

                            increaseDpsTimer = resetTimer(increaseDpsTimer);

                            if(delay == 1)
                            {
                                scheduleDpsPurchaseTimer(delay, v);
                            }

                            else if(delay < 11)
                            {
                                scheduleDpsPurchaseTimer(delay - 1, v);
                            }
                            else
                            {
                                scheduleDpsPurchaseTimer(delay - 5, v);
                            }
                        }
                        else
                        {
                            increaseDpsTimer = resetTimer(increaseDpsTimer);
                        }
                    }
                });
            }
        }, delay);
    }

    public void awardAwayTimeGold() {
        Calendar timeOnLoad = Calendar.getInstance();
        BigInteger tOL = new BigInteger(Long.toString(timeOnLoad.getTimeInMillis()));

        BigInteger timeGone = tOL.subtract(timeOfLastExit);
        timeGone = timeGone.divide(new BigInteger("1000"));

        String k = "Gone for ";
        k += bigIntToTimeString(timeGone);
        k += "...";
        Toast toast = Toast.makeText(this, k, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();

        k = "Defeated  ";
        k += bigIntToCustomString(totalDps.multiply(timeGone).divide(maximumHealth));
        k += " minions...\n";
        k += "Gained ";
        k += bigIntToCustomString(totalDps.multiply(timeGone).divide(maximumHealth)
                .multiply(maximumHealth));
        k += " gold...";
        toast = Toast.makeText(this, k, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();

        gold = gold.add(totalDps.multiply(timeGone).divide(maximumHealth).multiply(maximumHealth));
    }

    public void setupProgressBars() {
        healthBar = (ProgressBar) findViewById(R.id.progressBar);
        healthBar.setMax(1000000);
        healthBar.setProgress(bigIntToProgressBar(currentHealth, maximumHealth));

        bossTimerProgressBar = (ProgressBar) findViewById(R.id.bossTimer);
        bossTimerProgressBar.setMax(30);
        bossTimerProgressBar.setProgress(0);
    }

    public void initializeDatabase() {
        databaseHelper = new DatabaseHelper(this);

        try {
            databaseHelper.create();
        } catch (IOException error) {
            throw new Error("databaseTest() error... " + error);
        }

        databaseHelper.open();
    }

    public boolean stillPressedInButton(MotionEvent event, View v) {
        float x = event.getRawX();
        float y = event.getRawY();
        float buttonLeft = v.getLeft();
        float buttonRight = v.getRight();
        float buttonTop = v.getTop();
        float buttonBottom = v.getBottom();

        return !((x < buttonLeft) || (x > buttonRight) || (y < buttonTop) ||
                (y > buttonBottom));

    }

    public void startMinionSpellAnimation(final int castRate) {
        minionSpellActive = true;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
        final ImageView minionSpell = new ImageView(this);
        minionSpell.setImageResource(R.drawable.minion_spell);

        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                scaleWidthToScreen(25),
                scaleHeightToScreen(25));

        int margin = scaleHeightToScreen(5);

        l.setMargins(margin, margin, margin, margin);

        l.addRule(RelativeLayout.CENTER_IN_PARENT);

        minionSpell.setLayoutParams(l);

        layout.addView(minionSpell);

        //Only works for my phone, needs to use relative coordinates
        TranslateAnimation animation = new TranslateAnimation(
                scaleWidthToScreen(pxToDp(150, MY_SCREEN_DENSITY)),
                scaleWidthToScreen(pxToDp(15, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(-75, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(-350, MY_SCREEN_DENSITY)));

        animation.setDuration(castRate);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //Don't care...
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!enemyTimerActive) {
                    if (totalDps.compareTo(new BigInteger("1000000")) == 1) {
                        //Loss of damage if dps was less than 1M before
                        currentHealth = currentHealth
                                .subtract(totalDps.divide(new BigInteger("2")));
                    } else {
                        currentHealth = currentHealth.subtract(totalDps);
                    }

                    if (healthIsLessThanZero()) {
                        currentHealth = new BigInteger("0");
                        enemyKilledTimer(true);
                    }

                    healthBar.setProgress(bigIntToProgressBar(currentHealth,
                            maximumHealth));

                    updateEnemyHealthText();
                }

                ((ViewManager) minionSpell.getParent()).removeView(minionSpell);
                minionSpellActive = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //Don't care...
            }
        });

        minionSpell.startAnimation(animation);
    }

    public void resetMultipliers() {
        tapDamageMultiplier = new BigInteger("1");
        dpsMultiplier = new BigInteger("1");
    }

    public void startTapSpellAnimation() {
        final Path path = new Path();
        path.moveTo(
                scaleWidthToScreen(pxToDp(490, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(1300, MY_SCREEN_DENSITY)));

        if(direction == DIRECTION.RIGHT)
        {
            direction = DIRECTION.LEFT;
            path.quadTo(
                    scaleWidthToScreen(pxToDp(975, MY_SCREEN_DENSITY)),
                    scaleHeightToScreen(pxToDp(1000, MY_SCREEN_DENSITY)),
                    scaleWidthToScreen(pxToDp(500, MY_SCREEN_DENSITY)),
                    scaleHeightToScreen(pxToDp(575, MY_SCREEN_DENSITY)));
        }

        else if(direction == DIRECTION.LEFT)
        {
            direction = DIRECTION.RIGHT;
            path.quadTo(
                    scaleWidthToScreen(pxToDp(-25, MY_SCREEN_DENSITY)),
                    scaleHeightToScreen(pxToDp(1000, MY_SCREEN_DENSITY)),
                    scaleWidthToScreen(pxToDp(500, MY_SCREEN_DENSITY)),
                    scaleHeightToScreen(pxToDp(575, MY_SCREEN_DENSITY)));
        }

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
        final ImageView tapSpell = new ImageView(this);
        tapSpell.setImageResource(R.drawable.tap_spell);

        layout.addView(tapSpell);

        tapSpell.getLayoutParams().width = scaleWidthToScreen(40);
        tapSpell.getLayoutParams().height = scaleHeightToScreen(40);


        ValueAnimator pathAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);

        pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float[] point = new float[2];

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = animation.getAnimatedFraction();
                PathMeasure pathMeasure = new PathMeasure(path, false);
                pathMeasure.getPosTan(pathMeasure.getLength() * val, point, null);
                tapSpell.setX(point[0]);
                tapSpell.setY(point[1]);
            }
        });

        pathAnimator.setDuration(1000);

        pathAnimator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                ((ViewManager) tapSpell.getParent()).removeView(tapSpell);

                if (!enemyTimerActive) {
                    currentHealth = currentHealth.subtract(totalTapDamage);
                    if (healthIsLessThanZero()) {
                        currentHealth = new BigInteger("0");
                        enemyKilledTimer(true);
                    }

                    healthBar.setProgress(bigIntToProgressBar(currentHealth, maximumHealth));
                    updateEnemyHealthText();
                }
            }
        });
        pathAnimator.start();
    }
//Clean below
    public void addActiveAbilityOneButton() {
        final int duration = ABILITY_ONE_DURATION;
        final int cooldown = ABILITY_ONE_COOLDOWN;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                scaleWidthToScreen(pxToDp(150, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(150, MY_SCREEN_DENSITY)));

        int margin = scaleHeightToScreen(5);

        l.setMargins(margin, margin, margin, margin);

        l.addRule(RelativeLayout.LEFT_OF, R.id.blankAbilitySpacing2);
        l.addRule(RelativeLayout.ABOVE, R.id.tapDamageText);

        final ProgressBar abilityOneBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        abilityOneBar.setMax(duration/1000);
        abilityOneBar.setProgress(duration/1000);
        abilityOneBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));
        abilityOneBar.setLayoutParams(l);
        abilityOneBar.setTag("Ability 1 Progress");

        Button b = new Button(getApplicationContext());
        b.setText(R.string.activeAbilityOneText);
        b.setTextSize(pxToDp(scaleHeightToScreen(10), SCREEN_DENSITY));
        b.setAllCaps(false);
        b.setTag("Active Ability 1");
        b.setPadding(scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)));

        b.setLayoutParams(l);
        b.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        b.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String s = "Tap X5\n" + "In Use\n";
                s += abilityOneBar.getProgress();

                v.setEnabled(false);
                ((Button) v).setText(s);
                tapDamageMultiplier = tapDamageMultiplier.multiply(new BigInteger("5"));
                totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);

                abilityOneBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_active));

                abilityOneStatus = ABILITY_STATUS.ACTIVE;

                updateTapText();

                activeAbilityOneTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityOneStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityOneBar.setProgress(abilityOneBar.getProgress() - 1);

                                    String s = "Tap X5\n" + "In Use\n";
                                    s += abilityOneBar.getProgress();
                                    ((Button) v).setText(s);
                                }

                                else if (abilityOneStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityOneBar.setProgress(abilityOneBar.getProgress() + 1);

                                    String s = "Tap X5\n" + "C.D.\n";
                                    s += ((cooldown/1000)-abilityOneBar.getProgress());
                                    ((Button) v).setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityOneTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tapDamageMultiplier = tapDamageMultiplier
                                        .divide(new BigInteger("5"));
                                totalTapDamage = baseTapDamage.multiply(tapDamageMultiplier).multiply(globalDamageMultiplier);

                                abilityOneStatus = ABILITY_STATUS.COOLDOWN;

                                abilityOneBar.setMax(cooldown/1000);
                                abilityOneBar.setProgress(0);

                                String s = "Tap X5\n" + "C.D.\n";
                                s += ((cooldown/1000)-abilityOneBar.getProgress());
                                ((Button) v).setText(s);

                                abilityOneBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));

                                updateTapText();
                            }
                        });
                    }
                }, duration);

                activeAbilityOneTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                ((Button) v).setText(R.string.activeAbilityOneText);

                                abilityOneBar.setMax(duration/1000);
                                abilityOneBar.setProgress(duration/1000);

                                abilityOneBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityOneStatus = ABILITY_STATUS.READY;

                                activeAbilityOneTimer = resetTimer(activeAbilityOneTimer);
                            }
                        });
                    }
                }, duration+cooldown);
            }

        });

        layout.addView(abilityOneBar);
        layout.addView(b);
    }

    public void addActiveAbilityTwoButton() {
        final int duration = ABILITY_TWO_DURATION;
        final int cooldown = ABILITY_TWO_COOLDOWN;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                scaleWidthToScreen(pxToDp(150, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(150, MY_SCREEN_DENSITY)));

        int margin = scaleHeightToScreen(5);

        l.setMargins(margin, margin, margin, margin);

        l.addRule(RelativeLayout.LEFT_OF, R.id.blankAbilitySpacing1);
        l.addRule(RelativeLayout.ABOVE, R.id.tapDamageText);

        final ProgressBar abilityTwoBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        abilityTwoBar.setMax(duration/1000);
        abilityTwoBar.setProgress(duration/1000);
        abilityTwoBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));
        abilityTwoBar.setLayoutParams(l);
        abilityTwoBar.setTag("Ability 2 Progress");

        Button b = new Button(getApplicationContext());
        b.setText(R.string.activeAbilityTwoText);
        b.setTextSize(pxToDp(scaleHeightToScreen(10), SCREEN_DENSITY));
        b.setAllCaps(false);
        b.setTag("Active Ability 2");
        b.setPadding(scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)));

        b.setLayoutParams(l);
        b.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        b.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String s = "Dps X5\n" + "In Use\n";
                s += abilityTwoBar.getProgress();

                v.setEnabled(false);
                ((Button) v).setText(s);
                dpsMultiplier = dpsMultiplier.multiply(new BigInteger("5"));
                totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);

                abilityTwoBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_active));

                abilityTwoStatus = ABILITY_STATUS.ACTIVE;

                updateDpsText();

                activeAbilityTwoTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityTwoStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityTwoBar.setProgress(abilityTwoBar.getProgress() - 1);

                                    String s = "Dps X5\n" + "In Use\n";
                                    s += abilityTwoBar.getProgress();
                                    ((Button) v).setText(s);
                                }

                                else if (abilityTwoStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityTwoBar.setProgress(abilityTwoBar.getProgress() + 1);

                                    String s = "Dps X5\n" + "C.D.\n";
                                    s += ((cooldown/1000)-abilityTwoBar.getProgress());
                                    ((Button) v).setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityTwoTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dpsMultiplier = dpsMultiplier
                                        .divide(new BigInteger("5"));
                                totalDps = baseDps.multiply(dpsMultiplier).multiply(globalDamageMultiplier);

                                abilityTwoStatus = ABILITY_STATUS.COOLDOWN;

                                abilityTwoBar.setMax(cooldown/1000);
                                abilityTwoBar.setProgress(0);

                                String s = "Dps X5\n" + "C.D.\n";
                                s += ((cooldown/1000)-abilityTwoBar.getProgress());
                                ((Button) v).setText(s);

                                abilityTwoBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));

                                updateDpsText();
                            }
                        });
                    }
                }, duration);

                activeAbilityTwoTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                ((Button) v).setText(R.string.activeAbilityTwoText);

                                abilityTwoBar.setMax(duration/1000);
                                abilityTwoBar.setProgress(duration/1000);

                                abilityTwoBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityTwoStatus = ABILITY_STATUS.READY;

                                activeAbilityTwoTimer = resetTimer(activeAbilityTwoTimer);
                            }
                        });
                    }
                }, duration+cooldown);
            }

        });

        layout.addView(abilityTwoBar);
        layout.addView(b);
    }

    public void addActiveAbilityThreeButton() {
        final int duration = ABILITY_THREE_DURATION;
        final int cooldown = ABILITY_THREE_COOLDOWN;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                scaleWidthToScreen(pxToDp(150, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(150, MY_SCREEN_DENSITY)));

        int margin = scaleHeightToScreen(5);

        l.setMargins(margin, margin, margin, margin);

        l.addRule(RelativeLayout.RIGHT_OF, R.id.blankAbilitySpacing1);
        l.addRule(RelativeLayout.ABOVE, R.id.tapDamageText);

        final ProgressBar abilityThreeBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        abilityThreeBar.setMax(duration/1000);
        abilityThreeBar.setProgress(duration/1000);
        abilityThreeBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));
        abilityThreeBar.setLayoutParams(l);
        abilityThreeBar.setTag("Ability 3 Progress");

        Button b = new Button(getApplicationContext());
        b.setText(R.string.activeAbilityThreeText);
        b.setTextSize(pxToDp(scaleHeightToScreen(10), SCREEN_DENSITY));
        b.setAllCaps(false);
        b.setTag("Active Ability 3");
        b.setPadding(scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)));

        b.setLayoutParams(l);
        b.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        b.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String s = "Gold X5\n" + "In Use\n";
                s += abilityThreeBar.getProgress();

                v.setEnabled(false);
                ((Button) v).setText(s);
                goldMultiplier = goldMultiplier.multiply(new BigInteger("5"));

                abilityThreeBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_active));

                abilityThreeStatus = ABILITY_STATUS.ACTIVE;

                activeAbilityThreeTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityThreeStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityThreeBar.setProgress(abilityThreeBar.getProgress() - 1);

                                    String s = "Gold X5\n" + "In Use\n";
                                    s += abilityThreeBar.getProgress();
                                    ((Button) v).setText(s);
                                }

                                else if (abilityThreeStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityThreeBar.setProgress(abilityThreeBar.getProgress() + 1);

                                    String s = "Gold X5\n" + "C.D.\n";
                                    s += ((cooldown/1000)-abilityThreeBar.getProgress());
                                    ((Button) v).setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityThreeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                goldMultiplier = goldMultiplier
                                        .divide(new BigInteger("5"));

                                abilityThreeStatus = ABILITY_STATUS.COOLDOWN;

                                abilityThreeBar.setMax(cooldown/1000);
                                abilityThreeBar.setProgress(0);

                                String s = "Gold X5\n" + "C.D.\n";
                                s += ((cooldown/1000)-abilityThreeBar.getProgress());
                                ((Button) v).setText(s);

                                abilityThreeBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));
                            }
                        });
                    }
                }, duration);

                activeAbilityThreeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                ((Button) v).setText(R.string.activeAbilityThreeText);

                                abilityThreeBar.setMax(duration/1000);
                                abilityThreeBar.setProgress(duration/1000);

                                abilityThreeBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityThreeStatus = ABILITY_STATUS.READY;

                                activeAbilityThreeTimer = resetTimer(activeAbilityThreeTimer);
                            }
                        });
                    }
                }, duration+cooldown);
            }

        });

        layout.addView(abilityThreeBar);
        layout.addView(b);
    }

    public void addActiveAbilityFourButton() {
        final int duration = ABILITY_FOUR_DURATION;
        final int cooldown = ABILITY_FOUR_COOLDOWN;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(
                scaleWidthToScreen(pxToDp(150, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(150, MY_SCREEN_DENSITY)));

        int margin = scaleHeightToScreen(5);

        l.setMargins(margin, margin, margin, margin);

        l.addRule(RelativeLayout.RIGHT_OF, R.id.blankAbilitySpacing2);
        l.addRule(RelativeLayout.ABOVE, R.id.tapDamageText);

        final ProgressBar abilityFourBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        abilityFourBar.setMax(duration/1000);
        abilityFourBar.setProgress(duration/1000);
        abilityFourBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));
        abilityFourBar.setLayoutParams(l);
        abilityFourBar.setTag("Ability 4 Progress");

        Button b = new Button(getApplicationContext());
        b.setText(R.string.activeAbilityFourText);
        b.setTextSize(pxToDp(scaleHeightToScreen(10), SCREEN_DENSITY));
        b.setAllCaps(false);
        b.setTag("Active Ability 4");
        b.setPadding(scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleWidthToScreen(pxToDp(5, MY_SCREEN_DENSITY)),
                scaleHeightToScreen(pxToDp(5, MY_SCREEN_DENSITY)));

        b.setLayoutParams(l);
        b.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        b.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.buttonTextColor));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String s = "Auto-Tap\n" + "In Use\n";
                s += abilityFourBar.getProgress();

                v.setEnabled(false);
                ((Button) v).setText(s);

                abilityFourBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_active));

                abilityFourStatus = ABILITY_STATUS.ACTIVE;

                activeAbilityFourTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityFourStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    startTapSpellAnimation();
                                }
                            }
                        });
                    }
                }, 0, 100);

                activeAbilityFourTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityFourStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityFourBar.setProgress(abilityFourBar.getProgress() - 1);

                                    String s = "Auto-Tap\n" + "In Use\n";
                                    s += abilityFourBar.getProgress();
                                    ((Button) v).setText(s);
                                }

                                else if (abilityFourStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityFourBar.setProgress(abilityFourBar.getProgress() + 1);

                                    String s = "Auto-Tap\n" + "C.D.\n";
                                    s += ((cooldown/1000)-abilityFourBar.getProgress());
                                    ((Button) v).setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityFourTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                abilityFourStatus = ABILITY_STATUS.COOLDOWN;

                                abilityFourBar.setMax(cooldown/1000);
                                abilityFourBar.setProgress(0);

                                String s = "Auto-Tap\n" + "C.D.\n";
                                s += ((cooldown/1000)-abilityFourBar.getProgress());
                                ((Button) v).setText(s);

                                abilityFourBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));
                            }
                        });
                    }
                }, duration);

                activeAbilityFourTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                ((Button) v).setText(R.string.activeAbilityFourText);

                                abilityFourBar.setMax(duration/1000);
                                abilityFourBar.setProgress(duration/1000);

                                abilityFourBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityFourStatus = ABILITY_STATUS.READY;

                                activeAbilityFourTimer = resetTimer(activeAbilityFourTimer);
                            }
                        });
                    }
                }, duration+cooldown);
            }

        });

        layout.addView(abilityFourBar);
        layout.addView(b);
    }

    public int getAbilityCooldown(int abilityNumber) {
        int cooldown = 0;

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);

        switch(abilityNumber)
        {
            case 1:
            {
                if (abilityOneStatus == ABILITY_STATUS.READY)
                {
                    cooldown = 0;
                }
                else if (abilityOneStatus == ABILITY_STATUS.ACTIVE)
                {
                    cooldown = ABILITY_ONE_COOLDOWN;
                }
                else if (abilityOneStatus == ABILITY_STATUS.COOLDOWN)
                {
                    ProgressBar bar = (ProgressBar) layout.findViewWithTag("Ability 1 Progress");
                    cooldown = ABILITY_ONE_COOLDOWN - (bar.getProgress()*1000);
                }
                break;
            }

            case 2:
            {
                if (abilityTwoStatus == ABILITY_STATUS.READY)
                {
                    cooldown = 0;
                }
                else if (abilityTwoStatus == ABILITY_STATUS.ACTIVE)
                {
                    cooldown = ABILITY_TWO_COOLDOWN;
                }
                else if (abilityTwoStatus == ABILITY_STATUS.COOLDOWN)
                {
                    ProgressBar bar = (ProgressBar) layout.findViewWithTag("Ability 2 Progress");
                    cooldown = ABILITY_TWO_COOLDOWN - (bar.getProgress()*1000);
                }
                break;
            }

            case 3:
            {
                if (abilityThreeStatus == ABILITY_STATUS.READY)
                {
                    cooldown = 0;
                }
                else if (abilityThreeStatus == ABILITY_STATUS.ACTIVE)
                {
                    cooldown = ABILITY_THREE_COOLDOWN;
                }
                else if (abilityThreeStatus == ABILITY_STATUS.COOLDOWN)
                {
                    ProgressBar bar = (ProgressBar) layout.findViewWithTag("Ability 3 Progress");
                    cooldown = ABILITY_THREE_COOLDOWN - (bar.getProgress()*1000);
                }
                break;
            }

            case 4:
            {
                if (abilityFourStatus == ABILITY_STATUS.READY)
                {
                    cooldown = 0;
                }
                else if (abilityFourStatus == ABILITY_STATUS.ACTIVE)
                {
                    cooldown = ABILITY_FOUR_COOLDOWN;
                }
                else if (abilityFourStatus == ABILITY_STATUS.COOLDOWN)
                {
                    ProgressBar bar = (ProgressBar) layout.findViewWithTag("Ability 4 Progress");
                    cooldown = ABILITY_FOUR_COOLDOWN - (bar.getProgress()*1000);
                }
                break;
            }

            default:
                break;
        }

        return cooldown;
    }

    public void applyAbilityCooldown(int ability, int cooldown) {
        switch(ability)
        {
            case 1:
            {
                abilityOneStatus = ABILITY_STATUS.COOLDOWN;

                RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
                final ProgressBar abilityOneBar = (ProgressBar) layout.findViewWithTag("Ability 1 Progress");

                abilityOneBar.setMax(ABILITY_ONE_COOLDOWN/1000);
                abilityOneBar.setProgress((ABILITY_ONE_COOLDOWN/1000)-(cooldown/1000));

                String s = "Tap X5\n" + "C.D.\n";
                s += ((ABILITY_ONE_COOLDOWN/1000)-abilityOneBar.getProgress());
                final Button v = (Button) layout.findViewWithTag("Active Ability 1");
                v.setText(s);
                v.setEnabled(false);

                abilityOneBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));

                activeAbilityOneTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityOneStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityOneBar.setProgress(abilityOneBar.getProgress() - 1);

                                    String s = "Tap X5\n" + "In Use\n";
                                    s += abilityOneBar.getProgress();
                                    v.setText(s);
                                }

                                else if (abilityOneStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityOneBar.setProgress(abilityOneBar.getProgress() + 1);

                                    String s = "Tap X5\n" + "C.D.\n";
                                    s += ((ABILITY_ONE_COOLDOWN/1000)-abilityOneBar.getProgress());
                                    v.setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityOneTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                v.setText(R.string.activeAbilityOneText);

                                abilityOneBar.setMax(ABILITY_ONE_DURATION/1000);
                                abilityOneBar.setProgress(ABILITY_ONE_DURATION/1000);

                                abilityOneBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityOneStatus = ABILITY_STATUS.READY;

                                activeAbilityOneTimer = resetTimer(activeAbilityOneTimer);
                            }
                        });
                    }
                }, cooldown);
                break;
            }

            case 2:
            {
                abilityTwoStatus = ABILITY_STATUS.COOLDOWN;

                RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
                final ProgressBar abilityTwoBar = (ProgressBar) layout.findViewWithTag("Ability 2 Progress");

                abilityTwoBar.setMax(ABILITY_TWO_COOLDOWN/1000);
                abilityTwoBar.setProgress((ABILITY_TWO_COOLDOWN/1000)-(cooldown/1000));

                String s = "Dps X5\n" + "C.D.\n";
                s += ((ABILITY_TWO_COOLDOWN/1000)-abilityTwoBar.getProgress());
                final Button v = (Button) layout.findViewWithTag("Active Ability 2");
                v.setText(s);
                v.setEnabled(false);

                abilityTwoBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));

                activeAbilityTwoTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityTwoStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityTwoBar.setProgress(abilityTwoBar.getProgress() - 1);

                                    String s = "Dps X5\n" + "In Use\n";
                                    s += abilityTwoBar.getProgress();
                                    v.setText(s);
                                }

                                else if (abilityTwoStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityTwoBar.setProgress(abilityTwoBar.getProgress() + 1);

                                    String s = "Dps X5\n" + "C.D.\n";
                                    s += ((ABILITY_TWO_COOLDOWN/1000)-abilityTwoBar.getProgress());
                                    v.setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityTwoTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                v.setText(R.string.activeAbilityTwoText);

                                abilityTwoBar.setMax(ABILITY_TWO_DURATION/1000);
                                abilityTwoBar.setProgress(ABILITY_TWO_DURATION/1000);

                                abilityTwoBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityTwoStatus = ABILITY_STATUS.READY;

                                activeAbilityTwoTimer = resetTimer(activeAbilityTwoTimer);
                            }
                        });
                    }
                }, cooldown);
                break;
            }

            case 3:
            {
                abilityThreeStatus = ABILITY_STATUS.COOLDOWN;

                RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
                final ProgressBar abilityThreeBar = (ProgressBar) layout.findViewWithTag("Ability 3 Progress");

                abilityThreeBar.setMax(ABILITY_THREE_COOLDOWN/1000);
                abilityThreeBar.setProgress((ABILITY_THREE_COOLDOWN/1000)-(cooldown/1000));

                String s = "Gold X5\n" + "C.D.\n";
                s += ((ABILITY_THREE_COOLDOWN/1000)-abilityThreeBar.getProgress());
                final Button v = (Button) layout.findViewWithTag("Active Ability 3");
                v.setText(s);
                v.setEnabled(false);

                abilityThreeBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));

                activeAbilityThreeTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityThreeStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityThreeBar.setProgress(abilityThreeBar.getProgress() - 1);

                                    String s = "Gold X5\n" + "In Use\n";
                                    s += abilityThreeBar.getProgress();
                                    v.setText(s);
                                }

                                else if (abilityThreeStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityThreeBar.setProgress(abilityThreeBar.getProgress() + 1);

                                    String s = "Gold X5\n" + "C.D.\n";
                                    s += ((ABILITY_THREE_COOLDOWN/1000)-abilityThreeBar.getProgress());
                                    v.setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityThreeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                v.setText(R.string.activeAbilityThreeText);

                                abilityThreeBar.setMax(ABILITY_THREE_DURATION/1000);
                                abilityThreeBar.setProgress(ABILITY_THREE_DURATION/1000);

                                abilityThreeBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityThreeStatus = ABILITY_STATUS.READY;

                                activeAbilityThreeTimer = resetTimer(activeAbilityThreeTimer);
                            }
                        });
                    }
                }, cooldown);
                break;
            }

            case 4:
            {
                abilityFourStatus = ABILITY_STATUS.COOLDOWN;

                RelativeLayout layout = (RelativeLayout) findViewById(R.id.mainFragLayout);
                final ProgressBar abilityFourBar = (ProgressBar) layout.findViewWithTag("Ability 4 Progress");

                abilityFourBar.setMax(ABILITY_FOUR_COOLDOWN/1000);
                abilityFourBar.setProgress((ABILITY_FOUR_COOLDOWN/1000)-(cooldown/1000));

                String s = "Auto-Tap\n" + "C.D.\n";
                s += ((ABILITY_FOUR_COOLDOWN/1000)-abilityFourBar.getProgress());
                final Button v = (Button) layout.findViewWithTag("Active Ability 4");
                v.setText(s);
                v.setEnabled(false);

                abilityFourBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_on_cooldown));

                activeAbilityFourTimer.scheduleAtFixedRate(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(abilityFourStatus == ABILITY_STATUS.ACTIVE)
                                {
                                    abilityFourBar.setProgress(abilityFourBar.getProgress() - 1);

                                    String s = "Auto-Tap\n" + "In Use\n";
                                    s += abilityFourBar.getProgress();
                                    v.setText(s);
                                }

                                else if (abilityFourStatus == ABILITY_STATUS.COOLDOWN)
                                {
                                    abilityFourBar.setProgress(abilityFourBar.getProgress() + 1);

                                    String s = "Auto-Tap\n" + "C.D.\n";
                                    s += ((ABILITY_FOUR_COOLDOWN/1000)-abilityFourBar.getProgress());
                                    v.setText(s);
                                }
                            }
                        });
                    }
                }, 1000, 1000);

                activeAbilityFourTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                v.setEnabled(true);
                                v.setText(R.string.activeAbilityFourText);

                                abilityFourBar.setMax(ABILITY_FOUR_DURATION/1000);
                                abilityFourBar.setProgress(ABILITY_FOUR_DURATION/1000);

                                abilityFourBar.setProgressDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.abilty_ready));

                                abilityFourStatus = ABILITY_STATUS.READY;

                                activeAbilityFourTimer = resetTimer(activeAbilityFourTimer);
                            }
                        });
                    }
                }, cooldown);
                break;
            }

            default:
                break;
        }
    }

    public BigInteger calculateGlobalMultiplier() {
        BigInteger retVal = new BigInteger("2");

        retVal = retVal.pow((xp.divide(new BigInteger("100"))).intValue());

        return retVal;
    }

    public int dpToPx(float dp, float density) {
        //
        return (int) (dp / 160.0 * density);
    }

    public int pxToDp(float px, float density) {
        //
        return (int) (px * 160.0 / density);
    }

    public int scaleWidthToScreen(float dp) {
        //
        return dpToPx((dp / pxToDp(MY_SCREEN_WIDTH, MY_SCREEN_DENSITY) * pxToDp(SCREEN_WIDTH, SCREEN_DENSITY)), SCREEN_DENSITY);
    }

    public int scaleHeightToScreen(float dp) {
        //
        return dpToPx((dp / pxToDp(MY_SCREEN_HEIGHT, MY_SCREEN_DENSITY) * pxToDp(SCREEN_HEIGHT, SCREEN_DENSITY)), SCREEN_DENSITY);
    }

    public void setupMainFragSizes() {
        TextView t = (TextView) findViewById(R.id.globalMultiplierText);
        t.getLayoutParams().width = scaleWidthToScreen(65);
        t.getLayoutParams().height = scaleHeightToScreen(65);
        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);
        t.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));

        t = (TextView) findViewById(R.id.distanceText);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);
        t.setTextSize(pxToDp(scaleHeightToScreen(20), SCREEN_DENSITY));

        ProgressBar p = (ProgressBar) findViewById(R.id.progressBar);
        p.getLayoutParams().height = scaleHeightToScreen(25);
        p.getLayoutParams().width = scaleWidthToScreen(200);
        l = (RelativeLayout.LayoutParams) p.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        p.setLayoutParams(l);

        p = (ProgressBar) findViewById(R.id.bossTimer);
        p.getLayoutParams().height = scaleHeightToScreen(10);
        p.getLayoutParams().width = scaleWidthToScreen(150);
        l = (RelativeLayout.LayoutParams) p.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        p.setLayoutParams(l);

        t = (TextView) findViewById(R.id.healthText);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);
        t.setTextSize(pxToDp(scaleHeightToScreen(18), SCREEN_DENSITY));

        t = (TextView) findViewById(R.id.emptyView1);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);

        t = (TextView) findViewById(R.id.goldText);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);
        t.setTextSize(pxToDp(scaleHeightToScreen(20), SCREEN_DENSITY));

        t = (TextView) findViewById(R.id.bossTimerText);
        t.getLayoutParams().height = scaleHeightToScreen(14);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(0), scaleHeightToScreen(0), scaleWidthToScreen(5), scaleHeightToScreen(0));
        t.setLayoutParams(l);
        t.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));

        ImageView i = (ImageView) findViewById(R.id.goldIcon);
        i.getLayoutParams().width = scaleWidthToScreen(36);
        i.getLayoutParams().height = scaleHeightToScreen(27);
        l = (RelativeLayout.LayoutParams) i.getLayoutParams();
        l.setMargins(scaleWidthToScreen(0), scaleHeightToScreen(5), scaleWidthToScreen(0), scaleHeightToScreen(0));
        i.setLayoutParams(l);

        i = (ImageView) findViewById(R.id.minionImage);
        i.getLayoutParams().width = scaleWidthToScreen(125);
        i.getLayoutParams().height = scaleHeightToScreen(125);
        l = (RelativeLayout.LayoutParams) i.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        i.setLayoutParams(l);

        Button b = (Button) findViewById(R.id.increaseTapButton);
        b.getLayoutParams().width = scaleWidthToScreen(100);
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));
        l = (RelativeLayout.LayoutParams) b.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));

        b = (Button) findViewById(R.id.increaseDpsButton);
        b.getLayoutParams().width = scaleWidthToScreen(100);
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.setTextSize(pxToDp(scaleHeightToScreen(12), SCREEN_DENSITY));
        l = (RelativeLayout.LayoutParams) b.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));

        t = (TextView) findViewById(R.id.dpsText);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);
        t.setTextSize(pxToDp(scaleHeightToScreen(15), SCREEN_DENSITY));

        t = (TextView) findViewById(R.id.tapDamageText);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);
        t.setTextSize(pxToDp(scaleHeightToScreen(15), SCREEN_DENSITY));

        t = (TextView) findViewById(R.id.blankAbilitySpacing1);
        t.getLayoutParams().width = scaleWidthToScreen(1);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(1), scaleHeightToScreen(1), scaleWidthToScreen(1), scaleHeightToScreen(1));
        t.setLayoutParams(l);

        t = (TextView) findViewById(R.id.blankAbilitySpacing2);
        t.getLayoutParams().width = scaleWidthToScreen(130);
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(1), scaleHeightToScreen(1), scaleWidthToScreen(1), scaleHeightToScreen(1));
        t.setLayoutParams(l);

        i = (ImageView) findViewById(R.id.allyMinionImage);
        i.getLayoutParams().width = scaleWidthToScreen(125);
        i.getLayoutParams().height = scaleHeightToScreen(125);
        l = (RelativeLayout.LayoutParams) i.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        i.setLayoutParams(l);

        ImageButton ib = (ImageButton) findViewById(R.id.optionsButton);
        ib.getLayoutParams().width = scaleWidthToScreen(45);
        ib.getLayoutParams().height = scaleHeightToScreen(45);
        l = (RelativeLayout.LayoutParams) ib.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        ib.setLayoutParams(l);

        ib = (ImageButton) findViewById(R.id.menuButton);
        ib.getLayoutParams().width = scaleWidthToScreen(45);
        ib.getLayoutParams().height = scaleHeightToScreen(45);
        l = (RelativeLayout.LayoutParams) ib.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        ib.setLayoutParams(l);
    }

    public void setupDescriptionFragSizes() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.descriptionLayout);
        rl.getLayoutParams().width = scaleWidthToScreen(275);
        rl.getLayoutParams().height = scaleHeightToScreen(500);

        ImageButton ib = (ImageButton) findViewById(R.id.exitDescriptionButton);
        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) ib.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        ib.setLayoutParams(l);

        TextView t = (TextView) findViewById(R.id.descriptionTitle);
        t.setTextSize(pxToDp(scaleHeightToScreen(40), SCREEN_DENSITY));
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);

        ScrollView s = (ScrollView) findViewById(R.id.descriptionScrollview);
        s.getLayoutParams().height = scaleHeightToScreen(500);
        s.setPadding(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        l = (RelativeLayout.LayoutParams) s.getLayoutParams();
        l.setMargins(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        s.setLayoutParams(l);

        LinearLayout ll = (LinearLayout) findViewById(R.id.descriptionInnerLayout);
        FrameLayout.LayoutParams l2 = (FrameLayout.LayoutParams) ll.getLayoutParams();
        l2.setMargins(scaleWidthToScreen(0), scaleHeightToScreen(0), scaleWidthToScreen(0), scaleHeightToScreen(0));
        ll.setLayoutParams(l2);
        ll.setPadding(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));

        t = (TextView) findViewById(R.id.gameDescription);
        t.setTextSize(pxToDp(scaleHeightToScreen(15), SCREEN_DENSITY));
        LinearLayout.LayoutParams l3 = (LinearLayout.LayoutParams) t.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l3);
        t.setPadding(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
    }

    public void setupOptionsFragSizes() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.optionsLayout);
        rl.getLayoutParams().height = scaleHeightToScreen(500);
        rl.getLayoutParams().width = scaleWidthToScreen(275);

        ImageButton ib = (ImageButton) findViewById(R.id.exitOptionsButton);
        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) ib.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        ib.setLayoutParams(l);

        TextView t = (TextView) findViewById(R.id.optionsTitle);
        t.setTextSize(pxToDp(scaleHeightToScreen(40), SCREEN_DENSITY));
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);

        Button b = (Button) findViewById(R.id.gameDescriptionButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        l = (RelativeLayout.LayoutParams) b.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));

        b = (Button) findViewById(R.id.prestigeButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        l = (RelativeLayout.LayoutParams) b.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));

        b = (Button) findViewById(R.id.openSettingsButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        l = (RelativeLayout.LayoutParams) b.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
    }

    public void setupUpgradesFragSizes() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.upgradesFragLayout);
        rl.getLayoutParams().height = scaleHeightToScreen(300);

        ImageButton ib = (ImageButton) findViewById(R.id.exitUpgradesButton);
        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) ib.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        ib.setLayoutParams(l);

        TextView t = (TextView) findViewById(R.id.upgradesTitle);
        t.setTextSize(pxToDp(scaleHeightToScreen(30), SCREEN_DENSITY));
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        t.setLayoutParams(l);

        ScrollView s = (ScrollView) findViewById(R.id.upgradesScrollview);
        s.getLayoutParams().height = scaleHeightToScreen(250);
        s.setPadding(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        l = (RelativeLayout.LayoutParams) s.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        s.setLayoutParams(l);

        LinearLayout ll = (LinearLayout) findViewById(R.id.upgradesFragOuterLinearLayout);
        FrameLayout.LayoutParams l2 = (FrameLayout.LayoutParams) ll.getLayoutParams();
        l2.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        ll.setLayoutParams(l2);
        ll.setPadding(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
    }

    public void setupSettingsFragSizes() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.settingsLayout);
        rl.getLayoutParams().height = scaleHeightToScreen(500);
        rl.getLayoutParams().width = scaleWidthToScreen(275);

        ImageButton ib = (ImageButton) findViewById(R.id.exitSettingsButton);
        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams) ib.getLayoutParams();
        l.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        ib.setLayoutParams(l);

        TextView t = (TextView) findViewById(R.id.settingsTitle);
        t.setTextSize(pxToDp(scaleHeightToScreen(40), SCREEN_DENSITY));
        l = (RelativeLayout.LayoutParams) t.getLayoutParams();
        l.setMargins(scaleWidthToScreen(0), scaleHeightToScreen(0), scaleWidthToScreen(0), scaleHeightToScreen(0));
        t.setLayoutParams(l);
        t.setPadding(scaleWidthToScreen(0), scaleHeightToScreen(0), scaleWidthToScreen(0), scaleHeightToScreen(0));

        ScrollView s = (ScrollView) findViewById(R.id.settingsScrollview);
        s.getLayoutParams().height = scaleHeightToScreen(375);
        s.getLayoutParams().width = scaleWidthToScreen(200);
        l = (RelativeLayout.LayoutParams) s.getLayoutParams();
        l.setMargins(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        s.setLayoutParams(l);
        s.setPadding(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));

        LinearLayout ll = (LinearLayout) findViewById(R.id.settingsInnerLayout);
        ll.setPadding(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        FrameLayout.LayoutParams l2 = (FrameLayout.LayoutParams) ll.getLayoutParams();
        l2.setMargins(scaleWidthToScreen(0), scaleHeightToScreen(0), scaleWidthToScreen(0), scaleHeightToScreen(0));
        ll.setLayoutParams(l2);

        Button b = (Button) findViewById(R.id.resetAllButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        LinearLayout.LayoutParams l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);

        b = (Button) findViewById(R.id.resetAbilityCooldownButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);

        b = (Button) findViewById(R.id.resetUpgradesButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);

        b = (Button) findViewById(R.id.resetXPButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);

        b = (Button) findViewById(R.id.resetDistanceLevelButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);

        b = (Button) findViewById(R.id.resetDpsButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);

        b = (Button) findViewById(R.id.resetTapDamageButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);

        b = (Button) findViewById(R.id.resetGoldButton);
        b.setTextSize(pxToDp(scaleHeightToScreen(14), SCREEN_DENSITY));
        b.getLayoutParams().height = scaleHeightToScreen(45);
        b.getLayoutParams().width = scaleWidthToScreen(160);
        b.setPadding(scaleWidthToScreen(10), scaleHeightToScreen(10), scaleWidthToScreen(10), scaleHeightToScreen(10));
        l3 = (LinearLayout.LayoutParams) b.getLayoutParams();
        l3.setMargins(scaleWidthToScreen(5), scaleHeightToScreen(5), scaleWidthToScreen(5), scaleHeightToScreen(5));
        b.setLayoutParams(l3);
    }
//--------------------------------------------------------------------------------------------------
}
