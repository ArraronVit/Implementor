package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Implementor implements Impler {


    private static final String IMPL = "Impl";

    private static final String LS = System.lineSeparator();

    private static final String NOT_FOUND = "class not found: ";

    private static final String IO_ERR = "io error";

    private static final String IMPL_ERR = "implementation error!";

    private static final String ARGS = "invalid arguments";

    private static final String DOT_JAVA = ".java";

    private static final String STAR = "*";

    private static String implPackage;


    public static void main(String[] args) {  //solution for easy level
        if (args.length < 2 || args.length > 3) {
            System.out.println(ARGS);
            //printUsage();
            System.exit(1);
        }

        String name;
        String FileName;

        if (args.length == 3) {
            if (!"-jar".equals(args[0])) {
                System.out.println(ARGS);
                //printUsage();
                System.exit(1);
            }

            name = args[1];
            FileName = args[2].substring(0, args[2].indexOf(".jar"));
        } else {
            name = args[0];
            FileName = args[1];
        }

        File root = new File(FileName);
        Class clazz = null;
        try {
            clazz = Class.forName(name);
        } catch (ClassNotFoundException e) {
            System.out.println(NOT_FOUND + name);
            System.exit(1);
        }
    }

    private static void generateImplementation(Class clazz, Appendable out) throws IOException { //main implement method
        final String pcg = clazz.getPackage().getName();
        genPackage(pcg, out);
        out.append(LS);
        genImports(clazz, out);
        out.append(LS);
        genDeclaration(clazz, out);
        genConstructor(clazz, out);
        genMethods(clazz, out);
        out.append("}").append(LS);
    }

    private void checkIfPossible(Class clazz) throws ImplerException {
        boolean flag = true;
        for (Constructor c : clazz.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                flag = false;
            }
        }

        if (clazz.getDeclaredConstructors().length == 0)
            flag = false;


        if (flag || Modifier.isFinal(clazz.getModifiers())) {
            throw new ImplerException();
        }
    }


    public void implement(Class<?> clazz, File root) throws ImplerException { //method for user
        checkIfPossible(clazz);

        String Name = clazz.getSimpleName();
        String fileName = null;
        System.out.println("Creating implementation for " + Name);
        implPackage = clazz.getPackage().getName();
        try {
            fileName = root.getAbsolutePath() + "/";
            fileName += clazz.getPackage().getName().replaceAll("\\.", "/");
            fileName += (clazz.getPackage().getName().isEmpty()) ? "" : "/";
            (new File(fileName)).mkdirs();

            fileName += clazz.getSimpleName() + IMPL + DOT_JAVA;

            FileWriter fileWriter = new FileWriter(fileName, true); // true - append mode

            Implementor.generateImplementation(clazz, fileWriter);
            fileWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(1);
        } catch (IOException e) {
            System.out.println(IO_ERR);
            System.exit(1);
        }

        System.out.println("done: " + new File(fileName).getAbsoluteFile());
    }

    private static void genPackage(String s, Appendable out) throws IOException {
        out.append("package ").append(s).append(";").append(LS);
    }

    private static void genImports(Class clazz, Appendable out) throws IOException {
        for (Class c : findUsedClasses(clazz)) {
            out.append("import ").append(c.getCanonicalName()).append(";").append(LS);
        }
    }

    private static void genDeclaration(Class clazz, Appendable out) throws IOException {
        out.append("public class " + clazz.getSimpleName() + IMPL);
        if (clazz.isInterface())
            out.append(" implements " + clazz.getSimpleName()).append(" {");
        else
            out.append(" extends " + clazz.getSimpleName()).append(" {");
    }

    private static void genConstructor(Class clazz, Appendable out) throws IOException {
        Constructor[] c = clazz.getDeclaredConstructors();
        boolean defaultConstuctor = false;
        if (c.length == 0)
            defaultConstuctor = true;
        for (Constructor ctor : c) {
            if (Modifier.isPrivate(ctor.getModifiers()))
                continue;
            if (ctor.getParameterTypes().length == 0)
                defaultConstuctor = true;
        }

        if (!defaultConstuctor) {
            int k = 0;
            while ((Modifier.isPrivate(c[k].getModifiers())))
                ++k;
            Class[] params = c[k].getParameterTypes();
            out.append(LS);
            out.append("    public " + clazz.getSimpleName() + IMPL + "()");
            if (c[k].getExceptionTypes().length != 0) {
                out.append(" throws ");
                Class[] es = c[k].getExceptionTypes();
                for (int i = 0; i < es.length; ++i) {
                    out.append(es[i].getSimpleName());
                    if (i < es.length - 1)
                        out.append(", ");
                }
            }
            out.append("{").append(LS);
            out.append("        super(");
            for (int i = 0; i < params.length; ++i) {
                out.append("(" + params[i].getSimpleName() + ")");
                out.append(setDefault(params[i]));
                if (i < params.length - 1)
                    out.append(", ");
            }
            out.append(");").append(LS);
            out.append("    }");
            out.append(LS);
        }
    }

    private static void genMethods(Class clazz, Appendable out) throws IOException {
        for (Method m : getMethods(clazz)) {
            int mod = m.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isNative(mod) || Modifier.isPrivate(mod)
                    || !Modifier.isAbstract(mod)) {
                continue;
            }
            mod ^= Modifier.ABSTRACT;
            if (Modifier.isTransient(mod)) {
                mod ^= Modifier.TRANSIENT;
            }
            out.append(LS);
            if (m.isAnnotationPresent(Override.class)) {
                out.append("    @Override").append(LS);
            }
//            for (Annotation annotation : m.getAnnotations()) {
//                if (annotation instanceof Override)
//                    continue;
//                out.append(annotation.toString()).append(LS);
//            }
            out.append("    ");
            out.append(Modifier.toString(mod));

            out.append(" " + m.getReturnType().getSimpleName() + " ");
            out.append(m.getName() + "(");
            Class[] params = m.getParameterTypes();
            for (int i = 0; i < params.length; ++i) {
                out.append(params[i].getSimpleName() + " " + "arg" + i);
                if (i < params.length - 1)
                    out.append(", ");
            }
            out.append(")");
            Class[] exceptions = m.getExceptionTypes();

            if (exceptions.length != 0) {
                out.append(" throws ");
                for (int i = 0; i < exceptions.length; ++i) {
                    out.append(exceptions[i].getSimpleName());
                    if (i < exceptions.length - 1) {
                        out.append(", ");
                    }
                }
            }

            out.append("{").append(LS).append("        return ");
            out.append(setDefault(m.getReturnType())).append(";").append(LS);
            out.append("    }").append(LS);
        }
    }

    private static List<Method> getMethods(Class clazz) {   // get methods from interface that need to implement
        List<Method> methods = new ArrayList<Method>();
        if (clazz == null)
            return methods;

        methods.addAll(getMethods(clazz.getSuperclass()));

        for (Class interf : clazz.getInterfaces()) {
            methods.addAll(getMethods(interf));
        }

        for (Method m : clazz.getDeclaredMethods()) {

            if (Modifier.isNative(m.getModifiers())
                    || Modifier.isStatic(m.getModifiers()) || m.isSynthetic())
                continue;

            if (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())
                    || (!Modifier.isProtected(m.getModifiers())
                    && !Modifier.isPublic(m.getModifiers())
                    && !Modifier.isPrivate(m.getModifiers())
                    && clazz.getPackage().getName().equals(implPackage))) {
                boolean noAdding = false;
                for (int i = 0; i < methods.size(); ++i) {
                    Method pm = methods.get(i);

                    if (compareMethods(m, pm))     // compare signatures
                    {
//                        if (!Modifier.isAbstract(m.getModifiers())) {
//                            methods.set(i, m);
//                        }
                        methods.set(i, m);
                        noAdding = true;
                        break;
                    }
                }
                if (!noAdding) {
                    methods.add(m);
                }
            }
        }
        return methods;
    }

    private static boolean compareMethods(Method m1, Method m2) { //by signature
        if (m1.getName().equals(m2.getName())) {
            Class[] args1 = m1.getParameterTypes();
            Class[] args2 = m2.getParameterTypes();
            if (args1.length == args2.length) {
                for (int i = 0; i < args1.length; ++i) {
                    if (!args1[i].getCanonicalName().equals(args2[i].getCanonicalName())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static Class getFromArray(Class arrayType) {  //get type of element from given arr
        if (arrayType.getComponentType().isArray()) {
            return getFromArray(arrayType.getComponentType());
        } else {
            return arrayType.getComponentType();
        }
    }

    private static Set<Class> findUsedClasses(Class clazz) {   //detect all used classes from same package and standard classes
        Set<Class> classes = new HashSet<Class>();
        for (Method method : getMethods(clazz)) {
            for (Class paramType : method.getParameterTypes()) {
                if (paramType.isArray()) {
                    Class cls = getFromArray(paramType);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!paramType.isPrimitive()
                        && !paramType.getPackage().getName().startsWith("java.lang")
                        && !paramType.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(paramType);
                }
            }

            if (method.getReturnType().isArray()) {
                Class cls = getFromArray(method.getReturnType());
                if (!cls.isPrimitive())
                    classes.add(cls);
            } else if (!method.getReturnType().isPrimitive()
                    && !method.getReturnType().getPackage().getName().startsWith("java.lang")
                    && !method.getReturnType().getPackage().getName().equals(clazz.getPackage().getName())) {
                classes.add(method.getReturnType());
            }

            for (Class e : Arrays.asList(method.getExceptionTypes())) {
                if (e.isArray()) {
                    Class cls = getFromArray(e);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!e.isPrimitive()
                        && !e.getPackage().getName().startsWith("java.lang")
                        && !e.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(e);
                }
            }

        }

        for (Constructor ctr : Arrays.asList(clazz.getConstructors())) {
            for (Class paramType : ctr.getParameterTypes()) {
                if (paramType.isArray()) {
                    Class cls = getFromArray(paramType);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!paramType.isPrimitive()
                        && !paramType.getPackage().getName().startsWith("java.lang")
                        && !paramType.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(paramType);
                }
            }

            for (Class e : Arrays.asList(ctr.getExceptionTypes())) {
                if (e.isArray()) {
                    Class cls = getFromArray(e);
                    if (!cls.isPrimitive())
                        classes.add(cls);
                } else if (!e.isPrimitive()
                        && !e.getPackage().getName().startsWith("java.lang")
                        && !e.getPackage().getName().equals(clazz.getPackage().getName())
                        ) {
                    classes.add(e);
                }
            }
        }
        return classes;
    }

    private static String setDefault(Class type) {  // gives default values for given types
        if (type.isPrimitive()) {
            if (Boolean.TYPE.equals(type))
                return "false";
            else if (Void.TYPE.equals(type))
                return "";
            else
                return "0";
        } else
            return "null";
    }

}
