package ext;

import api.CompanyService;
import api.CompanyServiceImpl;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.Properties;

import static ext.commonHelper.getProperties;

public class CompanyServiceResolver implements ParameterResolver {
    private final static String propertiesFilePath = "src/main/resources/API_x_client.properties";
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.getParameter().getType().equals(CompanyService.class)) return true;
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        Properties properties = getProperties(propertiesFilePath);
        String baseUri = properties.getProperty("baseURI");
        CompanyService service = new CompanyServiceImpl(baseUri);
        return service;
    }
}
