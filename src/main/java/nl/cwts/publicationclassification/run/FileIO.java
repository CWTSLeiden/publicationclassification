package nl.cwts.publicationclassification.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import nl.cwts.networkanalysis.Network;
import nl.cwts.util.DynamicDoubleArray;
import nl.cwts.util.DynamicIntArray;

public class FileIO
{
    /**
     * Column separator for input and output files.
     */
    public static final String COLUMN_SEPARATOR = "\t";

    /**
     * Reads publications and citation links from a file and creates a citation
     * network.
     *
     * @param pubFile     Name of the publications file
     * @param citLinkFile Name of the citation links file
     *
     * @return Network
     */
    public static Network readNetwork(String pubFile, String citLinkFile)
    {
        DynamicDoubleArray pubWeight = new DynamicDoubleArray(100);
        DynamicIntArray[] citLink = new DynamicIntArray[2];
        citLink[0] = new DynamicIntArray(100);
        citLink[1] = new DynamicIntArray(100);
        DynamicDoubleArray citLinkWeight = new DynamicDoubleArray(100);

        BufferedReader reader = null;
        // Read publications file.
        try
        {
            reader = new BufferedReader(new FileReader(pubFile));
            String line = reader.readLine();
            int lineNo = 0;
            while (line != null)
            {
                lineNo++;
                String[] columns = line.split(COLUMN_SEPARATOR);
                if (columns.length != 2)
                    throw new IOException("Incorrect number of columns (line " + lineNo + ").");
                int pubNo;
                try
                {
                    pubNo = Integer.parseUnsignedInt(columns[0]);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Publication numbers must be integers starting at zero (line " + lineNo + ").");
                }
                if (pubNo != (lineNo - 1))
                    throw new IOException("The lines in the file must be sorted by the publication numbers in the first column (line " + lineNo + ").");
                // Core publications are given a weight of 1 and non-core publications are given a weight of 0.
                pubWeight.append((Boolean.parseBoolean(columns[1]) || columns[1].equals("1")) ? 1 : 0);
                line = reader.readLine();
            }
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while reading publications file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while reading publications file: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (reader != null)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error while reading publications file: " + e.getMessage());
                    System.exit(-1);
                }
        }
        // Read citation links file.
        try
        {
            reader = new BufferedReader(new FileReader(citLinkFile));
            String line = reader.readLine();
            int lineNo = 0;
            while (line != null)
            {
                lineNo++;
                String[] columns = line.split(COLUMN_SEPARATOR);
                if (columns.length != 3)
                    throw new IOException("Incorrect number of columns (line " + lineNo + ").");
                int pubNo1;
                int pubNo2;
                try
                {
                    pubNo1 = Integer.parseUnsignedInt(columns[0]);
                    pubNo2 = Integer.parseUnsignedInt(columns[1]);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Publication numbers must be integers starting at zero (line " + lineNo + ").");
                }
                citLink[0].append(pubNo1);
                citLink[1].append(pubNo2);
                double weight;
                try
                {
                    weight = Double.parseDouble(columns[2]);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Citation link weight must be a number (line " + lineNo + ").");
                }
                citLinkWeight.append(weight);
                line = reader.readLine();
            }
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while reading citation links from file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while reading citation links from file: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (reader != null)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error while reading citation links from file: " + e.getMessage());
                    System.exit(-1);
                }
        }

        // Create citation network.
        Network citNetwork = null;
        int[][] citLink2 = new int[2][];
        citLink2[0] = citLink[0].toArray();
        citLink2[1] = citLink[1].toArray();
        try
        {
            citNetwork = new Network(pubWeight.toArray(), citLink2, citLinkWeight.toArray(), true, true);
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Error while creating citation network: " + e.getMessage());
            System.exit(-1);
        }

        return citNetwork;
    }

    /**
     * Writes a publication classification to a file.
     *
     * @param classificationFile Name of the classification file.
     * @param pub                Publication numbers
     * @param cluster            Cluster numbers
     */
    public static void writeClassification(String classificationFile, int[] pub, int[][] cluster)
    {
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new FileWriter(classificationFile));
            int nLevels = cluster.length;
            for (int i = 0; i < pub.length; i++)
            {
                writer.write(pub[i] + "");
                for (int j = 0; j < nLevels; j++)
                    writer.write(COLUMN_SEPARATOR + cluster[j][i]);
                writer.newLine();
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error while writing publication classification to file: File not found.");
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("Error while writing publication classification to file: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (writer != null)
                try
                {
                    writer.close();
                }
                catch (IOException e)
                {
                    System.err.println("Error while writing publication classification to file: " + e.getMessage());
                    System.exit(-1);
                }
        }
    }
}
