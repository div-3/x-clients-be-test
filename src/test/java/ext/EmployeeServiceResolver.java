package ext;

import api.EmployeeService;
import api.EmployeeServiceImpl;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Properties;

import static ext.CommonHelper.getProperties;

public class EmployeeServiceResolver implements ParameterResolver {
    private final static String PROPERTIES_FILE_PATH = "src/main/resources/API_x_client.properties";
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(EmployeeService.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Properties properties = getProperties(PROPERTIES_FILE_PATH);
        String baseUri = properties.getProperty("baseURI");
        return new EmployeeServiceImpl(baseUri);
    }
}
