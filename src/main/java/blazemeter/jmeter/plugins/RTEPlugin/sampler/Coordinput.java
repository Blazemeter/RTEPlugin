
public class Coordinput {
    private Position position;
    private String input;

    public Coordinput(Position pos, String in){
        position = pos;
        input = in;
    }

    public Position getPosition(){
        return position;
    }

    public String getInput(){
        return input;
    }
}
