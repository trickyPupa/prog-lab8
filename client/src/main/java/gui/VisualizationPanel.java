package gui;

import common.model.entities.Coordinates;
import common.model.entities.Movie;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class VisualizationPanel extends JPanel {
    private static final float SATURATION = 0.7f; // насыщенность
    private static final float BRIGHTNESS = 0.8f; // яркость

    protected List<Movie> objects;
    private Movie selectedObject = null;
    protected double scale = 1;

    private HashMap<String, Color> colors = new HashMap<>();

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
                        JOptionPane.showMessageDialog(null, obj.toString());
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
    }

    public void setScale(double scale) {
        this.scale = scale;
        repaint();
    }

    void addObject(Movie obj) {
        objects.add(obj);
        var random = new Random();
        if (!colors.containsKey(obj.getCreator())){
            colors.put(obj.getCreator(), new Color(random.nextInt(256),
                    random.nextInt(256), random.nextInt(256)));
        }
        repaint();
    }

    void removeObject(Movie obj) {
        objects.remove(obj);
        repaint();
    }

    void updateObject(Movie obj) {
        repaint();
    }

    void draw(Graphics g, Movie mov) {
        Coordinates coords = mov.getCoordinates();
        g.setColor(colors.get(mov.getCreator()));

        int width = (int) Math.round(10 * scale);
        int height = (int) Math.round(10 * scale);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int x = centerX + (int) Math.round(coords.getX() * scale);
        int y = (int) (centerY - Math.round(coords.getY() * scale));

        g.fillRect(x - width / 2, y - height / 2, width, height);
    }

    boolean containsMovie(Movie mov, int mx, int my) {
        Coordinates coords = mov.getCoordinates();
        return mx >= coords.getX() - 5 && mx <= coords.getX() + 5 && my >= coords.getY() - 5 && my <= coords.getY() + 5;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        g2d.drawLine(centerX, 0, centerX, getHeight()); // Y axis
        g2d.drawLine(0, centerY, getWidth(), centerY); // X axis


        for (Movie obj : objects) {
            draw(g, obj);
        }
    }

    private void animation(){
        new Timer(30, new ActionListener() {
            int dx = 2;
            int dy = 2;

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Movie obj : objects) {
                    ;
                }
                repaint();
            }
        }).start();
    }
}