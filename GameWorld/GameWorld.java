package GameWorld;

import AI.AIHandler;
import Graphics.GameWindow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by joseph on 09/02/2017.
 */
public class GameWorld {
    protected int HARDWALL = 0;
    protected int SOFTWALL = 1;
    public int gridSize; // in 1 dimension
    protected int amountPlayers;
    protected int amountOfRounds = 0;
    protected Boolean windowBool;
    protected GameWindow window;
    private boolean PRINT = false;
    protected WorldPosition[][] positions;
    protected ArrayList<AIHandler> ai;
    protected ArrayList<BomberMan> bomberManList;
    protected ArrayList<Bomb> activeBombList;
    protected ArrayList<Bomb> explodedBombList;
    private int win = 0;

    private int roundTime;

    public GameWorld(int gridSize, int amountOfPlayers, Boolean windowBool) {
        this.gridSize = gridSize;
        this.amountPlayers = amountOfPlayers;
        bomberManList = new ArrayList<>();
        activeBombList = new ArrayList<>();
        explodedBombList = new ArrayList<>();
        InitWorld();
        this.windowBool = windowBool;
        if (windowBool) window = new GameWindow(this);


    }

    public void SetAi(ArrayList<AIHandler> ai) {
        this.ai = ai;
    }

    public void InitWorld() {
        bomberManList.clear();
        activeBombList.clear();
        explodedBombList.clear();
        amountOfRounds = 0;
        positions = new WorldPosition[gridSize][gridSize];
        //init the grid
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                positions[x][y] = new WorldPosition(x, y, SOFTWALL);
                if (x % 2 == 1) positions[x][y] = new WorldPosition(x, y, (y + 1) % 2);
                if (x == 0 || x == gridSize - 1) positions[x][y] = new WorldPosition(x, y, SOFTWALL);
                if (y == 0 || y == gridSize - 1) positions[x][y] = new WorldPosition(x, y, SOFTWALL);
            }

        }

        // init the players
        // 4 is max amount of players

        int y = 0;
        int x = 0;
        int bomberManId = 1;
        if (amountPlayers > 4) amountPlayers = 4;
        if (amountPlayers < 1) amountPlayers = 1;
        for (int idx = 0; idx < amountPlayers && idx < 4; idx++) {
            for (int temp = x - 1; temp <= x + 1; temp++) {
                if (temp >= 0 && temp < gridSize) {
                    positions[temp][y].setType(2);
                }
            }

            for (int temp = y - 1; temp <= y + 1; temp++) {
                if (temp >= 0 && temp < gridSize) {
                    positions[x][temp].setType(2);
                }
            }
            bomberManList.add(new BomberMan(x, y, bomberManId++, this));
            positions[x][y].getBombermanList().add(bomberManList.get(bomberManId - 2)); // min 2 because of indexing start at 1 and ++


            if (y == 0) y += gridSize - 1;
            else if (x == 0) {
                x += gridSize - 1;
                y = 0;
            }
        }
    }

    public void setWindowBool(boolean bool) {
        if (bool == windowBool) return;
        windowBool = bool;
        if (windowBool) this.window = new GameWindow(this);
        else {
            if (!(window == null)) window.close();
        }
    }

    public void RunGameLoop() {

        Thread loop = new Thread() {
            @Override
            public void run() {
                GameLoop();
            }
        };
        loop.start();

    }

    public void GameLoop() {
        //create local copy;
        ArrayList<AIHandler> localAIList = (ArrayList<AIHandler>) ai.clone();

        while (PlayerCheck()) {

            for (AIHandler temp : localAIList) temp.AddMoveToBuffer(); //gather moves

            // get the last appended move
            for (AIHandler temp : localAIList) temp.MakeMove(); //make the moves

            // update all bombs
            for (Bomb bomb : activeBombList) bomb.Round();

            for (Bomb bomb : activeBombList) {
                if (bomb.getExploded()) {
                    explodedBombList.add(bomb);
                }
            }
            for (BomberMan man : bomberManList) man.loadBufferIntoPoints(); //add gained points

            for (Bomb bomb : explodedBombList) activeBombList.remove(bomb);

            for (BomberMan man : bomberManList) man.updateBombCooldown();

            for (AIHandler temp : localAIList) temp.UpdateWeights();

            //remove all dead ai's
            Iterator<AIHandler> i = localAIList.iterator();
            while (i.hasNext()) {
                AIHandler temp = i.next();
                if (!temp.getMan().getAlive()) {
                    temp.EndOfRound(-1);
                    i.remove();

                }
            }

            if(amountOfRounds>150)Randombomb(( (amountOfRounds-150)/(double)(50*amountOfRounds)));

            amountOfRounds++;
            if (windowBool && !(window == null)) try {
                window.repaint();
                Thread.sleep(roundTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
 /*       if (amountOfRounds == 200) {
            Iterator<AIHandler> i = localAIList.iterator();
            while (i.hasNext()) {
                AIHandler temp = i.next();
                // SHOULD WE INCORPORATE THE TIME ELEMENT?
                //  temp.getMan().getPoints().add(Bomb.DIECOST);
                // temp.UpdateWeights();
                temp.EndOfRound(-1);
            }
        } else {*/
        Iterator<AIHandler> i = localAIList.iterator();
        while (i.hasNext()) {
            AIHandler temp = i.next();
            temp.EndOfRound(1);
        }
    }

    public Boolean PlayerCheck() {
        int count = 0;
        if(!bomberManList.get(0).getAlive()) return false;
        for (int x = 0; x < bomberManList.size(); x++) {
            if (bomberManList.get(x).getAlive()) count++;
        }
        if (count > 1) return true;
        return false;
    }

    public void Randombomb(double bombProbability) {
        Random rnd = new Random();
        BomberMan man = new BomberMan(0, 0, 5, this);
        for (WorldPosition[] positionarray : positions) {
            for (WorldPosition position : positionarray) {
                if (position.bomb == null && position.type == 2) {
                    if (bombProbability > rnd.nextDouble()) {
                        Bomb bomb = new Bomb(position, man, this);
                        activeBombList.add(bomb);
                        position.add_Bomb(bomb);
                    }
                }
            }
        }
    }


    public boolean getPrint() {
        return PRINT;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getAmountPlayers() {
        return amountPlayers;
    }

    public int getAmountOfRounds() {
        return amountOfRounds;
    }

    public Boolean getWindowBool() {
        return windowBool;
    }

    public GameWindow getWindow() {
        return window;
    }

    public WorldPosition getPositions(int x, int y) {
        return positions[x][y];
    }

    public ArrayList<AIHandler> getAi() {
        return ai;
    }

    public ArrayList<BomberMan> getBomberManList() {
        return bomberManList;
    }

    public ArrayList<Bomb> getActiveBombList() {
        return activeBombList;
    }

    public ArrayList<Bomb> getExplodedBombList() {
        return explodedBombList;
    }

    public int getWin() {
        return win;
    }

    public int getAmountOfAlivePlayers(){
        int count = 0;
        for (int x = 0; x < bomberManList.size(); x++) {
            if (bomberManList.get(x).getAlive()) count++;
        }
        return count;
    }

    public void setRoundTime(int time){
        roundTime = time;
    }

    public int getRoundtime(){
        return roundTime;
    }
}
