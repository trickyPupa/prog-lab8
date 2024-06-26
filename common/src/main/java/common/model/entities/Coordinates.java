package common.model.entities;

import common.abstractions.IInputManager;
import common.abstractions.IOutputManager;
import common.exceptions.InterruptException;

import java.io.IOException;

import static common.utils.Funcs.isInt;
import static common.utils.Funcs.isLong;
import static java.lang.Math.sqrt;

public class Coordinates implements Checkable, Comparable<Coordinates> {
    public static final int X_MAX_VALUE = Integer.MAX_VALUE;
    public static final long Y_MAX_VALUE = 155;
    public static final int X_MIN_VALUE = -878;
    public static final long Y_MIN_VALUE = Long.MIN_VALUE;

    private int x;  // > -879
    private long y;  // <= 155

    public Coordinates(int a, long b){
        x = a;
        y = b;
    }
    private Coordinates(){}

    public void setX(int x) {
        this.x = x;
    }

    public void setY(long y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public long getY() {
        return y;
    }

    public static Coordinates createCoords(IInputManager input, IOutputManager output){
        Coordinates elem = new Coordinates();

        try{
            while(true) {
                output.print("Введиите координату X фильма (целое число >-879 и <2*10^9): ");
                String line = input.nextLine();
                if (line == null || line.equals("exit")){
                    throw new InterruptException();
                }
                if (isInt(line) && Integer.parseInt(line) > -879){
                    elem.setX(Integer.parseInt(line));
                    break;
                }
                output.print("Некорректные данные.");
            }

            while(true) {
                output.print("Введиите координату Y фильма (целое число <=155 и >-9*10^18): ");
                String line = input.nextLine();
                if (line == null || line.equals("exit")){
                    throw new InterruptException();
                }
                if (isLong(line) && Long.parseLong(line) <= 155){
                    elem.setY(Long.parseLong(line));
                    break;
                }
                output.print("Некорректные данные.");
            }

        } catch (IOException e){
            output.print(e.getMessage());
        }

        return elem;
    }

    public static Coordinates createCoordsNoText(IInputManager input, IOutputManager output){
        Coordinates elem = new Coordinates();

        try{
            while(true) {
                output.print("Введиите координату X фильма (целое число >-879 и <2*10^9): \n");
                String line = input.nextLine();
                output.print(line + "\n");
                if (line == null || line.equals("exit")){
                    throw new InterruptException();
                }
                if (isInt(line) && Integer.parseInt(line) > -879){
                    elem.setX(Integer.parseInt(line));
                    break;
                }
                output.print("Некорректные данные.\n");
            }

            while(true) {
                output.print("Введиите координату Y фильма (целое число <=155 и >-9*10^18): \n");
                String line = input.nextLine();
                output.print(line + "\n");
                if (line == null || line.equals("exit")){
                    throw new InterruptException();
                }
                if (isLong(line) && Long.parseLong(line) <= 155){
                    elem.setY(Long.parseLong(line));
                    break;
                }
                output.print("Некорректные данные.\n");
            }

        } catch (IOException e){
            output.print(e.getMessage() + "\n");
        }

        return elem;
    }

    @Override
    public boolean checkItself(){
        return x > -879 && y <= 155;
    }

    @Override
    public int compareTo(Coordinates o) {
        double dif = sqrt((long) this.x * this.x + this.y * this.y) - sqrt((long) o.x * o.x + o.y * o.y);
        if (dif == 0){
            return 0;
        } else if (dif < 0){
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
