package ua.com.adwall;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Queue;

/**
 * Hello world!
 */
@SpringBootApplication
public class App {

    static final char wall = '@';
    static final char empty = '*';
    static final char passed = ' ';
    static final char enemy = '#';
    static final char user = 'O';

    static final int xSize = 31;
    static final int ySize = 31;
    static final char[][] field = new char[xSize][ySize];
    static int cellSize = 30;

    static BufferedImage wallImage;
    static BufferedImage userImage;
    static BufferedImage emptyImage;
    static BufferedImage enemyImage;

    static int globalUserX;
    static int globalUserY;

    static int userScore;
    static int enemyScore;

    public static void main(String[] args) throws IOException, InterruptedException {
        clearField();
        prepareField();
        loadResources();
        new MyFrame();
    }

    static void loadResources() throws IOException {
        wallImage = ImageIO.read(new File("brick.jpg"));
        userImage = ImageIO.read(new File("D:\\Programming\\Projects\\AdWall\\src\\main\\resources\\user2.gif"));
        emptyImage = ImageIO.read(new File("D:\\Programming\\Projects\\AdWall\\src\\main\\resources\\star.png"));
        enemyImage = ImageIO.read(new File("D:\\Programming\\Projects\\AdWall\\src\\main\\resources\\user3.png"));
    }

    public static void prepareField() {
        Random random = new Random();
        for (int i = 0; i < xSize; i++) {
            if (i % 2 == 1) {
                continue;
            }
            for (int j = 0; j < ySize; j++) {
                if (j == 1 || j == ySize - 2) {
                    continue;
                }
                int rnd = random.nextInt(7);
                if (rnd > 0) {
                    setField(i, j, wall);
                }
            }
        }
    }

    static void setField(int x, int y, char value) {
        field[x][y] = value;
    }

    static boolean isGoodDirection(int x, int y, int direction) {
        switch (direction) {
            case 0:  //up
                if (x == 0 || isNotEmptyField(x - 1, y)) {
                    return false;
                }
                break;
            case 1:  //right
                if (y == ySize - 1 || isNotEmptyField(x, y + 1)) {
                    return false;
                }
                break;
            case 2:  //down
                if (x == xSize - 1 || isNotEmptyField(x + 1, y)) {
                    return false;
                }
                break;
            case 3:  //left
                if (y == 0 || isNotEmptyField(x, y - 1)) {
                    return false;
                }
                break;
        }
        return true;
    }

    static boolean isNotEmptyField(int x, int y) {
        char fieldCell = field[x][y];
        return fieldCell == wall || fieldCell == user || fieldCell == enemy;
    }

    static void clearField() {
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                setField(i, j, empty);
            }
        }
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                if (i == 0 || j == 0 || i == xSize - 1 || j == ySize - 1) {
                    setField(i, j, wall);
                }
            }
        }
    }

    public static class MyFrame extends JFrame {
        public MyFrame() throws InterruptedException {
            super("Eat the Stars!!!");
            JPanel jPanel = new FieldPanel();
            addKeyListener(new PlayerKeyListener(jPanel, 1, 1));
            setContentPane(jPanel);

            JPanel scorePanel = new ScorePanel();
            jPanel.add(scorePanel);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize((xSize + 1) * cellSize, (ySize + 2) * cellSize);
            setVisible(true);

            new EnemyThread(jPanel, xSize - 2, ySize - 2, true).start();
            Thread.sleep(100);
            new EnemyThread(jPanel, 1, ySize - 2, true).start();
            Thread.sleep(100);
            new EnemyThread(jPanel, xSize - 2, 1, true).start();
        }
    }

    private static class FieldPanel extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < xSize; i++) {
                for (int j = 0; j < ySize; j++) {
                    char area = field[i][j];
                    switch (area) {
                        case empty:
                            g.setColor(Color.WHITE);
                            g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                            g.drawImage(emptyImage, j * cellSize + cellSize / 4, i * cellSize + cellSize / 4, cellSize / 2, cellSize / 2, Color.WHITE, null);
                            break;
                        case user:
                            g.drawImage(userImage, j * cellSize, i * cellSize, cellSize, cellSize, Color.WHITE, null);
                            break;
                        case enemy:
                            g.drawImage(enemyImage, j * cellSize, i * cellSize, cellSize, cellSize, Color.WHITE, null);
                            break;
                        case passed:
                            g.setColor(Color.WHITE);
                            g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                            break;
                        case wall:
                            g.drawImage(wallImage, j * cellSize, i * cellSize, cellSize, cellSize, Color.BLACK, null);
                            break;
                    }
                }
            }
        }
    }

    private static class PlayerKeyListener implements KeyListener {
        private JPanel jPanel;
        private int userX;
        private int userY;

        PlayerKeyListener(JPanel jPanel, int startX, int startY) {
            this.jPanel = jPanel;
            this.userX = startX;
            this.userY = startY;
            globalUserX = this.userX;
            globalUserY = this.userY;
            setField(userX, userY, user);
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
            makeUserMove(e);
        }

        private void makeUserMove(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'w':
                    if (isGoodDirection(userX, userY, 0)) {
                        setField(userX, userY, passed);
                        userX--;
                        if (field[userX][userY] == empty) {
                            userScore++;
                        }
                        setField(userX, userY, user);
                        jPanel.updateUI();
                    }
                    break;
                case 'd':
                    if (isGoodDirection(userX, userY, 1)) {
                        setField(userX, userY, passed);
                        userY++;
                        if (field[userX][userY] == empty) {
                            userScore++;
                        }
                        setField(userX, userY, user);
                        jPanel.updateUI();
                    }
                    break;
                case 's':
                    if (isGoodDirection(userX, userY, 2)) {
                        setField(userX, userY, passed);
                        userX++;
                        if (field[userX][userY] == empty) {
                            userScore++;
                        }
                        setField(userX, userY, user);
                        jPanel.updateUI();
                    }
                    break;
                case 'a':
                    if (isGoodDirection(userX, userY, 3)) {
                        setField(userX, userY, passed);
                        userY--;
                        if (field[userX][userY] == empty) {
                            userScore++;
                        }
                        setField(userX, userY, user);
                        jPanel.updateUI();
                    }
                    break;
            }
            globalUserX = this.userX;
            globalUserY = this.userY;
        }
    }

    private static class ScorePanel extends JPanel {

        private ScoreArea userScoreArea;
        private ScoreArea enemyScoreArea;

        public ScorePanel() {
            userScoreArea = new ScoreArea("User Score", userScore);
            add(userScoreArea, 0);
            enemyScoreArea = new ScoreArea("Enemy Score", enemyScore);
            add(enemyScoreArea, 1);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            userScoreArea.setScore(userScore);
            enemyScoreArea.setScore(enemyScore);
        }
    }

    private static class ScoreArea extends JLabel {
        private int score;
        private String message;

        public ScoreArea(String message, int score) {
            super(message + " - " + score);
            this.message = message;
            this.score = score;
        }

        public void setScore(int score) {
            this.score = score;
            updateUI();
        }

        @Override
        public String getText() {
            return message + " - " + score;
        }
    }

    private static class EnemyThread extends Thread {
        private JPanel jPanel;
        private int enemyX;
        private int enemyY;
        private int direction;
        private boolean smart;

        public EnemyThread(JPanel jPanel, int startX, int startY, boolean smart) {
            this.jPanel = jPanel;
            this.enemyX = startX;
            this.enemyY = startY;
            this.smart = smart;
            field[enemyX][enemyY] = enemy;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (field) {
                    direction = getRandomDirection(direction);
                    if (direction >= 0) {
                        makeMove();
                    }
                }
                jPanel.updateUI();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void makeMove() {
            switch (direction) {
                case 0:
                    setField(enemyX, enemyY, passed);
                    enemyX--;
                    if (field[enemyX][enemyY] == empty) {
                        enemyScore++;
                    }
                    setField(enemyX, enemyY, enemy);
                    break;
                case 1:
                    setField(enemyX, enemyY, passed);
                    enemyY++;
                    if (field[enemyX][enemyY] == empty) {
                        enemyScore++;
                    }
                    setField(enemyX, enemyY, enemy);
                    break;
                case 2:
                    setField(enemyX, enemyY, passed);
                    enemyX++;
                    if (field[enemyX][enemyY] == empty) {
                        enemyScore++;
                    }
                    setField(enemyX, enemyY, enemy);
                    break;
                case 3:
                    setField(enemyX, enemyY, passed);
                    enemyY--;
                    if (field[enemyX][enemyY] == empty) {
                        enemyScore++;
                    }
                    setField(enemyX, enemyY, enemy);
                    break;
            }
        }

        private int getRandomDirection(int lastDirection) {
            Queue<DirectionEntry> directionQueue = getDirectionQueue(lastDirection);
            while (!directionQueue.isEmpty()) {
                int direction = directionQueue.poll().value;
                if (isGoodDirection(enemyX, enemyY, direction)) {
                    return direction;
                }
            }
            return direction - 4;
        }

        private int getRandomValue(Set<Integer> set) {
            int size = set.size();
            int item = new Random().nextInt(size);
            int i = 0;
            Integer foundValue = 0;
            for (Integer obj : set) {
                if (i == item) {
                    foundValue = obj;
                    break;
                }
                i = i + 1;
            }
            set.remove(foundValue);
            return foundValue;
        }

        private Set<Integer> getPrioritySet(int start, int count) {
            Set<Integer> prioritySet = new HashSet<Integer>();
            for (int i = start; i < start + count; i++) {
                prioritySet.add(i);
            }
            return prioritySet;
        }

        private Queue<DirectionEntry> getDirectionQueue(int lastDirection) {
            Queue<DirectionEntry> directionQueue = new PriorityQueue<DirectionEntry>();
            if (lastDirection >= 0) {
                if (smart) {
                    smartPopulateQueue(directionQueue);
                } else {
                    randomPopulateQueue(lastDirection, directionQueue);
                }
            } else {
                if (lastDirection < -2) {
                    directionQueue.add(new DirectionEntry(1, lastDirection + 6));
                } else {
                    directionQueue.add(new DirectionEntry(1, lastDirection + 2));
                }
            }
            return directionQueue;
        }

        private void randomPopulateQueue(int lastDirection, Queue<DirectionEntry> queue) {
            Set<Integer> prioritySet = getPrioritySet(0, 5);
            for (int i = 0; i < 4; i++) {
                if ((lastDirection < 2 && lastDirection + 2 != i) || (lastDirection >= 2 && lastDirection - 2 != i)) {
                    queue.add(new DirectionEntry(getRandomValue(prioritySet), i));
                }
            }
            queue.add(new DirectionEntry(getRandomValue(prioritySet), lastDirection));
            queue.add(new DirectionEntry(getRandomValue(prioritySet), lastDirection));
        }

        private void smartPopulateQueue(Queue<DirectionEntry> directionQueue) {
            int priorityUp = 0;
            int priorityRight = 0;
            int priorityDown = 0;
            int priorityLeft = 0;

            if (globalUserX == enemyX) {
                if (globalUserY > enemyY) {
                    priorityRight = 3;
                } else {
                    priorityLeft = 3;
                }
                Set<Integer> prioritySet = getPrioritySet(1, 2);
                priorityUp = getRandomValue(prioritySet);
                priorityDown = getRandomValue(prioritySet);
            } else if (globalUserY == enemyY) {
                if (globalUserX > enemyX) {
                    priorityDown = 3;
                } else {
                    priorityUp = 3;
                }
                Set<Integer> prioritySet = getPrioritySet(1, 2);
                priorityLeft = getRandomValue(prioritySet);
                priorityRight = getRandomValue(prioritySet);
            } else if (globalUserX > enemyX) {
                if (globalUserY > enemyY) {
                    Set<Integer> prioritySet = getPrioritySet(2, 2);
                    priorityDown = getRandomValue(prioritySet);
                    priorityRight = getRandomValue(prioritySet);
                    prioritySet = getPrioritySet(0, 2);
                    priorityUp = getRandomValue(prioritySet);
                    priorityLeft = getRandomValue(prioritySet);
                } else {
                    Set<Integer> prioritySet = getPrioritySet(2, 2);
                    priorityDown = getRandomValue(prioritySet);
                    priorityLeft = getRandomValue(prioritySet);
                    prioritySet = getPrioritySet(0, 2);
                    priorityUp = getRandomValue(prioritySet);
                    priorityRight = getRandomValue(prioritySet);
                }
            } else if (globalUserX < enemyX) {
                if (globalUserY < enemyY) {
                    Set<Integer> prioritySet = getPrioritySet(2, 2);
                    priorityUp = getRandomValue(prioritySet);
                    priorityLeft = getRandomValue(prioritySet);
                    prioritySet = getPrioritySet(0, 2);
                    priorityDown = getRandomValue(prioritySet);
                    priorityRight = getRandomValue(prioritySet);
                } else {
                    Set<Integer> prioritySet = getPrioritySet(2, 2);
                    priorityUp = getRandomValue(prioritySet);
                    priorityRight = getRandomValue(prioritySet);
                    prioritySet = getPrioritySet(0, 2);
                    priorityDown = getRandomValue(prioritySet);
                    priorityLeft = getRandomValue(prioritySet);
                }
            }

            directionQueue.add(new DirectionEntry(priorityUp, 0));
            directionQueue.add(new DirectionEntry(priorityRight, 1));
            directionQueue.add(new DirectionEntry(priorityDown, 2));
            directionQueue.add(new DirectionEntry(priorityLeft, 3));
        }
    }

    private static class DirectionEntry implements Comparable<DirectionEntry> {
        private Integer priority;
        private Integer value;

        public DirectionEntry(Integer priority, Integer value) {
            this.priority = priority;
            this.value = value;
        }

        public int compareTo(DirectionEntry o) {
            return o.priority.compareTo(priority);
        }
    }
}
