import java.time.LocalDateTime;
import java.util.Random;

public class Dragon {
    private Integer id = new Random().nextInt(); //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.LocalDateTime creationDate = LocalDateTime.now(); //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Long age; //Значение поля должно быть больше 0, Поле не может быть null
    private String description; //Поле может быть null
    private Double wingspan; //Значение поля должно быть больше 0, Поле может быть null
    private DragonType type; //Поле может быть null
    private DragonCave cave; //Поле может быть null
    // стандартный конструктор
    public Dragon(String n, Coordinates coords, Long age, String d,
                  Double w, DragonType t, DragonCave c ) {
        if ((n != null) && (n.length() != 0)) {
            this.name = n;
        } else {
            throw new NumberFormatException();
        }
        if (coords != null) {
            this.coordinates = coords;
        } else {
            throw new NumberFormatException();
        }
        if ((age > 0)) {
            this.age = age;
        } else {
            throw new NumberFormatException();
        }
        this.description = d;
        if (w > 0) {
            this.wingspan = w;
        } else {
            throw new NumberFormatException();
        }
        this.type = t;
        this.cave = c;

    }
    public Integer getId() {return id;}
    public String getName() {return name;}
    public Coordinates getCoordinates() {return coordinates;}
    public Long getAge(){ return age; }
    public java.time.LocalDateTime getCreationDate() {return creationDate;}
    public String getDescription(){
        return description;
    }
    public Double getWingspan() {return wingspan;}
    public DragonType getType() {return type;}
    public DragonCave getCave(){ return cave; }
    public void update(Dragon dragon) {
        this.id = dragon.getId();
        this.name = dragon.getName();
        this.coordinates = dragon.getCoordinates();
        this.age = dragon.getAge();
        this.creationDate = dragon.getCreationDate();
        this.description = dragon.getDescription();
        this.wingspan = dragon.getWingspan();
        this.type = dragon.getType();
        this.cave = dragon.getCave();
    }
}

class Coordinates {
    private long x;
    private Double y; //Поле не может быть null
    public Coordinates(long x,Double y){
        this.x = x;
        if (y != null) {
            this.y = y;
        }
    }
    public long getX(){
        return x;
    }
    public Double getY(){
        return y;
    }
}

class DragonCave {
    private int depth;
    private Double numberOfTreasures; //Поле не может быть null, Значение поля должно быть больше 0
    public DragonCave(int d, Double n) {
        this.depth = d;
        if (n > 0) {
            this.numberOfTreasures = n;
        }
    }
    public int getDepth(){
        return depth;
    }
    public Double getNumberOfTreasures() {
        return numberOfTreasures;
    }
}

enum DragonType {
    UNDERGROUND,
    AIR,
    FIRE;
}
