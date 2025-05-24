package javaapplication73;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import static javaapplication73.NewClass.diff;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

public class JavaApplication73 extends Applet implements Runnable {

    //INITIALISING VARIABLES
    int x = 0, y = 0, v = 35, z = 280;
    boolean up = false, down = false, left = false, right = false;
    private Image offscreenImage;
    private Graphics offscreenGraphics;
    private Point point;
    private Random random;
    public static int score = 0;

    @Override
    public void init() {
        //INITIALISING APPLET
        setSize(410, 410);
        setBackground(Color.BLACK);

        random = new Random();
        point = new Point(random.nextInt(getWidth() - 22), random.nextInt(getHeight() - 22));

        //KeyListener THAT DETERMINES PAC-MANs BEARINGS AND DIRECTION.
        addKeyListener(new KeyAdapter() {
             @Override
            public void keyPressed(KeyEvent ke) {
                switch (ke.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        up = true;
                        down = false;
                        left = false;
                        right = false;
                        v = 130;
                        z = 280;
                        break;
                    case KeyEvent.VK_DOWN:
                        up = false;
                        down = true;
                        left = false;
                        right = false;
                        v = 320;
                        z = 260;
                        break;
                    case KeyEvent.VK_LEFT:
                        up = false;
                        down = false;
                        left = true;
                        right = false;
                        v = 210;
                        z = 290;
                        break;
                    case KeyEvent.VK_RIGHT:
                        up = false;
                        down = false;
                        left = false;
                        right = true;
                        v = 35;
                        z = 280;
                        break;
                }
            }
});
        

        setFocusable(true);
        requestFocusInWindow();
        Thread t = new Thread(this);
        t.start();
        
         
        //INITIALISES ProgressBar WITH PROPERTIES
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(true); // Use indeterminate mode for unknown duration
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        progressBar.setLayout(new BorderLayout());

        //CREATES A JOptionPane THAT HOLDS THE ProgressBar
        JOptionPane optionPane = new JOptionPane(progressBar, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        
        //CREATES AND DISPLAYS A DIALOG CONTAINING THE JOptionPane
        JDialog dd = optionPane.createDialog("STARTING GAME!");
        dd.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        //STARTS THE ProgressBar IN A SEPERATE THREAD
        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(100); 
                    progressBar.setValue(i);
                    progressBar.setString("Progress: " + i + "%");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //CLOSES THE DIALOG WHEN ProgressBar IS DONE
            dd.dispose(); 
        }).start();
        
        dd.setVisible(true);
        
        
    }
@Override
    public void update(Graphics g) {
       
        if (offscreenImage == null) {
            offscreenImage = createImage(this.getWidth(), this.getHeight());
            offscreenGraphics = offscreenImage.getGraphics();
        }
        offscreenGraphics.setColor(getBackground());
        offscreenGraphics.fillRect(0, 0, this.getWidth(), this.getHeight());

        paint(offscreenGraphics);
        g.drawImage(offscreenImage, 0, 0, this);
    }

    //PAINTING (PAC-MAN/DOT/SCORE LABEL)
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        enableAntialiasing(g2d);

        g2d.setColor(Color.YELLOW);
        g2d.fillArc(x, y, 20, 20, v, z);

        g2d.setColor(Color.RED);
        g2d.fillOval(point.x, point.y, 10, 10);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font(null,Font.BOLD,16));
        g2d.drawString("SCORE: " + score, 290, 20);
        
    }

    private void enableAntialiasing(Graphics2D g2d) {
        //ENHANCES PAC-MANs GRAPHICS
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    //CONTROLS THE PLACING OF THE DOT AFTER BEING ATE BY PAC-MAN BY RANDOMISING ITS NEXT LOCATION
    @Override
    public void run() {
        try {
            while (true) {
                if (up && y > 0) {
                    y -= 1;
                }
                if (down && y < getHeight() - 20) {
                    y += 1;
                }
                if (left && x > 0) {
                    x -= 1;
                }
                if (right && x < getWidth() - 20) {
                    x += 1;
                }
                if (Math.abs(x - point.x) < 18 && Math.abs(y - point.y) < 18) {
                    point = new Point(random.nextInt(getWidth() - 20), random.nextInt(getHeight() - 20));
                    score++;
                }
                
                
                Thread.sleep(diff);
                repaint();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
}

