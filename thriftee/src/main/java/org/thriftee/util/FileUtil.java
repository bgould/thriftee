package org.thriftee.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    public static String readAsString(File file) throws IOException {
        InputStream in = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int) file.length());
        try {
            in = new FileInputStream(file);
            if (((int) file.length()) > 0) {
                byte[] buffer = new byte[(int) file.length()];
                for (int n = -1; (n = in.read(buffer)) > -1; ) {
                    baos.write(buffer, 0, n);
                }
            }
        } finally {
            forceClosed(in);
        }
        return baos.toString();
    }
    
    public static void writeStringToFile(String s, File file) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(s.getBytes());
        } finally {
            forceClosed(out);
        }
    }
    
    public static void createZipFromDirectory(File outputFile, String zipBaseDir, File fileBaseDir, File... extraZipDirectories) throws IOException {
        FileOutputStream os = null;
        ZipOutputStream zip = null;
        try {
            os = new FileOutputStream(outputFile);
            zip = new ZipOutputStream(os);
            addDirectory(zip, fileBaseDir, zipBaseDir);
            if (extraZipDirectories != null) {
                for (File extraDir : extraZipDirectories) {
                    addDirectory(zip, extraDir, "");
                }
            }
        } finally {
            forceClosed(zip);
            forceClosed(os);
        }
    }
    
    private static void addDirectory(ZipOutputStream zip, File source, String basePath) throws IOException {
        File[] files = source.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                String path = basePath + file.getName() + "/";
                zip.putNextEntry(new ZipEntry(path));
                addDirectory(zip, file, path);
                zip.closeEntry();
            } else {
                FileInputStream fileIn = null;
                try {
                    fileIn = new FileInputStream(file);
                    String path = basePath + file.getName();
                    zip.putNextEntry(new ZipEntry(path));
                    copy(fileIn, zip);
                    zip.closeEntry();
                } finally {
                    forceClosed(fileIn);
                }
            }
        }
    }
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        for (int n; (n = in.read(buffer)) > -1; ) {
            out.write(buffer, 0, n);
        }
    }
    

    public static void deleteRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                deleteRecursively(c);
            }
        }
        if (!file.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + file);
        }
    }
    
    public static void forceClosed(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {}
        }
    }
    
    public static void forceClosed(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {}
        }
    }
    
    public static Properties readProperties(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        return props;
    }
    
    public static Properties readProperties(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            Properties props = new Properties();
            props.load(in);
            return props;
        } finally {
            forceClosed(in);
        }
    }
    
    public static void writeProperties(File file, Properties props) throws IOException {
        writeProperties(file, props, null);
    }
    
    public static void writeProperties(File file, Properties props, String comments) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            props.store(out, comments);
        } finally {
            forceClosed(out);
        }
    }
}
