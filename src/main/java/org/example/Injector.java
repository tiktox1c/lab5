package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Annotation used to mark fields that should be auto-injected by the Injector.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface AutoInjectable {
}

/**
 * Represents an interface with a method {@code doSomething()}.
 */
interface SomeInterface {
    void doSomething();
}

/**
 * Another interface with a method {@code doSomeOther()}.
 */
interface SomeOtherInterface {
    void doSomeOther();
}

/**
 * Implementation of {@code SomeInterface} with a method {@code doSomething()} printing "A".
 */
class SomeImpl implements SomeInterface {
    /**
     * Default constructor for {@code SomeImpl}.
     */
    public SomeImpl() {
        // Default constructor
    }

    /**
     * Prints "A".
     */
    public void doSomething() {
        System.out.println("A");
    }
}

/**
 * Another implementation of {@code SomeInterface} with a method {@code doSomething()} printing "B".
 */
class OtherImpl implements SomeInterface {
    /**
     * Default constructor for {@code OtherImpl}.
     */
    public OtherImpl() {
        // Default constructor
    }

    /**
     * Prints "B".
     */
    public void doSomething() {
        System.out.println("B");
    }
}

/**
 * Implementation of {@code SomeOtherInterface} with a method {@code doSomeOther()} printing "C".
 */
class SODoer implements SomeOtherInterface {
    /**
     * Default constructor for {@code SODoer}.
     */
    public SODoer() {
        // Default constructor
    }

    /**
     * Prints "C".
     */
    public void doSomeOther() {
        System.out.println("C");
    }
}

/**
 * Represents a bean with fields marked for auto-injection.
 */
class SomeBean {
    /**
     * Field marked for auto-injection with {@code SomeInterface}.
     */
    @AutoInjectable
    private SomeInterface field1;

    /**
     * Field marked for auto-injection with {@code SomeOtherInterface}.
     */
    @AutoInjectable
    private SomeOtherInterface field2;

    /**
     * Performs actions based on injected fields.
     */
    public void foo() {
        if (field1 != null) {
            field1.doSomething();
        }
        if (field2 != null) {
            field2.doSomeOther();
        }
    }
}

/**
 * Injector class responsible for injecting dependencies into annotated fields.
 */
class Injector {
    /**
     * Map to store class mappings from properties file.
     */
    private Map<String, String> classMappings = new HashMap<>();

    /**
     * Constructs an {@code Injector} with the specified properties file path.
     *
     * @param propertiesFilePath The path to the properties file.
     */
    public Injector(String propertiesFilePath) {
        loadProperties(propertiesFilePath);
    }

    /**
     * Loads properties from the specified file and populates the classMappings map.
     *
     * @param filePath The path to the properties file.
     */
    private void loadProperties(String filePath) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            for (String key : properties.stringPropertyNames()) {
                classMappings.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Injects dependencies into annotated fields of the provided instance.
     *
     * @param instance The object instance to inject dependencies into.
     * @param <T>      The type of the instance.
     * @return The instance with injected dependencies.
     */
    public <T> T inject(T instance) {
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(AutoInjectable.class)) {
                String interfaceName = field.getType().getName();
                String implementationClassName = classMappings.get(interfaceName);

                if (implementationClassName != null) {
                    try {
                        Class<?> implementationClass = Class.forName(implementationClassName);
                        Constructor<?> constructor = implementationClass.getDeclaredConstructor();
                        Object implementationInstance = constructor.newInstance();

                        field.setAccessible(true);
                        field.set(instance, implementationInstance);
                    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                             InstantiationException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Implementation class not found for interface: " + interfaceName);
                }
            }
        }

        return instance;
    }

    /**
     * Main method for testing the Injector.
     *
     * @param args Command line arguments (not used in this context).
     */
    public static void main(String[] args) {
        try {
            Injector injector = new Injector("C:\\Users\\demin\\lab5\\src\\main\\resources\\injector.properties");
            SomeBean sb = new SomeBean();
            sb = injector.inject(sb);
            sb.foo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
