package gui;

import common.model.entities.Coordinates;
import common.model.entities.Movie;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class VisualizationPanel extends JPanel /*implements MouseWheelListener, MouseListener, MouseMotionListener*/ {
    private static final float SATURATION = 0.7f; // насыщенность
    private static final float BRIGHTNESS = 0.6f; // яркость

    protected List<Movie> objects;
    protected Movie selectedObject = null;
    private Movie animated = null;
    private double angle = 0;
    private Timer timer;

    protected BufferedImage image;

    private HashMap<String, Color> colors = new HashMap<>();


    VisualizationPanel(List<Movie> data) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1600, 1600));
        objects = data;

        var random = new Random();
        for (Movie obj : objects) {
            if (!colors.containsKey(obj.getCreator())){
                float hue = random.nextFloat();
                Color color = Color.getHSBColor(hue, SATURATION, BRIGHTNESS);
                colors.put(obj.getCreator(), color);
            }
        }

        try {
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("cinema.png"));
        } catch (IOException e) {
            e.printStackTrace();
            image = null;
        }

        /*addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);*/
    }

    public Movie getSelected(){
        return selectedObject;
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
        int width = 40;
        int height = 40;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int x = centerX + coords.getX() - width / 2;
        int y = (int) (centerY - coords.getY()) - height / 2;

        var g2 = (Graphics2D) g;
        g2.setColor(colors.get(mov.getCreator()));

        if(image != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Создаем новое изображение с цветовой заливкой
            BufferedImage coloredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gImg = coloredImage.createGraphics();
            gImg.drawImage(image, 0, 0, width, height, null);
            gImg.setComposite(AlphaComposite.SrcAtop);

            gImg.setColor(colors.get(mov.getCreator()));
            gImg.fillRect(0, 0, width, height);
            gImg.dispose();

            // Рисуем раскрашенное изображение
            g2.drawImage(coloredImage, x, y, width, height, this);
        }
        else {
            g2.fillRect(x, y, width, height);
        }

        if (mov == selectedObject) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(x, y, width, height);
        }
    }

    private int objX(Movie obj){
        return obj.getCoordinates().getX() + getWidth() / 2;
    }

    private int objY(Movie obj){
        return getHeight() / 2 - (int) obj.getCoordinates().getY();
    }

    protected boolean containsMovie(Movie mov, int mx, int my) {
        Coordinates coords = mov.getCoordinates();
        int x = coords.getX() + getWidth() / 2;
        int y = getHeight() / 2 - (int) coords.getY();

        int size = 20;

        return mx >= x - size / 2 && mx <= x + size / 2
                && my >= y - size / 2 && my <= y + size / 2;
    }

    @Override
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
    }

    private void appearance(Movie obj){
        animated = obj;

        angle = 0.0d;
        timer = new Timer(50, e -> {
            angle += Math.toRadians(5);
            if (angle == 2 * Math.PI) {
                timer.stop();
            }
            System.out.println(angle);
            repaint();
        });
        timer.start();

        animated = null;
    }

    private void fading(Movie obj){
        animated = obj;

        angle = 0.0d;
        timer = new Timer(50, e -> {
            angle -= Math.toRadians(5);
            if (angle == -2 * Math.PI) {
                timer.stop();
            }
            System.out.println(angle);
            repaint();
        });
        timer.start();

        animated = null;
    }

    public void update(List<Movie> newData){
        for (Movie obj : newData) {
            if (!objects.contains(obj)) {
                scrollRectToVisible(new Rectangle(objX(obj) - 50, objY(obj) - 50, 100, 100));
//                appearance(obj);
            }
        }
        for (Movie obj : objects) {
            if(!newData.contains(obj)){
                scrollRectToVisible(new Rectangle(objX(obj)- 50, objY(obj) - 50, 100, 100));
//                fading(obj);

                /*try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/
            }
        }

        objects = newData;
        selectedObject = null;
        repaint();
    }
}