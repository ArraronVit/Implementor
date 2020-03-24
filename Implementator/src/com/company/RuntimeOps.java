package com.company;

import javax.tools.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RuntimeOps {    //for compilation and generates jars at runtime

    private static final String DOT_JAVA = ".java";

    private static final String STAR = "*";


    public static void compile(String path) throws IOException { //compiles all files in given directory
        File dir = new File(path);
        File[] javaFiles = dir.listFiles(
                new FilenameFilter() {
                    public boolean accept(File file, String name) {
                        return name.endsWith(DOT_JAVA);
                    }
                });

        var javaCompiler = ToolProvider.getSystemJavaCompiler();

        var compilationUnits = new String[javaFiles.length];
        for (int i = 0; i < javaFiles.length; ++i) {
            compilationUnits[i] = javaFiles[i].getPath();
        }

        int exitCode = javaCompiler.run(null, null, null, compilationUnits);

        if (exitCode != 0) {
            System.out.println("some compile errors occurred!");
        }
    }


    public static void compileFile(String pathToFile) throws IOException { //compile specified one file
        var javaCompiler = ToolProvider.getSystemJavaCompiler();

        String[] compilationUnits = {pathToFile};

        int exitCode = -1;
        try {
            exitCode = javaCompiler.run(null, null, null, compilationUnits);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        if (exitCode != 0) {
            System.out.println("some compile errors occurred!: exitcode: " + exitCode);
            System.out.println(compilationUnits[0] + " vs " + pathToFile);
        }
    }

    public static void createJar(String pathToJar, String pathToClass) throws IOException { //create specified one jar file

        FileOutputStream fos = null;
        JarOutputStream jarOutputStream = null;
        try {
            var file = new File(pathToJar);

            if(file.getParentFile()!= null)
                file.getParentFile().mkdirs();

            if(file!= null)
                file.createNewFile();
            else
            {
                System.out.println("invalid path to jar file to be created");
                System.exit(1);
            }

            fos = new FileOutputStream(pathToJar);

            var newClassPath = pathToClass.substring(pathToClass.indexOf("tmp/") + 4);

            var manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            //manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, newClassPath);
            jarOutputStream = new JarOutputStream(fos, manifest);

            jarOutputStream.putNextEntry(new ZipEntry(newClassPath));

            var bis = new BufferedInputStream(new FileInputStream(pathToClass));

            int bytesRead;
            byte[] buffer = new byte[8 * 1024];
            while ((bytesRead = bis.read(buffer)) != -1) {
                jarOutputStream.write(buffer, 0, bytesRead);
            }
            jarOutputStream.closeEntry();
            jarOutputStream.close();
            fos.close();

        } catch (IOException e) {

            jarOutputStream.closeEntry();
            jarOutputStream.close();
            fos.close();
            throw new IOException(e.getMessage());
        }
    }

    public static void doFromConsole(String rootPath, Class clazz) throws IOException {
        var s = rootPath + "\\" + clazz.getPackage().getName().replaceAll("\\.", "/") + "\\" + STAR + DOT_JAVA;
        Runtime.getRuntime().exec("javac  " + s);
        var s2 = clazz.getPackage().getName() + " " + rootPath + "/" + clazz.getCanonicalName().toString() + " " + rootPath;
        Runtime.getRuntime().exec("jar cfe " + s2);
    }
}
