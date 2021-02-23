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
import java.util.*;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        // считываем из файла с помощью Scanner
        Scanner xmlScanner = new Scanner(new File("C:\\Users\\Sheogarath\\IdeaProjects\\LAB5\\src\\DragonCollection.xml"));
        String xmlString = "";
        while(xmlScanner.hasNext()) {
            xmlString += xmlScanner.nextLine();
        }
        // создаем DOM parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))); // File -> String -> byte[] -> BAIS -> Document :))
        NodeList dragonElements = document.getDocumentElement().getElementsByTagName("dragon");
        // создаем LinkedHashSet
        LinkedHashSet<Dragon> set = new LinkedHashSet<Dragon>();
        // в цикле заполняем коллекцию элементами из запаршенного файла
        ArrayList<Integer> numberOfErrors= new ArrayList<>();         // Чтобы выводить номера элементов с ошибками
        String numbers = "";
        boolean xmlStatus = false;
        for (int i = 0; i < dragonElements.getLength(); i++) {
            Node dragon = dragonElements.item(i);
            NamedNodeMap attributes = dragon.getAttributes();
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
                set.add(new Dragon(name, coords, age, description, wingspan, dragonType, cave));
            } catch (Exception e) {
                xmlStatus = true;
                numberOfErrors.add(i);
                numbers = numbers + i + " ";
            }
        }
        if (xmlStatus) {
            System.out.println("Invalid fields of elements were found. These elements will not be added to collection: " + numbers);
        }


        // считываем команды из консоли
        Scanner consoleScanner = new Scanner(System.in);
        processingCommands(consoleScanner, set);


    }

    public static void processingCommands(Scanner scanner, LinkedHashSet<Dragon> set) throws FileNotFoundException, ParserConfigurationException {
        boolean exitStatus = false;
        while (!exitStatus){
            String[] text = scanner.nextLine().replaceAll("^\\s+", "").split(" ", 2);
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
                case ("info"): // DONE
                    if (argument != null) System.out.println("'info' command was detected");
                    System.out.println("type = LinkedHashSet of Dragon's \nnumber of items = " + set.size());
                    break;
                case ("show"): // DONE
                    if (argument != null) System.out.println("'show' command was detected");
                    for (Dragon dragon : set) {
                        System.out.println(dragon.getAge());
                    }
                    break;
                case ("clear"): // DONE
                    if (argument != null) System.out.println("'clear' command was detected");
                    set.clear();
                    break;
                case ("exit"): // DONE
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
                        Dragon inputDragon = inputDragonFromConsole();
                        boolean isUpdated = false;
                        for (Dragon dragon : set){
                            if (dragon.getId() == id){
                                dragon.update(inputDragon);
                                isUpdated = true;
                            }
                        }
                        if (isUpdated) {
                            System.out.println("Element(s) has been updated");
                        } else {
                            System.out.println("No such element in set");
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
                    try {
                        processingCommands(new Scanner(new File(argument)), set); // /home/s312551/lab5/script.txt
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found. Try again");
                    } catch (NullPointerException e) {
                        System.out.println("No argument. Try again");
                    }
                    break;
                case ("filter_starts_with_name") :
                    try {
                        boolean existing = false;
                        for (Dragon dragon : set) {
                            if (dragon.getName().startsWith(argument)){
                                System.out.println(dragon.getDescription());
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
                                System.out.println(dragon.getDescription());
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
                        dragonElement.setAttribute("name", dragon.getName());
                        String coordinatesField = dragon.getCoordinates().getX() + " " + dragon.getCoordinates().getY();
                        dragonElement.setAttribute("coordinates", coordinatesField);
                        dragonElement.setAttribute("age", Long.toString(dragon.getAge()));
                        dragonElement.setAttribute("description", dragon.getDescription());
                        dragonElement.setAttribute("wingspan", Double.toString(dragon.getWingspan()));
                        dragonElement.setAttribute("type", dragon.getType().toString());
                        String caveField = dragon.getCave().getDepth() + " " + dragon.getCave().getNumberOfTreasures();
                        dragonElement.setAttribute("cave", caveField);
                    }
                    writeDocument(newDocument, "/home/s312551/lab5/DragonCollection.xml");
                    break;
                default:
                    System.out.println("Invalid command. Try 'help' to see list of commands");
                    System.out.println(command);
            }
        }
    }
    // функция записи Document в файл
    public static void writeDocument(Document document, String path) throws TransformerFactoryConfigurationError {
        Transformer transformer;
        DOMSource domSource;
        BufferedOutputStream stream;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            domSource = new DOMSource(document);
            stream = new BufferedOutputStream(new FileOutputStream(path));
            StreamResult result = new StreamResult(stream);
            transformer.transform(domSource, result);
        } catch (TransformerException e) {
            e.printStackTrace(System.out);
        } catch (FileNotFoundException e){
            System.out.println("File not found. Try again");
        }
    }

    public static Dragon inputDragonFromConsole() throws NumberFormatException {
        Scanner consoleScanner = new Scanner(System.in);
        int exceptionStatus = 0; // для проверки на исключения парсинга и несоответсвия правилам
        System.out.println("Enter name");
        String name = "";
        while (exceptionStatus == 0){
            name = consoleScanner.nextLine();
            if ((name != null) && (name.length() > 0)) {
                exceptionStatus = 1;
            } else {
                System.out.println("field can't be empty. Try again");
            }
        }
        System.out.println("Enter x coordinate");
        long x = inputLongField();
        System.out.println("Enter y coordinate");
        Double y = inputDoubleField();
        Coordinates coordinates = new Coordinates(x, y);
        System.out.println("Enter age");
        Long age = inputPositiveLongField();
        System.out.println("Enter description");
        String description = consoleScanner.nextLine();
        System.out.println("Enter wingspan");
        Double wingspan = inputPositiveDoubleField();
        System.out.println("Enter type(UNDERGROUND, AIR, FIRE)");
        String dragonType = consoleScanner.nextLine();
        DragonType type = inputDragonTypeField(dragonType);
        System.out.println("Enter depth of cave");
        double depth = inputPositiveDoubleField();
        System.out.println("Enter number Of Treasures in cave");
        Double number = inputPositiveDoubleField();
        DragonCave cave = new DragonCave((int)depth, number);
        Dragon inputDragon = new Dragon(name, coordinates, age, description, wingspan, type, cave);
        return inputDragon;
    }
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
    public static Long inputLongField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
        while (exceptionStatus == 0){
            try {
                x = Long.parseLong(inputScanner.nextLine());
                exceptionStatus = 1;
            } catch (NumberFormatException e) {
                System.out.println("Input must be Long. Try again");
            }
        }
        return x;
    }
    public static Long inputPositiveLongField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Long x = null;
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
                    System.out.println("Input cant be < 0. Try again");
                    break;
            }
        }
        return x;
    }
    public static Double inputDoubleField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
        while (exceptionStatus == 0){
            try {
                x = Double.parseDouble(inputScanner.nextLine());
                exceptionStatus = 1;
            } catch (NumberFormatException e) {
                System.out.println("Input must be Duble. Try again.");
            }
        }
        return x;
    }
    public static Double inputPositiveDoubleField(){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
        Double x = null;
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
        return x;
    }
    public static DragonType dragonTypeFromFile(String type){
        int exceptionStatus = 0;
        Scanner inputScanner = new Scanner(System.in);
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
    public static Long maxAgeInSet(LinkedHashSet<Dragon> set) {
        Long maxAge = 0L;
        for (Dragon dragon : set) {
            if (dragon.getAge() > maxAge) {
                maxAge = dragon.getAge();
            }
        }
     return maxAge;
    }
    public static Long minAgeInSet(LinkedHashSet<Dragon> set) {
        Long minAge = 0L;
        for (Dragon dragon : set) {
            if (dragon.getAge() < minAge) {
                minAge = dragon.getAge();
            }
        }
        return minAge;
    }
}



/*
DONE - help, show, clear, exit, print_field_descending_cave, add_if_max, add_if_min, remove_lower, update id, , remove_by_id id,
execute_script, filter_starts_with_name, filter_less_than_age
UPDATE - add, info, save
NEED -
 */
