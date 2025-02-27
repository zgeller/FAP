/*   
 * Copyright 2024 Zoltán Gellér
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fap.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * File utilities.
 * 
 * @author Zoltán Gellér
 * @version 2025.02.27.
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static final String fseparator = System.getProperty("file.separator");

    public static final String userFolder = System.getProperty("user.dir");

    /**
     * Deserializes an object from a file.
     * 
     * @param fname the file name
     * @return the deserialized object
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object loadObject(String fname) throws FileNotFoundException, IOException, ClassNotFoundException {
        Object obj = null;
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fname)))) {
            obj = ois.readObject();
        }
        return obj;
    }

    /**
     * Deserializes an object from a file.
     * 
     * @param file the file
     * @return the deserialized object
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object loadObject(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
        Object obj = null;
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            obj = ois.readObject();
        }
        return obj;
    }

    /**
     * Serializes an object to a file.
     * 
     * @param fname the file name
     * @param obj   the object to serialize
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveObject(String fname, Object obj) throws FileNotFoundException, IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fname)))) {
            oos.writeObject(obj);
        }
    }

    /**
     * Serializes an object to a file.
     * 
     * @param file the file
     * @param obj  the object to serialize
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveObject(File file, Object obj) throws FileNotFoundException, IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(obj);
        }
    }

    /**
     * Recursively deletes a folder.
     * 
     * @param folder the folder to be deleted
     * @return true {@code true} if the folder has been successfully deleted
     * @throws SecurityException
     */
    public static boolean deleteFolder(File folder) {
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null)
                for (File file : files) {
                    if (file.isDirectory())
                        deleteFolder(file);
                    else
                        file.delete();
                }
        }
        return folder.delete();
    }

    /**
     * Recursively deletes a folder.
     * 
     * @param fname the name of the folder to be deleted
     * @return {@code true} if the folder has been successfully deleted
     * @throws SecurityException
     */
    public static boolean deleteFolder(String fname) {
        return deleteFolder(new File(fname));
    }

    /**
     * Deletes the files and folders with the given names.
     * 
     * @param fnames the names of the files and folders to be deleted
     * @return {@code null} if the files and folders have been successfully deleted
     *         or the name of the first file or folder which couldn't be deleted
     */
    public static String deleteFiles(String... fnames) {
        for (String fname : fnames) {
            File file = new File(fname);
            try {
                if (file.exists()) {
                    boolean ok = true;
                    if (file.isDirectory())
                        ok = deleteFolder(file);
                    else
                        ok = file.delete();
                    if (!ok)
                        return fname;
                }
            } catch (Exception e) {
                return fname;
            }
        }
        return null;
    }

}
