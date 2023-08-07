package ext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class commonHelper {
    //Получить параметры подключения к DB из файла
    public static Properties getProperties(String path) {
        File propFile = new File(path);
        Properties JDBCProperties = new Properties();
        try {
            JDBCProperties.load(new FileReader(propFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return JDBCProperties;
    }
}
