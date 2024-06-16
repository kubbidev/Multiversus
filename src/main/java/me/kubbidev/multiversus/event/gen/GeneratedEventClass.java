package me.kubbidev.multiversus.event.gen;

import me.kubbidev.multiversus.cache.LoadingMap;
import me.kubbidev.multiversus.event.EventDispatcher;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.multiversus.api.Multiversus;
import net.multiversus.api.event.MultiEvent;
import net.multiversus.api.event.util.Param;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Holds the generated event class for a given type of {@link MultiEvent}.
 */
public class GeneratedEventClass {

    /**
     * A loading cache of event types to {@link GeneratedEventClass}es.
     */
    private static final Map<Class<? extends MultiEvent>, GeneratedEventClass> CACHE = LoadingMap.of(clazz -> {
        try {
            return new GeneratedEventClass(clazz);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    });

    /**
     * Generate a {@link GeneratedEventClass} for the given {@code event} type.
     *
     * @param event the event type
     * @return the generated class
     */
    public static GeneratedEventClass generate(Class<? extends MultiEvent> event) {
        return CACHE.get(event);
    }

    /**
     * Pre-generates {@link GeneratedEventClass}es for known event types.
     */
    public static void preGenerate() {
        for (Class<? extends MultiEvent> eventType : EventDispatcher.getKnownEventTypes()) {
            generate(eventType);
        }
    }

    /**
     * A method handle for the constructor of the event class.
     */
    private final MethodHandle constructor;

    /**
     * An array of {@link MethodHandle}s, which can set values for each of the properties in the event class.
     */
    private final MethodHandle[] setters;

    private GeneratedEventClass(Class<? extends MultiEvent> eventClass) throws Throwable {
        // get a TypeDescription for the event class
        TypeDescription eventClassType = new TypeDescription.ForLoadedType(eventClass);

        // determine a generated class name of the event
        String eventClassSuffix = eventClass.getName().substring(MultiEvent.class.getPackage().getName().length());
        String packageWithName = GeneratedEventClass.class.getName();
        String generatedClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + eventClassSuffix;

        DynamicType.Builder<AbstractEvent> builder = new ByteBuddy(ClassFileVersion.JAVA_V8)
                // create a subclass of AbstractEvent
                .subclass(AbstractEvent.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                // using the predetermined generated class name
                .name(generatedClassName)
                // implement the event interface
                .implement(eventClassType)
                // implement all methods annotated with Param by simply returning the value from the corresponding field with the same name
                .method(isAnnotatedWith(Param.class))
                .intercept(FieldAccessor.of(NamedElement.WithRuntimeName::getInternalName))
                // implement MultiEvent#getEventType by returning the event class type
                .method(named("getEventType").and(returns(Class.class)).and(takesArguments(0)))
                .intercept(FixedValue.value(eventClassType))
                // implement AbstractEvent#mh by calling & returning the value of MethodHandles.lookup()
                .method(named("mhl").and(returns(MethodHandles.Lookup.class)).and(takesArguments(0)))
                .intercept(MethodCall.invoke(MethodHandles.class.getMethod("lookup")))
                // implement a toString method
                .withToString();

        // get a sorted array of all methods on the event interface annotated with @Param
        Method[] properties = Arrays.stream(eventClass.getMethods())
                .filter(m -> m.isAnnotationPresent(Param.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(Param.class).value()))
                .toArray(Method[]::new);

        // for each property, define a field on the generated class to hold the value
        for (Method method : properties) {
            builder = builder.defineField(method.getName(), method.getReturnType(), Visibility.PRIVATE);
        }

        // finish building, load the class, get a constructor
        Class<? extends AbstractEvent> generatedClass = builder.make().load(GeneratedEventClass.class.getClassLoader()).getLoaded();
        this.constructor = MethodHandles.publicLookup().in(generatedClass)
                .findConstructor(generatedClass, MethodType.methodType(void.class, Multiversus.class))
                .asType(MethodType.methodType(AbstractEvent.class, Multiversus.class));

        // create a dummy instance of the generated class & get the method handle lookup instance
        MethodHandles.Lookup lookup = ((AbstractEvent) this.constructor.invoke((Object) null)).mhl();

        // get 'setter' MethodHandles for each property
        this.setters = new MethodHandle[properties.length];
        for (int i = 0; i < properties.length; i++) {
            Method method = properties[i];
            this.setters[i] = lookup.findSetter(generatedClass, method.getName(), method.getReturnType())
                    .asType(MethodType.methodType(void.class, new Class[]{AbstractEvent.class, Object.class}));
        }
    }

    /**
     * Creates a new instance of the event class.
     *
     * @param api an instance of the Multiversus API
     * @param properties the event properties
     * @return the event instance
     * @throws Throwable if something goes wrong
     */
    public MultiEvent newInstance(Multiversus api, Object... properties) throws Throwable {
        if (properties.length != this.setters.length) {
            throw new IllegalStateException("Unexpected number of properties. given: " + properties.length + ", expected: " + this.setters.length);
        }

        // create a new instance of the event
        final AbstractEvent event = (AbstractEvent) this.constructor.invokeExact(api);

        // set the properties onto the event instance
        for (int i = 0; i < this.setters.length; i++) {
            MethodHandle setter = this.setters[i];
            Object value = properties[i];
            setter.invokeExact(event, value);
        }

        return event;
    }
}