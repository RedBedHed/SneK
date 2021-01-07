import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Game
 */
public final class Game {

    /**
     * A singleton instance of {@code Game}.
     */
    public static final Game INSTANCE;

    /**
     * The color of this{@code Game}'s {@code ScorePanel} component.
     */
    public static final Color SCORE_PANEL_COLOR;

    /**
     * The path to this {@code Game}'s Icon.
     */
    public static final String ICON_PATH;

    /**
     * Dimensions for use in JFrame initialization.
     */
    public static final int LENGTH;
    public static final Dimension GAME_FRAME_SIZE;

    /* init */
    static {
        LENGTH = 600;
        SCORE_PANEL_COLOR = new Color(0,30,32);
        GAME_FRAME_SIZE = new Dimension(LENGTH, LENGTH);
        ICON_PATH = "C:/Users/evcmo/IdeaProjects/Snake/icon/snake.png";
        INSTANCE = new Game();
    }

    /**
     * A Frame to hold the GUI.
     */
    private final JFrame gameFrame;

    /**
     * A Panel on which the game will be painted and updated.
     */
    private final GridPanel gamePanel;

    /**
     * A Panel to keep track of level and score.
     */
    private final ScorePanel scorePanel;

    /**
     * A private constructor for {@code Game}.
     */
    private Game(){
        gameFrame = new JFrame("SneK");
        try {
            gameFrame.setIconImage(ImageIO.read(new File(ICON_PATH)));
        } catch(IOException e){
            e.printStackTrace();
        }
        gameFrame.setBackground(Color.WHITE);
        gameFrame.setSize(GAME_FRAME_SIZE);
        gameFrame.setLayout(new BorderLayout());
        gamePanel = new GridPanel();
        scorePanel = new ScorePanel();
        gameFrame.add(scorePanel, BorderLayout.NORTH);
        gameFrame.add(gamePanel, BorderLayout.CENTER);
        gameFrame.setResizable(false);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
    }

    /**
     * Exposes the {@code Game}'s {@code JFrame}.
     *
     * @return the {@code JFrame}
     */
    public final JFrame getFrame(){
        return gameFrame;
    }

    /**
     * Exposes the {@code Game}'s {@code ScorePanel}.
     *
     * @return the {@code ScorePanel}
     */
    public final ScorePanel getScorePanel(){
        return scorePanel;
    }

    /**
     * Exposes the {@code Game}'s {@code GridPanel}.
     *
     * @return the {@code GridPanel}
     */
    public final GridPanel getGridPanel(){
        return gamePanel;
    }

    //Show.
    public static void main(String[] args){
        Game.INSTANCE.gameFrame.setVisible(true);
    }

    /**
     * A method to reset the {@code Grid}'s GUI components.
     */
    public final void reset(){
        gamePanel.reset();
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                gameFrame.revalidate();
                gameFrame.repaint();
            }
        });
    }

    /**
     * Score Panel
     */
    public static final class ScorePanel extends JPanel {

        /**
         * A public constructor for a {@code ScorePanel}.
         */
        public ScorePanel() {
            super();
            setBackground(SCORE_PANEL_COLOR);
            add(updateLabel(0,0, GridPanel.DEFAULT_TAIL_BITE_QUANTITY));
            setVisible(true);
        }

        /**
         * A method to update the {@code ScorePanel}'s text label.
         *
         * @param level the current level
         * @param score the current score
         */
        public final void update(final int level,
                                 final int score,
                                 final int tailBites){
            removeAll();
            add(updateLabel(level, score, tailBites));
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    validate();
                    repaint();
                }
            });
        }

        /**
         * A method to reset the {@code ScorePanel}.
         */
        public final void reset(){
            update(0,0, GridPanel.DEFAULT_TAIL_BITE_QUANTITY);
        }

        // Returns a new label given a level and score.
        private static JLabel updateLabel(final int level,
                                          final int score,
                                          final int tailBites){
            final JLabel label = new JLabel();
            label.setText(String.format(
                    "%s     Level: %d     Score: %d     Remaining Tail Bites: %d",
                    (level == 1? "Apples go monch!":
                            level == GridPanel.MINE_INTRODUCTION_LEVEL? "Mines go boom!":
                                    (level & 3) == 0? "Don't eat your tail!":
                                            "Press 'esc' to pause."), level, score, tailBites
            ));
            label.setForeground(Color.WHITE);
            return label;
        }

    }

    public static final class GridPanel extends JPanel {

        /**
         * Game constants.
         */
        public static final int SQUARE_SIZE;
        public static final int SQUARE_BUFF;
        public static final Dimension PANEL_SIZE;
        public static final int DEFAULT_SCORE;
        public static final int DEFAULT_APPLE_QUANTITY;
        public static final int DEFAULT_MINE_QUANTITY;
        public static final int DEFAULT_TAIL_BITE_QUANTITY;
        public static final int APPLE_INCREMENT;
        public static final int MINE_INCREMENT;
        public static final int SCORE_INCREMENT;
        public static final int MINE_INTRODUCTION_LEVEL;
        public static final Color HEAD_COLOR;
        public static final Color SEGMENT_COLOR;
        public static final Color APPLE_COLOR;
        public static final Color MINE_COLOR;
        public static final int DEFAULT_SNAKE_SIZE;
        public static final int DEFAULT_UPDATE_INTERVAL;
        public static final int HORIZONTAL_BOUND;
        public static final int VERTICAL_BOUND;
        public static final Random rgen;

        /* init */
        static {
            SQUARE_SIZE = 8;
            SQUARE_BUFF = 2;
            DEFAULT_APPLE_QUANTITY = 1;
            DEFAULT_MINE_QUANTITY = 1;
            DEFAULT_TAIL_BITE_QUANTITY = 5;
            DEFAULT_SCORE = 80;
            APPLE_INCREMENT = 2;
            MINE_INCREMENT = 1;
            SCORE_INCREMENT = 8;
            MINE_INTRODUCTION_LEVEL = 6;
            PANEL_SIZE = new Dimension(Game.LENGTH, Game.LENGTH);
            HEAD_COLOR = new Color(15,100,50);
            SEGMENT_COLOR = new Color(10,255,100);
            APPLE_COLOR = new Color(250, 10, 10);
            DEFAULT_SNAKE_SIZE = 10;
            DEFAULT_UPDATE_INTERVAL = 7500000;
            HORIZONTAL_BOUND = 576;
            VERTICAL_BOUND = 528;
            MINE_COLOR = new Color(200,0,100);
            rgen = new Random();
        }

        /**
         * Indicates whether or not the game is running/paused/done.
         */
        private GameStatus gameStatus;

        /**
         * A {@code List} of the {@code Segment}s that comprise the snake.
         */
        private List<Segment> snake;

        /**
         * A {@code Map} of all the {@code Segment}s that comprise the snake.
         */
        private Map<Point, Segment> segmentMap;

        /**
         * The current direction of the snake.
         */
        private Direction currentDirection;

        /**
         * The current location of the head of the snake.
         */
        private Point currentLocation;

        /**
         * The legal size of the snake.
         */
        private int legalSnakeSize;

        /**
         * A {@code List} of all current {@code Apple}s on the grid.
         */
        private List<Apple> apples;

        /**
         * The legal number of {@code Apple}s. This number changes with each level.
         */
        private int legalNumberOfApples;

        /**
         * The legal number of {@code Mine}s. This number changes with each level.
         */
        private int legalNumberOfMines;

        /**
         * The current level.
         */
        private int level;

        /**
         * The current score;
         */
        private int score;

        /**
         * The number of tail-bites remaining.
         */
        private int tailBites;

        /**
         * A {@code List} of all current {@code Mine}s on the grid.
         */
        private List<Mine> mines;

        /**
         * The game thread.
         */
        private Thread gameThread;

        /**
         * Represents the number of times the update method has been called.
         * This number is reset to zero each time the GUI is changed.
         */
        private int updateCount;

        /**
         * A public constructor for a {@code GridPanel}.
         */
        public GridPanel(){
            setSize(PANEL_SIZE);
            setBackground(Color.DARK_GRAY);
            //Initialize fields.
            init();
            //Add keyboard listener and hook up arrow keys + esc key.
            setFocusable(true);
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    final int keyCode = e.getKeyCode();
                    if((keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) && !currentDirection.isLeft())
                        currentDirection = Direction.RIGHT;
                    else if((keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) && !currentDirection.isRight())
                        currentDirection = Direction.LEFT;
                    else if((keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) && !currentDirection.isDown())
                        currentDirection = Direction.UP;
                    else if((keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) && !currentDirection.isUp())
                        currentDirection = Direction.DOWN;
                    else if(keyCode == KeyEvent.VK_ESCAPE) gameStatus = gameStatus.pause();
                }
            });
            //Start up game thread.
            (new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(gameStatus.isRunning()) update();
                }
            })).start();
        }

        /*
         * A method to initialize fields and avoid redundancy.
         */
        private void init(){
            snake = Collections.emptyList();
            apples = Collections.emptyList();
            mines = Collections.emptyList();
            segmentMap = Collections.emptyMap();
            currentLocation = new Point(SQUARE_SIZE, SQUARE_SIZE);
            legalSnakeSize = DEFAULT_SNAKE_SIZE;
            currentDirection = Direction.RIGHT;
            gameStatus = GameStatus.RUNNING;
            legalNumberOfApples = DEFAULT_APPLE_QUANTITY;
            legalNumberOfMines = DEFAULT_MINE_QUANTITY;
            tailBites = DEFAULT_TAIL_BITE_QUANTITY;
            level = 0;
            score = DEFAULT_SCORE;
        }

        /**
         * Resets the {@code GridPanel}'s fields to their default values.
         */
        public final void reset(){
            init();
        }

        /**
         * GameStatus
         */
        private enum GameStatus {
            RUNNING{
                /** @inheritDoc */
                @Override
                public boolean isRunning() {
                    return true;
                }
                /** @inheritDoc */
                @Override
                public boolean isPaused() {
                    return false;
                }
            },
            PAUSED{
                /** @inheritDoc */
                @Override
                public boolean isRunning() {
                    return false;
                }
                /** @inheritDoc */
                @Override
                public boolean isPaused() {
                    return true;
                }
                /**
                 * {@code inheritDoc}
                 *
                 * @return the RUNNING {@code GameStatus}
                 */
                @Override
                public GameStatus pause(){
                    return RUNNING;
                }
            },
            DONE{
                /** @inheritDoc */
                @Override
                public boolean isRunning() {
                    return false;
                }
                /** @inheritDoc */
                @Override
                public boolean isPaused() {
                    return false;
                }
            };

            /**
             * A method to indicate whether or not the {@code GameStatus} is DONE.
             *
             * @return whether or not the {@code GameStatus} is DONE
             */
            public abstract boolean isRunning();

            /**
             * A method to indicate whether or not the {@code GameStatus} is PAUSED.
             *
             * @return whether or not the {@code GameStatus} is PAUSED
             */
            public abstract boolean isPaused();

            /**
             * A polymorphic approach to toggling the {@code GameStatus} between
             * PAUSED and RUNNING.
             *
             * @return the PAUSED game status
             */
            public GameStatus pause(){
                return PAUSED;
            }

        }

        /*
         * A method to house immediate game logic and to update the GUI when needed.
         */
        private void update(){
            populatePixelElements();
            if(snake.size() > 2) {
                final Segment head = snake.get(snake.size() - 1);
                final boolean eatingMine = (level >= MINE_INTRODUCTION_LEVEL && eatingMine(head));
                final boolean eatingSelf = tailBites <= 0;
                if (head.getX() > HORIZONTAL_BOUND || head.getY() > VERTICAL_BOUND ||
                        head.getX() < 0 || head.getY() < 0 || eatingMine || eatingSelf) {
                    gameStatus = GameStatus.PAUSED;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final int option = JOptionPane.showConfirmDialog(
                                    Game.INSTANCE.getFrame(),
                                    String.format("Your score is: %d%nTry again?", score),
                                    (eatingMine? "Boom!": eatingSelf? "Chomp!": "Bonk!"),
                                    JOptionPane.YES_NO_OPTION
                            );
                            if (option == JOptionPane.NO_OPTION) System.exit(0);
                            else if (option == JOptionPane.YES_OPTION) {
                                Game.INSTANCE.reset();
                            }
                        }
                    });
                } else if (eatingApple(head)) {
                    legalSnakeSize++;
                    Game.INSTANCE.getScorePanel().update(level, score += SCORE_INCREMENT, tailBites);
                } else if (segmentMap.containsKey(head.getPoint())) {
                    Game.INSTANCE.getScorePanel().update(level, score, --tailBites);
                }
            }
            validate();
            repaint();
        }

        /*
         * A method to populate the snake, apples, and mines Lists if needed.
         * Apples and mines are updated at the end of each level, when the apples
         * List is empty. Apples and mines are not allowed to be placed atop
         * already-present apples and mines.
         */
        private void populatePixelElements(){
            if(snake.isEmpty()) snake = List.of(new Segment(currentLocation));
            if(apples.isEmpty()){
                Set<Apple> applesSet = new HashSet<>();
                tailBites = DEFAULT_TAIL_BITE_QUANTITY;
                level++;
                final List<Apple> replacementApples = new ArrayList<>();
                for(int i = 0; i < legalNumberOfApples; i++) {
                    Apple a = new Apple(level);
                    while (applesSet.contains(a)) {
                        a = new Apple(level);
                    }
                    applesSet.add(a);
                    replacementApples.add(a);
                }
                apples = Collections.unmodifiableList(replacementApples);
                legalNumberOfApples += APPLE_INCREMENT;
                if(level >= MINE_INTRODUCTION_LEVEL){
                    final List<Mine> replacementMines = new ArrayList<>();
                    for(int i = 0; i < legalNumberOfMines; i++) {
                        Mine m = new Mine(level);
                        while (applesSet.contains(m)) {
                            m = new Mine(level);
                        }
                        applesSet.add(m);
                        replacementMines.add(m);
                    }
                    mines = Collections.unmodifiableList(replacementMines);
                    legalNumberOfMines += MINE_INCREMENT;
                }
                Game.INSTANCE.getScorePanel().update(level, score, tailBites);
            }
        }

        /*
         * A method to determine whether or not the snake is eating an
         * apple. If so, the apple is simply ignored upon updating the
         * apples list.
         */
        private boolean eatingApple(final PixelElement pix){
            final List<Apple> replacementApples = new ArrayList<>();
            boolean eaten = false;
            for(Apple a: apples){
                if(!a.equals(pix)) replacementApples.add(a);
                else eaten = true;
            }
            apples = Collections.unmodifiableList(replacementApples);
            return eaten;
        }

        /*
         * A method to determine whether or not the snake is eating a mine,
         * detonating the mine if so.
         */
        private boolean eatingMine(final PixelElement pix){
            final List<Mine> replacementMines = new ArrayList<>();
            boolean eaten = false;
            for(Mine m: mines){
                if(m.equals(pix)) {
                    eaten = true;
                    m = m.detonate();
                }
                replacementMines.add(m);
            }
            mines = Collections.unmodifiableList(replacementMines);
            return eaten;
        }

        /**
         * This method is responsible for painting the {@code GridPanel}
         *
         * @param g the panel's {@code Graphics} Object.
         */
        @Override
        public void paint(final Graphics g){
            g.clearRect(0,0, Game.LENGTH, Game.LENGTH);
            g.drawRect(0,0, Game.LENGTH, Game.LENGTH);
            g.setColor(Color.BLACK);
            g.fillRect(0,0, Game.LENGTH, Game.LENGTH);
            paintSnake(g);
            for(Apple a: apples) a.paint(g);
            for(Mine m: mines) m.paint(g);
        }

        /*
         * A method to paint the snake, updating the current location and creating
         * a new head at this location. If the snake is legally-sized, the tail segment
         * is simply ignored upon painting and updating of the snake List.
         */
        private void paintSnake(final Graphics g){
            final List<Segment> replacementList = new ArrayList<>();
            final Map<Point, Segment> replacementMap = new HashMap<>();
            for(int i = (snake.size() > legalSnakeSize) ? 1: 0; i < snake.size(); i++) {
                final Segment s = snake.get(i);
                s.paint(g);
                replacementList.add(s);
                replacementMap.put(s.getPoint(), s);
            }
            final Segment s = new Segment(
                    currentLocation = currentDirection.traverse(currentLocation)
            );
            s.paint(g);
            replacementList.add(s);
            snake = Collections.unmodifiableList(replacementList);
            segmentMap = Collections.unmodifiableMap(replacementMap);
        }

        /*
         * Pixel Element
         */
        private static abstract class PixelElement {

            /*
             * The location of the element on the cartesian coordinate system.
             */
            protected final Point location;

            /*
             * A public constructor for a PixelElement.
             */
            public PixelElement(final Point location){
                this.location = location;
            }

            /*
             * Exposes the horizontal coordinate.
             */
            public final int getX(){
                return location.x;
            }

            /*
             * Exposes the vertical coordinate.
             */
            public final int getY(){
                return location.y;
            }

            /*
             * Exposes the point.
             */
            public final Point getPoint(){
                return location;
            }

            /*
             * A method for comparing two PixelElements.
             */
            @Override
            public boolean equals(Object other){
                if(this == other) return true;
                if(other == null) return false;
                if(!(other instanceof PixelElement)) return false;
                PixelElement cast = (PixelElement) other;
                return this.location.equals(cast.location);
            }

            @Override
            public int hashCode(){
                return location.hashCode();
            }

            /*
             * A method for paining the PixelElement.
             */
            public abstract void paint(Graphics g);

        }

        private static final class Segment extends PixelElement {

            /*
             * A field to indicate whether or not this segment is a
             * head segment.
             */
            private boolean isHead;

            /*
             * A public constructor for a segment.
             */
            public Segment(final Point location){
                super(location);
                isHead = true;
            }

            /*
             * See PixelElement.paint(Graphics).
             */
            @Override
            public void paint(final Graphics g){
                g.setColor(Color.BLACK);
                g.fillRect(getX(), getY(), SQUARE_SIZE, SQUARE_SIZE);
                if(isHead) {
                    g.setColor(HEAD_COLOR);
                    isHead = false;
                }
                else g.setColor(SEGMENT_COLOR);
                g.fillRect(
                        getX() + SQUARE_BUFF, getY() + SQUARE_BUFF,
                        SQUARE_SIZE - SQUARE_BUFF, SQUARE_SIZE - SQUARE_BUFF
                );
            }

        }

        /*
         * Apple
         */
        private static class Apple extends PixelElement {

            /*
             * Thirds of the horizontal and vertical boundaries respectively.
             */
            private static final int X_THIRD = 24;
            private static final int Y_THIRD = 22;

            /*
             * A public constructor for an Apple.
             */
            public Apple(final int level){
                super(init(level));
            }

            /*
             * A secondary public constructor for an Apple.
             */
            public Apple(final Point loc) {
                super(loc);
            }

            /*
             * A method to initialize the randomly-generated location of the Apple.
             * The interval of Random generation increases with each level so that
             * Apples will be placed closer to the center early on in the game.
             */
            private static Point init(final int level){
                int lowerXBound = level < X_THIRD? X_THIRD - (level << 1): 0;
                int lowerYBound = level < Y_THIRD? Y_THIRD - (level << 1): 0;
                int upperXBound = level < X_THIRD? X_THIRD + (level << 2): X_THIRD << 1;
                int upperYBound = level < Y_THIRD? Y_THIRD + (level << 2): Y_THIRD << 1;
                return new Point(
                        (lowerXBound + rgen.nextInt(upperXBound)) << 3,
                        (lowerYBound + rgen.nextInt(upperYBound)) << 3
                );
            }

            /*
             * See PixelElement.paint(Graphics).
             */
            @Override
            public void paint(final Graphics g){
                final int nx = getX();
                final int ny = getY();
                g.setColor(Color.BLACK);
                g.fillRect(nx, ny, SQUARE_SIZE, SQUARE_SIZE);
                g.setColor(APPLE_COLOR);
                g.fillRect(
                        nx + SQUARE_BUFF, ny + SQUARE_BUFF,
                        SQUARE_SIZE - SQUARE_BUFF, SQUARE_SIZE - SQUARE_BUFF
                );
            }

        }

        /*
         * Mine
         */
        private static final class Mine extends Apple {

            /*
             * A field to mark the Mine for detonation.
             */
            private final boolean detonate;

            /*
             * A field to indicate the current level.
             */
            private final int level;

            /*
             * A public constructor for a Mine.
             */
            public Mine(final int level){
                super(level);
                this.level = level;
                this.detonate = false;
            }

            public Mine(final Point loc, final int level) {
                super(loc);
                this.level = level;
                this.detonate = true;
            }

            /*
             * See PixelElement.paint(Graphics).
             */
            @Override
            public void paint(final Graphics g){
                final int nx = getX();
                final int ny = getY();
                g.setColor(Color.BLACK);
                g.fillRect(nx, ny, SQUARE_SIZE, SQUARE_SIZE);
                if(detonate) {
                    final int blastSize = SQUARE_SIZE << 2;
                    g.setColor(Color.WHITE);
                    g.fillRect(nx - 20, ny - 20, blastSize, blastSize);
                }
                else {
                    g.setColor(MINE_COLOR);
                    g.fillRect(
                            nx + SQUARE_BUFF, ny + SQUARE_BUFF,
                            SQUARE_SIZE - SQUARE_BUFF, SQUARE_SIZE - SQUARE_BUFF
                    );
                }
            }

            /*
             * Marks the Mine for detonation.
             */
            public Mine detonate(){
                return new Mine(location, level);
            }

        }

        /*
         * A re-creation of awt Point.
         */
        private static final class Point {

            /*
             * Public coordinate fields
             */
            public final int x;
            public final int y;

            /*
             * Convenience constructor.
             */
            public Point(final int x, final int y){
                this.x = x;
                this.y = y;
            }

            /*
             * A method to compare two Points.
             */
            @Override
            public boolean equals(Object other){
                if(this == other) return true;
                if(other == null) return false;
                if(!(other instanceof Point)) return false;
                Point cast = (Point) other;
                return this.x == cast.x && this.y == cast.y;
            }

            @Override
            public int hashCode() {
                int hash = 1;
                hash = 31 * hash + x;
                return 31 * hash + y;
            }

            @Override
            public String toString(){
                return "[" + x + ", " + y + "]";
            }

        }

        /**
         * Direction.
         */
        public enum Direction {

            UP {
                /** inheritDoc */
                @Override
                public Point traverse(final Point loc) {
                    return new Point(loc.x,loc.y - SQUARE_SIZE);
                }

                @Override
                public boolean isUp() {
                    return true;
                }

                @Override
                public boolean isDown() {
                    return false;
                }

                @Override
                public boolean isLeft() {
                    return false;
                }

                @Override
                public boolean isRight() {
                    return false;
                }
            },
            DOWN {
                /** inheritDoc */
                @Override
                public Point traverse(Point loc) {
                    return new Point(loc.x,loc.y + SQUARE_SIZE);
                }

                @Override
                public boolean isUp() {
                    return false;
                }

                @Override
                public boolean isDown() {
                    return true;
                }

                @Override
                public boolean isLeft() {
                    return false;
                }

                @Override
                public boolean isRight() {
                    return false;
                }
            },
            LEFT {
                /** inheritDoc */
                @Override
                public Point traverse(Point loc) {
                    return new Point(loc.x - SQUARE_SIZE, loc.y);
                }

                @Override
                public boolean isUp() {
                    return false;
                }

                @Override
                public boolean isDown() {
                    return false;
                }

                @Override
                public boolean isLeft() {
                    return true;
                }

                @Override
                public boolean isRight() {
                    return false;
                }
            },
            RIGHT {
                /** inheritDoc */
                @Override
                public Point traverse(Point loc) {
                    return new Point(loc.x + SQUARE_SIZE, loc.y);
                }

                @Override
                public boolean isUp() {
                    return false;
                }

                @Override
                public boolean isDown() {
                    return false;
                }

                @Override
                public boolean isLeft() {
                    return false;
                }

                @Override
                public boolean isRight() {
                    return true;
                }
            };

            /**
             * A polymorphic approach to snake navigation on the GridPanel.
             */
            public abstract Point traverse(final Point loc);

            public abstract boolean isUp();
            public abstract boolean isDown();
            public abstract boolean isLeft();
            public abstract boolean isRight();

        }

    }

}