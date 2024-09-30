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
 * @author Zoltan Geller
 * @version 2024.08.27.
 */
public final class DatasetUtils {

    private DatasetUtils() {
    }

    /**
     * Character used to separate values. Default value is comma.
     */
    public static char separator = ',';

    /**
     * Root folder of the datasets. Default value is {@code DATASETS}.
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
     * Loads the dataset with the given name. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by the default {@code separator}</li>
     * <li>the data points don't contain x-coordinates</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
     * 
     * @param dsname the name of the dataset
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname) throws IOException {
        return loadDataset(dsname, true, true);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by the default {@code separator}</li>
     * <li>the data points don't contain x-coordinates</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
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
     * Loads the dataset with the given name. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by the default {@code separator}</li>
     * <li>the data points don't contain x-coordinates</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
     * 
     * @param dsname the name of the dataset
     * @param train  indicates whether to load train set
     * @param test   indicates whether to load test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname, boolean train, boolean test)
            throws IOException {
        return loadDataset(dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by a space</li>
     * <li>the data points don't contain x-coordinates</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
     * 
     * @param folder the name of the folder
     * @param dsname the name of the dataset
     * @param train  indicates whether to load train set
     * @param test   indicates whether to load test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String folder, String dsname, boolean train, boolean test)
            throws IOException {
        return loadDataset(folder, dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by the given {@code separator}</li>
     * <li>the data points don't contain x-coordinates</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
     * 
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param train     indicates whether to load train set
     * @param test      indicates whether to load test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname, char separator, boolean train,
            boolean test) throws IOException {
        return loadDataset(dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by the given {@code separator}</li>
     * <li>the data points don't contain x-coordinates</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
     * 
     * @param folder    the folder
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param train     indicates whether to load train set
     * @param test      indicates whether to load test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String folder, String dsname, char separator,
            boolean train, boolean test) throws IOException {
        return loadDataset(folder, dsname, separator, false, train, test);
    }

    /**
     * Loads the dataset with the given name. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by the given {@code separator}</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
     * 
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param hasXValue tells if the list of input values contain x-coordinates of
     *                  points
     * @param train     indicates whether to load train set
     * @param test      indicates whether to load test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String dsname, char separator, boolean hasXValue,
            boolean train, boolean test) throws IOException {
        return loadDataset(root, dsname, separator, hasXValue, train, test);
    }

    /**
     * Loads the dataset with the given name from the given folder. Assumptions:<br>
     * <ol>
     * <li>the training set is stored in file {@code dsname\dsname+"_TRAIN"},</li>
     * <li>the test set is stored in file {@code dsname\dsname+"_TEST"},</li>
     * <li>the data points are separated by the given {@code separator}</li>
     * </ol>
     * The elements of the test set are added after the elements of the training
     * set.<br>
     * <br>
     * 
     * @param folder    the folder
     * @param dsname    the name of the dataset
     * @param separator the character which separates the values
     * @param hasXValue tells if the list of input values contain x-coordinates of
     *                  points
     * @param train     indicates whether to load train set
     * @param test      indicates whether to load test set
     * @return the dataset
     * @throws IOException
     */
    public static Dataset loadDataset(String folder, String dsname, char separator,
            boolean hasXValue, boolean train, boolean test) throws IOException {

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
        // 03.01.2011.
        Dataset dataset = null;

        if (train && test) {
            if (trainSet.size() > testSet.size()) {
                dataset = trainSet;
                dataset.addAll(testSet);
                testSet.clear();
                testSet = null;
            } else {
                dataset = testSet;
                dataset.addAll(0, trainSet); // 10.02.2011.
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
     * @param dataset the dataset to save
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
     * @param dataset   the dataset to save
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
     * @param hasXValue tells if the dataset values contain x-coordinates of points
     * @param dataset   the dataset to save
     * @throws IOException
     */
    public static void saveDataset(String dsname, char separator, boolean hasXValue,
            Dataset dataset) throws IOException {
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
    public static ArrayList<Double> loadLabelsList(String fname) throws IOException {
        ArrayList<Double> labels = new ArrayList<Double>();
        BufferedReader reader = new BufferedReader(new FileReader(fname));
        String line = reader.readLine();
        while (line != null && line.length() > 0) {
            double label = Double.parseDouble(line);
            labels.add(label);
            line = reader.readLine();
        }
        reader.close();
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
        ArrayList<Double> labelsList = loadLabelsList(fname);
        int size = labelsList.size();
        double[] labels = new double[size];
        for (int i = 0; i < size; i++)
            labels[i] = labelsList.get(i);
        return labels;
    }

    /**
     * Creates a new dataset based on the given list of labels. Time series are
     * indexed based on the indexes of the labels within the list.
     * 
     * @param labels list of labels
     * @return the new dataset
     */
    public static Dataset createDataSet(List<Double> labels) {
        return createDataSet(labels, 0, labels.size() - 1);
    }

    /**
     * Creates a new dataset based on the given list of labels. Time series are
     * indexed based on the indexes of the labels within the list.
     * 
     * @param labels list of labels
     * @param first  index of the first label to include
     * @param last   index of the last label to include
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
     * Creates a new dataset based on the given list of labels. Time series are
     * indexed based on the indexes of the labels within the array.
     * 
     * @param labels array of labels
     * @return the new dataset
     */
    public static Dataset createDataset(double[] labels) {
        return createDataset(labels, 0, labels.length - 1);
    }

    /**
     * Creates a new dataset based on the given list of labels. Time series are
     * indexed based on the indexes of the labels within the array.
     * 
     * @param labels array of labels
     * @param first  index of the first label to include
     * @param last   index of the last label to include
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
     * @param fname the name of the file
     * @return the dataset
     * @throws IOException
     */
    public static Dataset createDataset(String fname) throws IOException {
        return createDataset(loadLabelsArray(fname));
    }

    /**
     * Loads the names of the datasets from the given CSV file.
     * 
     * @param fname the name of the CSV file
     * @return list of dataset names
     * @throws IOException
     */
    public static ArrayList<String> loadDatasetNames(String fname) throws IOException {
        ArrayList<String> dsNames = new ArrayList<String>();
        int lineno = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("Datasets.csv"))) {
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
            throw new IOException("Error at line " + lineno + ".");
        }

        return dsNames;
    }

    /**
     * Loads the names of the datasets from the "Datasets.csv" file.
     * 
     * @return
     * @throws IOException
     */
    public static ArrayList<String> loadDatasetNames() throws IOException {
        return loadDatasetNames("Datasets.csv");
    }

}
