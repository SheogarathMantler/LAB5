import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * @author Aleksandr Kotov R3137
 * This is main class of console program
 */
public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        // считываем из файла с помощью Scanner
        File file = null;
        try {
            file = new File(System.getenv("FILE"));      // проверка на наличие переменной окружения
        } catch (NullPointerException e) {
            System.out.println("Cant find env variable");
            System.exit(0);
        }
        Scanner xmlScanner = null;
        try {
            xmlScanner = new Scanner(file);
        } catch (FileNotFoundException e) {   // неправильный путь к файлу или нет доступа на чтение
            if (!file.canRead()) {
                System.out.println("Permission to read denied");
            } else {
                System.out.println("File not found");
            }
            System.exit(0);
        }
        String xmlString = "";
        while(xmlScanner.hasNext()) {
            xmlString += xmlScanner.nextLine();
        }
        // создаем LinkedHashSet
        LinkedHashSet<Dragon> set = new LinkedHashSet<Dragon>();
        if (xmlString.length() > 0) {
            // создаем DOM parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))); // File -> String -> byte[] -> BAIS -> Document :))
            NodeList dragonElements = document.getDocumentElement().getElementsByTagName("dragon");
            // в цикле заполняем коллекцию элементами из запаршенного файла
            HashSet<Integer> numberOfErrors= new HashSet<>();         // Чтобы выводить номера элементов с ошибками
            boolean xmlStatus = false;
            for (int i = 0; i < dragonElements.getLength(); i++) {
                Node dragon = dragonElements.item(i);
                NamedNodeMap attributes = dragon.getAttributes();
                Integer id = null;
                try {         // проверяем есть ли айдишники и считываем их
                    id = Integer.parseInt(attributes.getNamedItem("id").getNodeValue());
                } catch (NullPointerException e){
                    id = null;
                } catch (NumberFormatException e) {
                    xmlStatus = true;
                    numberOfErrors.add(i);
                }
                LocalDateTime creationDate = null;
                try {
                    creationDate = LocalDateTime.parse(attributes.getNamedItem("creation_date").getNodeValue());
                } catch (DateTimeParseException e) {
                    xmlStatus = true;
                    numberOfErrors.add(i);;
                } catch (NullPointerException e) {
                    creationDate = null;
                }
                try {
                    String name = attributes.getNamedItem("name").getNodeValue();
                    Coordinates coords = new Coordinates(Integer.parseInt(attributes.getNamedItem("coordinates").getNodeValue().split(" ")[0]),
                            Double.parseDouble(attributes.getNamedItem("coordinates").getNodeValue().split(" ")[1]));
                    Long age = Long.parseLong(attributes.getNamedItem("age").getNodeValue());
                    String description = attributes.getNamedItem("description").getNodeValue();
                    Double wingspan = Double.parseDouble(attributes.getNamedItem("wingspan").getNodeValue());
                    String stringDragonType = attributes.getNamedItem("type").getNodeValue();
                    DragonType dragonType = dragonTypeFromFile(stringDragonType);
                    DragonCave cave = new DragonCave(Integer.parseInt(attributes.getNamedItem("cave").getNodeValue().split(" ")[0]),
                            Double.parseDouble(attributes.getNamedItem("cave").getNodeValue().split(" ")[1]));
                    set.add(new Dragon(id, name, coords, creationDate, age, description, wingspan, dragonType, cave));
                } catch (Exception e) {
                    xmlStatus = true;
                    numberOfErrors.add(i);
                }
            }
            if (xmlStatus) {
                String numbers = "";
                for (Integer integer : numberOfErrors) {
                    numbers = numbers + integer + " ";
                }
                System.out.println("Invalid fields of elements were found. These elements will not be added to collection: " + numbers);
            }
        }

        changeIds(set);

        // считываем команды из консоли
        Scanner consoleScanner = new Scanner(System.in);
        processingCommands(consoleScanner, set, false);


    }

    /**
     * Method that is reading and executing commands from console or file
     * @param scanner java.util.scanner from System.in or some file(script)
     * @param set LinkedHashSet Dragon, collection of dragons
     * @param fromScript variable that indicates source of commands
     * @throws FileNotFoundException if file not found
     * @throws ParserConfigurationException if problems with parsing script
     */
    public static void processingCommands(Scanner scanner, LinkedHashSet<Dragon> set, boolean fromScript) throws FileNotFoundException, ParserConfigurationException {
        boolean exitStatus = false;
        while (!exitStatus){
            String[] text = null;
            if (scanner.hasNext()){
                text = scanner.nextLine().replaceAll("^\\s+", "").split(" ", 2);
            } else {
                System.exit(0);
            }
            String command = text[0];
            String argument;
            try{
                argument = text[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                argument = null;
            }
            switch (command) {
                case ("help"):
                    if (argument != null) System.out.println("'help' command was detected");
                    Scanner helpscanner = new Scanner(new File("/home/s312551/lab5/help.txt"));
                    while (helpscanner.hasNext()){
                        System.out.println(helpscanner.nextLine());
                    }
                    break;
                case ("info"):
                    if (argument != null) System.out.println("'info' command was detected");
                    System.out.println("type = LinkedHashSet of Dragon's \nnumber of items = " + set.size());
                    break;
                case ("show"):
                    if (argument != null) System.out.println("'show' command was detected");
                    for (Dragon dragon : set) {
                        System.out.println(extendedDescription(dragon));
                    }
                    break;
                case ("clear"):
                    if (argument != null) System.out.println("'clear' command was detected");
                    set.clear();
                    break;
                case ("exit"):
                    if (argument != null) System.out.println("'exit' command was detected");
                    exitStatus = true;
                    break;
                case ("print_field_descending_cave"):
                    if (argument != null) System.out.println("'print_field_descending_cave' command was detected");
                    ArrayList<Integer> depthList = new ArrayList<>();
                    for (Dragon dragon : set){
                        depthList.add(dragon.getCave().getDepth());
                    }
                    depthList.sort(Collections.reverseOrder());
                    for (Integer depth : depthList){
                        System.out.println(depth);
                    }
                    break;
                case ("add"):
                    if (argument != null) System.out.println("'add' command was detected");
                    Dragon addedDragon = inputDragonFromConsole();
                    set.add(addedDragon);
                    System.out.println("new Dragon has been added");
                    break;
                case ("add_if_max"):
                    if (argument != null) System.out.println("'add_if_max' command was detected");
                    Dragon inputDragon1 = inputDragonFromConsole();
                    if (inputDragon1.getAge() > maxAgeInSet(set)){
                        set.add(inputDragon1);
                        System.out.println("new Dragon has been added");
                    } else {
                        System.out.println("new Dragon has NOT been added");
                    }
                    break;
                case ("add_if_min"):
                    if (argument != null) System.out.println("'add_if_min' command was detected");
                    Dragon inputDragon2 = inputDragonFromConsole();
                    if (inputDragon2.getAge() < minAgeInSet(set)){
                        set.add(inputDragon2);
                        System.out.println("new Dragon has been added");
                    }
                    else {
                        System.out.println("new Dragon has NOT been added");
                    }
                    break;
                case ("remove_lower"):
                    if (argument != null) System.out.println("'remove_lower' command was detected");
                    Dragon inputDragon3 = inputDragonFromConsole();
                    Long borderAge = inputDragon3.getAge();
                    LinkedHashSet<Dragon> dragonsToDelete = new LinkedHashSet<Dragon>();
                    for (Dragon dragon : set) {
                        if (dragon.getAge() < borderAge) {
                            dragonsToDelete.add(dragon);
                        }
                    }
                    set.removeAll(dragonsToDelete);
                    break;
                case ("update"):
                    try {
                        int id = Integer.parseInt(argument);
                        boolean existIdStatus = false;
                        for (Dragon dragon : set) {
                            if (dragon.getId() == id){
                                existIdStatus = true;
                            }
                        }
                        if (existIdStatus){
                            Dragon inputDragon = inputDragonFromConsole();
                            for (Dragon dragon : set){
                                if (dragon.getId() == id){
                                    dragon.update(inputDragon);
                                }
                            }
                            System.out.println("Dragon has been updated");
                        } else {
                            System.out.println("No such element id in set. Try 'show' to see available id's");
                        }

                    } catch (NumberFormatException e){
                        System.out.println("Invalid argument. Try again");
                    }
                    break;
                case ("remove_by_id"):
                    try {
                        int id = Integer.parseInt(argument);
                        boolean isRemoved = false;
                        for (Iterator<Dragon> iterator = set.iterator(); iterator.hasNext(); ) {
                            Dragon nextDragon = iterator.next();
                            if (nextDragon.getId() == id){
                                iterator.remove();
                                isRemoved = true;
                            }
                        }
                        if (isRemoved) {
                            System.out.println("Element(s) has been removed");
                        } else {
                            System.out.println("No such element in set");
                        }
                    } catch (NumberFormatException e){
                        System.out.println("Invalid argument. Try again");
                    }
                    break;
                case ("execute_script") :
                    if (fromScript) {
                        System.out.println("Danger of recursion. Command 'execute_script' skipped");
                        break;
                    } else {
                        try {
                            File script = new File(argument);
                            processingCommands(new Scanner(script), set, true); // /home/s312551/lab5/script.txt
                        } catch (FileNotFoundException e) {
                            System.out.println("File not found. Try again");
                        } catch (NullPointerException e) {
                            System.out.println("No argument. Try again");
                        }
                        break;
                    }
                case ("filter_starts_with_name") :
                    try {
                        boolean existing = false;
                        for (Dragon dragon : set) {
                            if (dragon.getName().startsWith(argument.trim())){
                                System.out.println(extendedDescription(dragon));
                                existing = true;
                            }
                        }
                        if (!existing) System.out.println("No such element");
                    } catch (NullPointerException e){
                        System.out.println("Invalid argument. Try again");
                    }
                    break;
                case ("filter_less_than_age") :
                    try {
                        boolean existing = false;
                        for (Dragon dragon : set) {
                            if (dragon.getAge() < Long.parseLong(argument)){
                                System.out.println(extendedDescription(dragon));
                                existing = true;
                            }
                        }
                        if (!existing) System.out.println("No such element");
                    } catch (NumberFormatException e){
                        System.out.println("Invalid argument. Try again");
                    }
                    break;
                case ("save") :
                    Document newDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    Element rootElement = newDocument.createElement("Dragons");
                    newDocument.appendChild(rootElement);
                    for (Dragon dragon : set) {
                        Element dragonElement = newDocument.createElement("dragon");
                        rootElement.appendChild(dragonElement);
                        dragonElement.setAttribute("id", dragon.getId().toString());
                        dragonElement.setAttribute("name", dragon.getName());
                        String coordinatesField = dragon.getCoordinates().getX() + " " + dragon.getCoordinates().getY();
                        dragonElement.setAttribute("coordinates", coordinatesField);
                        dragonElement.setAttribute("creation_date", dragon.getCreationDate().toString());
                        dragonElement.setAttribute("age", Long.toString(dragon.getAge()));
                        dragonElement.setAttribute("description", dragon.getDescription());
                        dragonElement.setAttribute("wingspan", Double.toString(dragon.getWingspan()));
                        dragonElement.setAttribute("type", dragon.getType().toString());
                        String caveField = dragon.getCave().getDepth() + " " + dragon.getCave().getNumberOfTreasures();
                        dragonElement.setAttribute("cave", caveField);
                    }
                    writeDocument(newDocument, System.getenv("FILE"));
                    break;
                default:
                    System.out.println("Invalid command. Try 'help' to see list of commands");
            }
        }
    }

    /**
     * Method that transforms Document format to xml string and writes it in file
     * @param document Document to write
     * @param path Path to file
     * @throws TransformerFactoryConfigurationError if error in transforming
     */
    public static void writeDocument(Document document, String path) throws TransformerFactoryConfigurationError {
        Transformer transformer;
        DOMSource domSource;
        BufferedOutputStream stream;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            domSource = new DOMSource(document);
            File file = new File(path);
            if (file.canWrite()) {
                stream = new BufferedOutputStream(new FileOutputStream(path));
                StreamResult result = new StreamResult(stream);
                transformer.transform(domSource, result);
            } else {
                System.out.println("Permission to edit file denied");
            }
        } catch (TransformerException e) {
            e.printStackTrace(System.out);
        } catch (FileNotFoundException e){
            System.out.println("File not found. Try again");
        }
    }
    /**
     * Method that is reading dragon fields from console to create new dragon
     * @return new dragon
     * @throws NumberFormatException if problems with format of input fields
     */
    public static Dragon inputDragonFromConsole() throws NumberFormatException {
        Scanner consoleScanner = new Scanner(System.in);
        int exceptionStatus = 0; // для проверки на исключения парсинга и несоответсвия правилам
        System.out.println("Enter name");
        String name = "";
        while (exceptionStatus == 0){
            if (consoleScanner.hasNext()){
                name = consoleScanner.nextLine();
                if ((name != null) && (name.length() > 0)) {
                    exceptionStatus = 1;
                } else {
                    System.out.println("field can't be empty. Try again");
                }
            } else {
                System.exit(0);
            }
        }
        System.out.println("Enter x coordinate (long)");
        long x = inputLongField();
        System.out.println("Enter y coordinate (Double)");
        Double y = inputDoubleField();
        Coordinates coordinates = new Coordinates(x, y);
        System.out.println("Enter age (Long, positive)");
        Long age = inputPositiveLongField();
        System.out.println("Enter description (String)");
        String description = null;
        if (consoleScanner.hasNext()){
            description = consoleScanner.nextLine();
        } else {
            System.exit(0);
        }
        System.out.println("Enter wingspan (Double, positive)");
        Double wingspan = inputPositiveDoubleField();
        System.out.println("Enter type(UNDERGROUND, AIR, FIRE)");
        String dragonType = null;
        if (consoleScanner.hasNext()){
            dragonType = consoleScanner.nextLine();
        } else {
            System.exit(0);
        }
        DragonType type = inputDragonTypeField(dragonType);
        System.out.println("Enter depth of cave (double, positive)");
        double depth = inputPositiveDoubleField();
        System.out.println("Enter number Of Treasures in cave (Double, positive)");
        Double number = inputPositiveDoubleField();
        DragonCave cave = new DragonCave((int)depth, number);
        Dragon inputDragon = new Dragon(null, name, coordinates, null, age, description, wingspan, type, cave);
        return inputDragon;
    }

    /**
     * Method used when 'show' command is called
     * @param dragon dragon which description need to be shown
     * @return String description
     */
    public static String extendedDescription(Dragon dragon) {
        return dragon.getId() + ", " + dragon.getName() + ", " + dragon.getType() + ", " + dragon.getAge() + ", " + dragon.getCoordinates().getX() +
                ", " + dragon.getCoordinates().getY() + ", " + dragon.getDescription() + ", " + dragon.getWingspan() + ", " + dragon.getCreationDate() + ", " +
                dragon.getCave().getDepth() + ", " + dragon.getCave().getNumberOfTreasures();
    }

    /**
     * Method transform String (received from console) to DragonType. If doesn't match any type then read next string from console
     * @param type String to transform
     * @return received type
     */
    public static DragonType inputDragonTypeField(String type) {
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        DragonType dragonType = DragonType.AIR;
        while (exceptionStatus == 0){
            switch (type){
                case ("UNDERGROUND"):
                    dragonType = DragonType.UNDERGROUND;
                    exceptionStatus = 1;
                    break;
                case ("AIR"):
                    dragonType = DragonType.AIR;
                    exceptionStatus = 1;
                    break;
                case ("FIRE"):
                    dragonType = DragonType.FIRE;
                    exceptionStatus = 1;
                    break;
                default:
                    System.out.println("Invalid Dragon type. Try again");
                    type = inputScanner.nextLine();
                    break;
            }
        }
        return dragonType;
    }

    /**
     * Method that reads Long field from console
     * @return Long field
     */
    public static Long inputLongField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus == 0){
                try {
                    x = Long.parseLong(inputScanner.nextLine());
                    exceptionStatus = 1;
                } catch (NumberFormatException e) {
                    System.out.println("Input must be Long. Try again");
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
    /**
     * Method that reads positive Long field from console
     * @return Long field
     */
    public static Long inputPositiveLongField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus >= 0){
                try {
                    x = Long.parseLong(inputScanner.nextLine());
                    if (x <= 0) {
                        exceptionStatus = 2;
                    } else {
                        exceptionStatus = -1;
                    }
                } catch (NumberFormatException e) {
                    exceptionStatus = 1;
                }
                switch (exceptionStatus) {
                    case (1):
                        System.out.println("Input must be long. Try again.");
                        break;
                    case (2):
                        System.out.println("Input cant be <= 0. Try again");
                        break;
                }
            }
        } else {
            System.exit(0);
        }

        return x;
    }
    /**
     * Method that reads Double field from console
     * @return Double field
     */
    public static Double inputDoubleField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus == 0){
                try {
                    x = Double.parseDouble(inputScanner.nextLine());
                    exceptionStatus = 1;
                } catch (NumberFormatException e) {
                    System.out.println("Input must be Duble. Try again.");
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
    /**
     * Method that reads positive Double field from console
     * @return Double field
     */
    public static Double inputPositiveDoubleField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
        if (inputScanner.hasNext()){
            while (exceptionStatus >= 0){
                try {
                    x = Double.parseDouble(inputScanner.nextLine());
                    if (x <= 0) {
                        exceptionStatus = 2;
                    } else {
                        exceptionStatus = -1;
                    }
                } catch (NumberFormatException e) {
                    exceptionStatus = 1;
                }
                switch (exceptionStatus) {
                    case (1):
                        System.out.println("Input must be Double. Try again.");
                        break;
                    case (2):
                        System.out.println("Input cant be < 0. Try again");
                        break;
                }
            }
        } else {
            System.exit(0);
        }
        return x;
    }
    /**
     * Method that reads string from file and transform it to DragonType. If doesn't match any type throw exception and continue
     * @param type String to transform
     * @return received dragon type
     */
    public static DragonType dragonTypeFromFile(String type){
        int exceptionStatus = 0;
        DragonType dragonType = null;
        while (exceptionStatus == 0){
            switch (type){
                case ("UNDERGROUND"):
                    dragonType = DragonType.UNDERGROUND;
                    exceptionStatus = 1;
                    break;
                case ("AIR"):
                    dragonType = DragonType.AIR;
                    exceptionStatus = 1;
                    break;
                case ("FIRE"):
                    dragonType = DragonType.FIRE;
                    exceptionStatus = 1;
                    break;
                default:
                    throw new NumberFormatException();
            }
        }
        return dragonType;
    }
    /**
     * Method finds max age of dragons in set.
     * @param set LinkedHashSet of Dragon's
     * @return Long max age
     */
    public static Long maxAgeInSet(LinkedHashSet<Dragon> set) {
        Long maxAge = 0L;
        for (Dragon dragon : set) {
            if (dragon.getAge() > maxAge) {
                maxAge = dragon.getAge();
            }
        }
        return maxAge;
    }
    /**
     * Method finds min age of dragons in set.
     * @param set LinkedHashSet of Dragon's
     * @return Long min age
     */
    public static Long minAgeInSet(LinkedHashSet<Dragon> set) {
        Long minAge = 0L;
        for (Dragon dragon : set) {
            if (dragon.getAge() < minAge) {
                minAge = dragon.getAge();
            }
        }
        return minAge;
    }

    /**
     * Method goes through the set and change duplicate id's of dragons
     * @param set LinkedHashSet of Dragon's
     */
    public static void changeIds(LinkedHashSet<Dragon> set){
        LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        for (Dragon dragon : set) {
            if (!ids.add(dragon.getId())) {
                dragon.setId(new Random().nextInt());
            }
        }
    }
}

