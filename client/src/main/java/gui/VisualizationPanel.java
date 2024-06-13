package gui;

import common.model.entities.Coordinates;
import common.model.entities.Movie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class VisualizationPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {
    private static final float SATURATION = 0.7f; // насыщенность
    private static final float BRIGHTNESS = 0.8f; // яркость

    protected List<Movie> objects;
    private Movie selectedObject = null;
    private ArrayList<Movie> added = new ArrayList<>();
    private ArrayList<Movie> removed = new ArrayList<>();

    private HashMap<String, Color> colors = new HashMap<>();

    private double zoomFactor = 1;
    private double prevZoomFactor = 1;
    private boolean zoomer;
    private boolean dragger;
    private boolean released;
    private double xOffset = 0;
    private double yOffset = 0;
    private int xDiff;
    private int yDiff;
    private Point startPoint;

    VisualizationPanel(List<Movie> data) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1600, 1600));
        objects = data;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Movie obj : objects) {
                    if (containsMovie(obj, e.getX(), e.getY())) {
                        selectedObject = obj;
                        JOptionPane.showMessageDialog(null, obj.toString(), "title",
                                JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
        });

        var random = new Random();
        for (Movie obj : objects) {
            if (!colors.containsKey(obj.getCreator())){
                float hue = random.nextFloat();
                Color color = Color.getHSBColor(hue, SATURATION, BRIGHTNESS);
                colors.put(obj.getCreator(), color);
            }
        }

        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    // создаем очередь добавленных элементов, чтобы потом нарисовать их
    void addObject(Movie obj) {
        objects.add(obj);
        var random = new Random();
        if (!colors.containsKey(obj.getCreator())){
            colors.put(obj.getCreator(), new Color(random.nextInt(256),
                    random.nextInt(256), random.nextInt(256)));
        }
//        repaint();
    }

    // создаем очередь удаленных элементов, чтобы потом стереть их
    void removeObject(Movie obj) {
        objects.remove(obj);
//        repaint();
    }

    void updateObject(Movie obj) {
        repaint();
    }

    void draw(Graphics g, Movie mov) {
        Coordinates coords = mov.getCoordinates();
        g.setColor(colors.get(mov.getCreator()));

        int width = 10;
        int height = 10;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int x = centerX + coords.getX();
        int y = (int) (centerY - coords.getY());

        g.fillRect(x - width / 2, y - height / 2, width, height);
    }

    private int objX(Movie obj){
        return obj.getCoordinates().getX() + getWidth() / 2;
    }

    private int objY(Movie obj){
        return getHeight() / 2 - (int) obj.getCoordinates().getY();
    }

    boolean containsMovie(Movie mov, int mx, int my) {
        Coordinates coords = mov.getCoordinates();
        int x = coords.getX() + getWidth() / 2;
        int y = getHeight() / 2 - (int) coords.getY();
        return mx >= x - 5 && mx <= x + 5 && my >= y - 5 && my <= y + 5;
    }

    /*@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.drawLine(centerX, 0, centerX, getHeight());
        g2d.drawLine(0, centerY, getWidth(), centerY);

        for (Movie obj : objects) {
            draw(g, obj);
        }
    }*/

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        if (zoomer) {
            AffineTransform at = new AffineTransform();

            double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
            double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

            double zoomDiv = zoomFactor / prevZoomFactor;

            xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * xRel;
            yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * yRel;

            at.translate(xOffset, yOffset);
            at.scale(zoomFactor, zoomFactor);
            prevZoomFactor = zoomFactor;
            g2.transform(at);
            zoomer = false;
        }

        if (dragger) {
            AffineTransform at = new AffineTransform();
            at.translate(xOffset + xDiff, yOffset + yDiff);
            at.scale(zoomFactor, zoomFactor);
            g2.transform(at);

            if (released) {
                xOffset += xDiff;
                yOffset += yDiff;
                dragger = false;
            }

        }

        // All drawings go here

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        g2.setColor(Color.BLACK);
        g2.drawLine(centerX, 0, centerX, getHeight());
        g2.drawLine(0, centerY, getWidth(), centerY);

        for (Movie obj : objects) {
            draw(g2, obj);
        }
    }

    private void animation(){
        new Timer(20, new ActionListener() {
            int dx = 2;
            int dy = 2;

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Movie obj : objects) {
                    appearance(obj);
                }
                repaint();
            }
        }).start();
    }

    private void appearance(Movie obj){
        ;
    }

    private void fading(Movie obj){
        ;
    }

    public void update(List<Movie> newData){
        for (Movie obj : newData) {
            if (!objects.contains(obj)) {
                appearance(obj);
                scrollRectToVisible(new Rectangle(objX(obj) - 50, objY(obj) - 50, 100, 100));
            }
        }
        for (Movie obj : objects) {
            if(!newData.contains(obj)){
                fading(obj);
                scrollRectToVisible(new Rectangle(objX(obj)- 50, objY(obj) - 50, 100, 100));
                /*try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/
            }
        }

        objects = newData;
    }

    // события мыши
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

        zoomer = true;

        //Zoom in
        if (e.getWheelRotation() < 0) {
            zoomFactor *= 1.1;
            repaint();
        }
        //Zoom out
        if (e.getWheelRotation() > 0) {
            zoomFactor = Math.max(zoomFactor / 1.1, 1);
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point curPoint = e.getLocationOnScreen();
        xDiff = curPoint.x - startPoint.x;
        yDiff = curPoint.y - startPoint.y;

        dragger = true;
        repaint();

    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        released = false;
        startPoint = MouseInfo.getPointerInfo().getLocation();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        released = true;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}