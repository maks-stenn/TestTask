import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Main {
    private static int columnNumber = 2;
    private static boolean isAdvancedSearch = false;

    public static void main(String[] args) {
        loadConfiguration();

        System.out.println("Введите строку: ");
        Scanner scanner = new Scanner(System.in);
        String inputText = scanner.nextLine();
        scanner.close();

        long start = System.currentTimeMillis();
        Collection<String> foundedLines = find(inputText, columnNumber, isAdvancedSearch);
        long duration = System.currentTimeMillis() - start;

        output(foundedLines, duration);
    }

    private static void loadConfiguration() {
        try (FileInputStream inputStream = new FileInputStream("application.yml")) {
            Map<String, Object> config = new Yaml().load(inputStream);
            if (config != null) {
                if (config.containsKey("columnNumber")) {
                    columnNumber = (int) config.get("columnNumber");
                }
                if (config.containsKey("isAdvancedSearch")) {
                    isAdvancedSearch = (boolean) config.get("isAdvancedSearch");
                }
            }
        } catch (IOException e) {
            System.out.println("Произошла ошибка при чтении конфигурации. Используем значения по умолчанию.");
        }
    }

    private static Collection<String> find(String inputText, int columnNumber, boolean isAdvancedSearch) {
        try (BufferedReader reader = new BufferedReader(new FileReader(getAirportsData()))) {
            String line;
            TreeMap<String, String> resultMap = new TreeMap<>();
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",\"");
                if (split.length < columnNumber) {
                    continue;
                }
                String value = split[columnNumber - 1];
                boolean res = isAdvancedSearch ? StringUtils.containsIgnoreCase(value, inputText)
                        : value.startsWith(inputText);
                if (res) {
                    resultMap.put(value, line);
                }
            }
            return resultMap.values();
        } catch (IOException e) {
            System.out.println("Произошла ошибка в процессе поиска. " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static String getAirportsData() throws FileNotFoundException {
        URL resource = Main.class.getResource("airports.dat");
        if (resource == null) {
            throw new FileNotFoundException("Файл с данными аэропортов не найден");
        }
        return resource.getFile();
    }

    private static void output(Collection<String> foundedLines, long duration) {
        for (String line : foundedLines) {
            System.out.println(line);
        }
        System.out.println("\nКоличество найденных строк: " + foundedLines.size()
                + "\nВремя, затраченное на поиск: " + duration + " мс.");
    }
}
