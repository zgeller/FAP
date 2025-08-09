/*   
 * Copyright 2024-2025 Zoltán Gellér
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fap.core.data.Dataset;
import fap.core.data.TimeSeries;
import fap.io.TimeSeriesTextFileReader;
import fap.io.TimeSeriesTextFileWriter;

/**
 * Dataset utilities.
 * 
 * @author Zoltán Gellér
 * @version 2025.03.31.
 */
public final class DatasetUtils {

    private DatasetUtils() {
    }

    /**
     * The character used to separate values. The default value is comma.
     */
    public static char separator = ',';

    /**
     * The root folder of the datasets. The default value is {@code DATASETS}.
     */
    public static String root = "DATASETS";

    /**
     * Returns the path of the dataset.
     * 
     * @param dsname the name of the dataset
     * @return the path of the dataset
     */
    public static String getDatasetPath(String dsname) {
        return root == null || root.length() == 0 ? dsname : root + FileUtils.fseparator + dsname;
    }

    /**
     * Loads the dataset with the given name. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>Data points are separated by the default {@link #separator}.
     *  <li>Data points don't contain x-coordinates.
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param dsname the name of the dataset
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname) throws IOException {
        return loadDataset(dsname, true, true);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>Data points are separated by the default {@link #separator}.
     *  <li>Data points don't contain x-coordinates.
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param folder the name of the folder
     * @param dsname the name of the dataset
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String folder, String dsname) throws IOException {
        return loadDataset(folder, dsname, true, true);
    }

    /**
     * Loads the dataset with the given name. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>Data points are separated by the default {@link #separator}.
     *  <li>Data points don't contain x-coordinates.
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param dsname the name of the dataset
     * @param train  indicates whether to load training set
     * @param test   indicates whether to load test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname, boolean train, boolean test)
            throws IOException {
        return loadDataset(dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>Data points are separated by the default {@link #separator}.
     *  <li>Data points don't contain x-coordinates.
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param folder the name of the folder
     * @param dsname the name of the dataset
     * @param train  indicates whether to load the training set
     * @param test   indicates whether to load the test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String folder, 
                                      String dsname, 
                                      boolean train, 
                                      boolean test) 
            throws IOException {
        return loadDataset(folder, dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>Data points are separated by the given {@code separator}
     *  <li>Data points don't contain x-coordinates
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param train     indicates whether to load the training set
     * @param test      indicates whether to load the test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname, 
                                      char separator, 
                                      boolean train,
                                      boolean test) 
            throws IOException {
        return loadDataset(dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>Data points are separated by the given {@code separator}.
     *  <li>Data points don't contain x-coordinates.
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param folder    the folder
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param train     indicates whether to load the training set
     * @param test      indicates whether to load the test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String folder, 
                                      String dsname, 
                                      char separator,
                                      boolean train, 
                                      boolean test) 
            throws IOException {
        return loadDataset(folder, dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>The data points are separated by the given {@code separator}.
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param hasXValue indicates whether the list of input values contain
     *                  x-coordinates of points
     * @param train     indicates whether to load the training set
     * @param test      indicates whether to load the test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname, 
                                      char separator, 
                                      boolean hasXValue,
                                      boolean train, 
                                      boolean test) 
            throws IOException {
        return loadDataset(root, dsname, separator, hasXValue, train, test);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:
     * <ol>
     *  <li>The training set is stored in the file {@code dsname\dsname+"_TRAIN"}.
     *  <li>The test set is stored in the file {@code dsname\dsname+"_TEST"}.
     *  <li>Data points are separated by the given {@code separator}.
     * </ol>
     * 
     * <p>
     * The elements of the test set are added after the elements of the training
     * set.
     * 
     * @param folder    the folder
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param hasXValue indicates whether the list of input values contain
     *                  x-coordinates of points
     * @param train     indicates whether to load the training set
     * @param test      indicates whether to load the test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String folder, 
                                      String dsname, 
                                      char separator,
                                      boolean hasXValue, 
                                      boolean train, 
                                      boolean test) 
            throws IOException {

        if (folder == null)
            folder = "";
        else if (folder.length() > 0)
            folder += FileUtils.fseparator;

        String trainFilename = folder + dsname + FileUtils.fseparator + dsname + "_TRAIN";
        String testFilename = folder + dsname + FileUtils.fseparator + dsname + "_TEST";

        Dataset trainSet = null;
        Dataset testSet = null;

        TimeSeriesTextFileReader reader;

        if (train) {
            reader = new TimeSeriesTextFileReader(trainFilename, separator, hasXValue);
            trainSet = reader.load();
        }

        if (test) {
            reader = new TimeSeriesTextFileReader(testFilename, separator, hasXValue);
            testSet = reader.load();
        }

        // preparing dataset
        Dataset dataset = null;

        if (train && test) {
            if (trainSet.size() > testSet.size()) {
                dataset = trainSet;
                dataset.addAll(testSet);
                testSet.clear();
                testSet = null;
            } else {
                dataset = testSet;
                dataset.addAll(0, trainSet);
                trainSet.clear();
                trainSet = null;
            }
        } else if (train)
            dataset = trainSet;
        else
            dataset = testSet;

        // setting indices
        int index = 0;
        for (TimeSeries ts : dataset)
            ts.setIndex(index++);

        return dataset;
    }

    /**
     * Saves the dataset without x-coordinates, data points are separated by the
     * default {@code separator}.
     * 
     * @param dsname  the name of the file
     * @param dataset the dataset to be saved
     * @throws IOException
     */
    public static void saveDataset(String dsname, Dataset dataset) throws IOException {
        saveDataset(dsname, separator, dataset);
    }

    /**
     * Saves the dataset without x-coordinates.
     * 
     * @param dsname    the name of the file
     * @param separator the character which separates the values
     * @param dataset   the dataset to be saved
     * @throws IOException
     */
    public static void saveDataset(String dsname, char separator, Dataset dataset)
            throws IOException {
        saveDataset(dsname, separator, false, dataset);
    }

    /**
     * Saves the dataset.
     * 
     * @param dsname    the name of the file
     * @param separator the character which separates the values
     * @param hasXValue indicates whether the output should contain x-coordinates of points
     * @param dataset   the dataset to be saved
     * @throws IOException
     */
    public static void saveDataset(String dsname, 
                                   char separator, 
                                   boolean hasXValue,
                                   Dataset dataset) 
            throws IOException {
        TimeSeriesTextFileWriter writer = new TimeSeriesTextFileWriter(dsname, separator, hasXValue);
        writer.write(dataset);
    }

    /**
     * Loads the labels from the given file. The assumption is that each line
     * contains one label.
     * 
     * @param fname the name of the file
     * @return list of the labels
     * @throws IOException
     */
    public static List<Double> loadLabelsList(String fname) throws IOException {
        List<Double> labels = new ArrayList<Double>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
            String line = reader.readLine();
            while (line != null && line.length() > 0) {
                double label = Double.parseDouble(line);
                labels.add(label);
                line = reader.readLine();
            }
        }
        return labels;
    }

    /**
     * Loads the labels from the given file. The assumption is that each line
     * contains one label.
     * 
     * @param fname the name of the file
     * @return array of the labels
     * @throws IOException
     */
    public static double[] loadLabelsArray(String fname) throws IOException {
        List<Double> labelsList = loadLabelsList(fname);
        int size = labelsList.size();
        double[] labels = new double[size];
        for (int i = 0; i < size; i++)
            labels[i] = labelsList.get(i);
        return labels;
    }

    /**
     * Creates a new dataset based on the given list of labels. Time series are
     * indexed according to the positions of the labels within the array.
     * 
     * @param labels list of labels
     * @return the new dataset
     */
    public static Dataset createDataSet(List<Double> labels) {
        return createDataSet(labels, 0, labels.size() - 1);
    }

    /**
     * Creates a new dataset based on the given list of labels. Time series are
     * indexed according to the positions of the labels within the array.
     * 
     * @param labels list of labels
     * @param first  index of the first label to be included in the dataset
     * @param last   index of the last label to be included in the dataset
     * @return the new dataset
     */
    public static Dataset createDataSet(List<Double> labels, int first, int last) {
        Dataset dataset = new Dataset();
        for (int i = first; i < last + 1; i++) {
            TimeSeries series = new TimeSeries(labels.get(i), i);
            dataset.add(series);
        }
        return dataset;

    }

    /**
     * Creates a new dataset based on the given array of labels. Time series are
     * indexed according to the positions of the labels within the array.
     * 
     * @param labels array of labels
     * @return the new dataset
     */
    public static Dataset createDataset(double[] labels) {
        return createDataset(labels, 0, labels.length - 1);
    }

    /**
     * Creates a new dataset based on the given array of labels. Time series are
     * indexed according to the positions of the labels within the array.
     * 
     * @param labels array of labels
     * @param first  index of the first label to be included in the dataset
     * @param last   index of the last label to be included in the dataset
     * @return the new dataset
     */
    public static Dataset createDataset(double[] labels, int first, int last) {
        Dataset dataset = new Dataset();
        for (int i = first; i < last + 1; i++) {
            TimeSeries series = new TimeSeries(labels[i], i);
            dataset.add(series);
        }
        return dataset;
    }

    /**
     * Creates a new dataset based on the labels loaded from the given file. Time
     * series are indexed based on the indexes of the labels within the file.
     * 
     * @param fname the name of the file containing the labels
     * @return the dataset
     * @throws IOException
     */
    public static Dataset createDataset(String fname) throws IOException {
        return createDataset(loadLabelsArray(fname));
    }

    /**
     * Loads the names of the datasets from the first column of the CSV file of the
     * specified name.
     * 
     * @param fname the name of the CSV file
     * @return list of dataset names
     * @throws IOException
     */
    public static List<String> loadDatasetNames(String fname) throws IOException {
        List<String> dsNames = new ArrayList<String>();
        int lineno = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(fname))) {
            lineno++;
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(",");
                if (parts.length > 0)
                    dsNames.add(parts[0]);
                lineno++;
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new IOException("Error at line " + lineno + ".", e);
        }

        return dsNames;
    }

    /**
     * Loads the names of the datasets from the "Datasets.csv" file.
     * 
     * @return list of dataset names
     * @throws IOException
     */
    public static List<String> loadDatasetNames() throws IOException {
        return loadDatasetNames("Datasets.csv");
    }

}
