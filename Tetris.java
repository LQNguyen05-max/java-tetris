/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.tetris;

/**
 * Le Nguyen
 * Exercise 1.6 Tetris Implementation
 * Date: 9/6/2025
 * @author n2swa
 */

// Import potential libaries
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Tetris extends JPanel implements ActionListener {

    // Board variables
    public static final int CELL_SIZE = 30;
    public static final int COLUMNS = 10;
    public static final int ROWS = 20;
    public Color[][] board = new Color[ROWS][COLUMNS];

    // Playback timer
    public Timer timer;
    public Tetromino currentPiece;
    
    // Holding Pieces
    public Tetromino holdPiece = null;
    public boolean holdUsed = false;
    
    public Tetromino nextPiece;

    // Randomize tetromino
    public Random random = new Random();
    
    // Tracking Score and Tetris Level
    public int score = 0;
    
    public int linesCleared = 0;
    public int level = 1;
    public int baseDelay = 500;
    
    public boolean pause = false;

    public final int[][][] SHAPES = {
        {{1,1,1,1}}, // I
        {{1,1},{1,1}}, // O
        {{0,1,0},{1,1,1}}, // T
        {{1,0},{1,0},{1,1}}, // L
        {{0,1},{0,1},{1,1}}, // J
        {{0,1,1},{1,1,0}}, // S
        {{1,1,0},{0,1,1}}  // Z
    };

    public final Color[] COLORS = {
        Color.CYAN, Color.YELLOW, Color.MAGENTA,
        Color.ORANGE, Color.BLUE, Color.GREEN, Color.RED
    };

    public Tetris() {
        setPreferredSize(new Dimension(COLUMNS * CELL_SIZE+120, ROWS * CELL_SIZE));
        setBackground(Color.GRAY);

        timer = new Timer(500, this);
        timer.start();

        spawnPiece();

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            
    @Override
    // Incorporating movement keys
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_P:
                if (pause) {
                    timer.start();
                } else {
                    timer.stop();
                }
                pause = !pause;
                repaint(); 
                break;

            default:
                if (pause) return; 

                switch(e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: setLocation(-1,0); break;
                    case KeyEvent.VK_RIGHT: setLocation(1,0); break;
                    case KeyEvent.VK_DOWN: setLocation(0,1); break;
                    case KeyEvent.VK_UP: rotate(); break;
                    case KeyEvent.VK_SPACE: hardDrop(); break;
                    case KeyEvent.VK_SHIFT: holdCurrentPiece(); break;
                }
                repaint();
        }
    }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                pause = true;
                timer.stop();
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                pause = false;
                timer.start();
                repaint();
            }
        });
    }

    // Spawning Pieces
    public void spawnPiece() {
        if (nextPiece == null) {
            int idx = random.nextInt(SHAPES.length);
            nextPiece = new Tetromino(SHAPES[idx], COLORS[idx], COLUMNS / 2 - 1, 0);
        }
        currentPiece = nextPiece;
        int idx = random.nextInt(SHAPES.length);
        nextPiece = new Tetromino(SHAPES[idx], COLORS[idx], COLUMNS / 2 - 1, 0); 
        
    }

    // Holding the Current Pieces, replacing it with Temp
    public void holdCurrentPiece() {
        if (!holdUsed) {
            Tetromino temp = holdPiece;
            holdPiece = currentPiece;
            if (temp != null) {
                currentPiece = new Tetromino(temp.shape, temp.color, COLUMNS / 2 - 1, 0);
            } else {
                int idx = random.nextInt(SHAPES.length);
                currentPiece = new Tetromino(SHAPES[idx], COLORS[idx], COLUMNS / 2 - 1, 0);
            }
            holdUsed = true;
        }
    }

    // Moving in grids in x,y coor
    public void setLocation(int dx, int dy) {
        if (!collides(currentPiece.shape, currentPiece.col + dx, currentPiece.row + dy)) {
            currentPiece.col += dx;
            currentPiece.row += dy;
        } else if (dy != 0) {
            lockPiece();
            clearRows();
            spawnPiece();
            if (collides(currentPiece.shape, currentPiece.col, currentPiece.row)) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Game Over");
            }
        }
    }

    // Rotating pieces
    public void rotate() {
        int[][] rotated = new int[currentPiece.shape[0].length][currentPiece.shape.length];
        for (int r = 0; r < currentPiece.shape.length; r++)
            for (int c = 0; c < currentPiece.shape[0].length; c++)
                rotated[c][currentPiece.shape.length - 1 - r] = currentPiece.shape[r][c];
        if (!collides(rotated, currentPiece.col, currentPiece.row)) {
            currentPiece.shape = rotated;
        }
    }

    // hard dropping pieces for those who wants to speed up tetris instead of down arrow
    public void hardDrop() {
        while (!collides(currentPiece.shape, currentPiece.col, currentPiece.row + 1)) {
            currentPiece.row++;
        }
        lockPiece();
        clearRows();
        spawnPiece();
    }

    public boolean collides(int[][] shape, int col, int row) {
        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape[0].length; c++) {
                if (shape[r][c] != 0) {
                    int newRow = row + r;
                    int newCol = col + c;
                    if (newCol < 0 || newCol >= COLUMNS || newRow >= ROWS)
                        return true;
                    if (newRow >= 0 && board[newRow][newCol] != null)
                        return true;
                }
            }
        }
        return false;
    }

    public void lockPiece() {
        int[][] shape = currentPiece.shape;
        for (int r = 0; r < shape.length; r++)
            for (int c = 0; c < shape[0].length; c++)
                if (shape[r][c] != 0) {
                    int row = currentPiece.row + r;
                    int col = currentPiece.col + c;
                    if (row >= 0) board[row][col] = currentPiece.color;
                }
        holdUsed = false; 
    }

    // Clearing rows
    public void clearRows() {
        int rowsCleared = 0;
        for (int r = ROWS - 1; r >= 0; r--) {
            boolean full = true;
            for (int c = 0; c < COLUMNS; c++) {
                if (board[r][c] == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                rowsCleared++;
                for (int i = r; i > 0; i--) board[i] = board[i-1].clone();
                board[0] = new Color[COLUMNS];
                r++;
            }
        }
        
        if (rowsCleared > 0) {
            linesCleared += rowsCleared;
            
            switch(rowsCleared) {
                case 1: score += 100; break;
                case 2: score += 200; break;
                case 3: score += 300; break;
                case 4: score += 400; break;
            }
            
            int newLevel = linesCleared / 10 + 1;
            if (newLevel > level) {
                level = newLevel;
                int newDelay = Math.max(100, baseDelay - (level-1) * 50);
                timer.setDelay(newDelay);
            }
        }
            
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setLocation(0,1);
        repaint();
    }

    // Drawing Components
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw board
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLUMNS; c++) {
                g.setColor(Color.WHITE);
                g.fillRect(c*CELL_SIZE, r*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                if (board[r][c] != null) {
                    g.setColor(board[r][c]);
                    g.fillRect(c*CELL_SIZE, r*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(c*CELL_SIZE, r*CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        // Draw current piece
        g.setColor(currentPiece.color);
        for (int r = 0; r < currentPiece.shape.length; r++)
            for (int c = 0; c < currentPiece.shape[0].length; c++)
                if (currentPiece.shape[r][c] != 0) {
                    int x = (currentPiece.col + c) * CELL_SIZE;
                    int y = (currentPiece.row + r) * CELL_SIZE;
                    g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                    g.setColor(currentPiece.color);
                }

        // Draw hold piece preview (bottom-right)
        if (holdPiece != null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("Hold", COLUMNS * CELL_SIZE + 10, ROWS * CELL_SIZE - 120);

            int offsetX = COLUMNS * CELL_SIZE + 20;
            int offsetY = ROWS * CELL_SIZE - 110;

            for (int r = 0; r < holdPiece.shape.length; r++) {
                for (int c = 0; c < holdPiece.shape[0].length; c++) {
                    if (holdPiece.shape[r][c] != 0) {
                        g.setColor(holdPiece.color);
                        g.fillRect(offsetX + c * CELL_SIZE / 2, offsetY + r * CELL_SIZE / 2, CELL_SIZE / 2, CELL_SIZE / 2);
                        g.setColor(Color.BLACK);
                        g.drawRect(offsetX + c * CELL_SIZE / 2, offsetY + r * CELL_SIZE / 2, CELL_SIZE / 2, CELL_SIZE / 2);
                    }
                }
            }
        }

        if (nextPiece != null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            g.drawString("Next shape", COLUMNS * CELL_SIZE + 10, 120);

            int offsetX = COLUMNS * CELL_SIZE + 20;
            int offsetY = 130;

            for (int r = 0; r < nextPiece.shape.length; r++) {
                for (int c = 0; c < nextPiece.shape[0].length; c++) {
                    if (nextPiece.shape[r][c] != 0) {
                        g.setColor(nextPiece.color);
                        g.fillRect(offsetX + c * CELL_SIZE / 2, offsetY + r * CELL_SIZE / 2, CELL_SIZE / 2, CELL_SIZE / 2);
                        g.setColor(Color.BLACK);
                        g.drawRect(offsetX + c * CELL_SIZE / 2, offsetY + r * CELL_SIZE / 2, CELL_SIZE / 2, CELL_SIZE / 2);
                    }
                }
            }
        }


        // Draw the score
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int stats = COLUMNS * CELL_SIZE + 10;
        g.drawString("Score: " + score, COLUMNS * CELL_SIZE + 10, 30);
        g.drawString("Lines: " + linesCleared, stats, 55);
        g.drawString("Level: " + level, stats, 80);
        
        if (pause) {
            String pauseText = "PAUSE";
            Font pauseFont = new Font("SansSerif", Font.BOLD, 34);
            g.setFont(pauseFont);
            FontMetrics metrics = g.getFontMetrics(pauseFont);

            int textWidth = metrics.stringWidth(pauseText);
            int textHeight = metrics.getHeight();

            int x = (COLUMNS * CELL_SIZE - textWidth) / 2;
            int y = (ROWS * CELL_SIZE) / 2;

            int paddingX = 10;
            int paddingY = 6;

            // Draw rectangle around the text (like a button)
            g.setColor(Color.BLUE);
            g.drawRect(x - paddingX, y - textHeight - paddingY, textWidth + 2 * paddingX, textHeight + 2 * paddingY);

            // Draw the pause text
            g.setColor(Color.BLUE);
            g.drawString(pauseText, x, y);
        }

    }

    public class Tetromino {
        int[][] shape;
        Color color;
        int row, col;

        public Tetromino(int[][] shape, Color color, int col, int row) {
            this.shape = shape;
            this.color = color;
            this.col = col;
            this.row = row;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris Game");
        
        Tetris panel = new Tetris();
       
        JPanel controls = new JPanel();
        JButton quitButton = new JButton("QUIT");
        quitButton.addActionListener(e -> System.exit(0));
        controls.add(quitButton);
       
        // Layouts
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

