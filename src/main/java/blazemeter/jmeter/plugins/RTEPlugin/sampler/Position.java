public class Position {
    private int row;
    private int column;

    public Position(int xcol, int yrow ){
        row = yrow;
        column = xcol;
    }

    public int getRow(){
        return row;
    }

    public int getCol(){
        return column;
    }
}
