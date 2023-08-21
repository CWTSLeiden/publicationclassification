package nl.cwts.publicationclassification.run;

import java.util.Random;

import nl.cwts.networkanalysis.Clustering;
import nl.cwts.networkanalysis.LeidenAlgorithm;
import nl.cwts.networkanalysis.Network;
import nl.cwts.publicationclassification.MultiLevelClustering;

/**
 * Command line tool for creating a multi-level publication classification.
 *
 * <p>
 * All methods in this class are static.
 * </p>
 *
 * @author Nees Jan van Eck
 */
public class PublicationClassificationCreator
{
    /**
     * Description text.
     */
    public static final String DESCRIPTION
        = "PublicationClassificationCreator version 1.1.0\n"
          + "By Nees Jan van Eck\n"
          + "Centre for Science and Technology Studies (CWTS), Leiden University\n";

    /**
     * Usage text.
     */
    public static final String USAGE
        = "Usage: PublicationClassificationCreator\n"
            + "\t<pub_file> <cit_link_file> <classification_file>\n"
            + "\t<largest_component> <n_iterations>\n"
            + "\t<resolution_micro_level> <pub_threshold_micro_level>\n"
            + "\t<resolution_meso_level> <pub_threshold_meso_level>\n"
            + "\t<resolution_macro_level> <pub_threshold_macro_level>\n"
            + "\t\t(to create a publication classification based on data in text files)\n\n"
            + "   or  PublicationClassificationCreator\n"
            + "\t<server> <database> <pub_table> <cit_link_table> <classification_table>\n"
            + "\t<largest_component> <n_iterations>\n"
            + "\t<resolution_micro_level> <pub_threshold_micro_level>\n"
            + "\t<resolution_meso_level> <pub_threshold_meso_level>\n"
            + "\t<resolution_macro_level> <pub_threshold_macro_level>\n"
            + "\t\t(to create a publication classification based on data in an SQL Server database)\n\n"
            + "Arguments:\n"
            + "<pub_file>\n"
            + "\tName of the publications input file. This text file must contain two tab-separated\n"
            + "\tcolumns (without a header line), first a column of publication numbers and then a\n"
            + "\tcolumn of core publication indicators (1 for core publications and 0 for non-core\n"
            + "\tpublications). Publication numbers must be integers starting at zero. Non-core\n"
            + "\tpublications are auxiliary publications that can be included to improve the clustering\n"
            + "\tof core publications. The lines in the file must be sorted by the publication numbers\n"
            + "\tin the first column.\n"
            + "<cit_link_file>\n"
            + "\tName of the citation links input file. This text file must contain three tab-separated\n"
            + "\tcolumns (without a header line), first two columns of publication numbers and then a\n"
            + "\tcolumn of weights. Each citation link must be included only once in the file. The\n"
            + "\tlines in the file must be sorted first by the publication numbers in the first column\n"
            + "\tand then by the publication numbers in the second column.\n"
            + "<classification_file>\n"
            + "\tName of the classification output file. This text file will contain four tab-separated\n"
            + "\tcolumns (without a header line), first a column of publication numbers and then three\n"
            + "\tcolumns of cluster numbers at the micro, meso, and macro level. Cluster numbers are\n"
            + "\tintegers starting at zero.\n"
            + "<server>\n"
            + "\tSQL Server server name. A connection will be made using integrated authentication.\n"
            + "<database>\n"
            + "\tDatabase name.\n"
            + "<pub_table>\n"
            + "\tName of the publications input table. This table must have two columns: pub_no and\n"
            + "\tcore_pub. Publication numbers must be integers starting at zero. Non-core\n"
            + "\tpublications (core_pub = 0) are auxiliary publications that can be included to improve\n"
            + "\tthe clustering of core publications (core_pub = 1).\n"
            + "<cit_link_table>\n"
            + "\tName of the citation links input table. This table must have three columns: pub_no1,\n"
            + "\tpub_no2, and cit_weight. Each citation link must be included only once in the table.\n"
            + "<classification_table>\n"
            + "\tName of the classification output table. This table will have four columns: pub_no,\n"
            + "\tmicro_cluster_no, meso_cluster_no, and macro_cluster_no. Cluster numbers are integers\n"
            + "\tstarting at zero.\n"            
            + "<largest_component>\n"
            + "\tBoolean indicating whether the publication classification should include only\n"
            + "\tpublications belonging to the largest connected component of the citation network\n"
            + "\t('true') or all publications ('false').\n"
            + "<n_iterations>\n"
            + "\tNumber of iterations of the Leiden algorithm (e.g., 50).\n"
            + "<resolution_micro_level>\n"
            + "\tValue of the resolution parameter at the micro level.\n"
            + "<pub_threshold_micro_level>\n"
            + "\tMinimum number of publications per cluster at the micro level (excluding non-core\n"
            + "\tpublications).\n"
            + "<resolution_meso_level>\n"
            + "\tValue of the resolution parameter at the meso level.\n"
            + "<pub_threshold_meso_level>\n"
            + "\tMinimum number of publications per cluster at the meso level (excluding non-core\n"
            + "\tpublications).\n"
            + "<resolution_macro_level>\n"
            + "\tValue of the resolution parameter at the macro level.\n"
            + "<pub_threshold_macro_level>\n"
            + "\tMinimum number of publications per cluster at the macro level (excluding non-core\n"
            + "\tpublications).\n";

    /**
     * This method is called when the tool is started.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        System.out.println(DESCRIPTION);
        if (args.length == 0)
        {
            System.out.print(USAGE);
            System.exit(-1);
        }

        // Process command line arguments.
        boolean useFiles = false;
        if (args.length == 11)
            useFiles = true;
        else if (args.length != 13)
        {
            System.err.print("Error while processing command line arguments: Incorrect number of command line arguments.\n\n" + USAGE);
            System.exit(-1);
        }

        String pubFile = null;
        String citLinkFile = null;
        String classificationFile = null;
        String server = null;
        String database = null;
        String pubTable = null;
        String citLinkTable = null;
        String classificationTable = null;
        boolean largestComponent = false;
        int nIterations = 0;
        double resolutionMicroLevel = 0;
        int pubThresholdMicroLevel = 0;
        double resolutionMesoLevel = 0;
        int pubThresholdMesoLevel = 0;
        double resolutionMacroLevel = 0;
        int pubThresholdMacroLevel = 0;
        int argIndex = 0;
        if (useFiles)
        {
            pubFile = args[argIndex++];
            citLinkFile = args[argIndex++];
            classificationFile = args[argIndex++];
        }
        else
        {
            server = args[argIndex++];
            database = args[argIndex++];
            pubTable = args[argIndex++];
            citLinkTable = args[argIndex++];
            classificationTable = args[argIndex++];
        }
        try
        {
            if (!args[argIndex].equalsIgnoreCase("true") && !args[argIndex].equalsIgnoreCase("false"))
                throw new IllegalArgumentException();
            largestComponent = Boolean.parseBoolean(args[argIndex++]);

        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Error while processing command line argument <largest_component>: Value must be a boolean ('true' or 'false').\n\n" + USAGE);
            System.exit(-1);
        }
        try
        {
            nIterations = Integer.parseInt(args[argIndex++]);
            if (nIterations <= 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error while processing command line argument <n_iterations>: Value must be a positive integer number.\n\n" + USAGE);
            System.exit(-1);
        }
        try
        {
            resolutionMicroLevel = Double.parseDouble(args[argIndex++]);
            if (resolutionMicroLevel < 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error while processing command line argument <resolution_micro_level>: Value must be a non-negative number.\n\n" + USAGE);
            System.exit(-1);
        }
        try
        {
            pubThresholdMicroLevel = Integer.parseInt(args[argIndex++]);
            if (pubThresholdMicroLevel <= 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error while processing command line argument <pub_threshold_micro_level>: Value must be a positive integer number.\n\n" + USAGE);
            System.exit(-1);
        }
        try
        {
            resolutionMesoLevel = Double.parseDouble(args[argIndex++]);
            if (resolutionMesoLevel < 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error while processing command line argument <resolution_meso_level>: Value must be a non-negative number.\n\n" + USAGE);
            System.exit(-1);
        }
        try
        {
            pubThresholdMesoLevel = Integer.parseInt(args[argIndex++]);
            if (pubThresholdMesoLevel < 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error while processing command line argument <pub_threshold_meso_level>: Value must be a positive integer number.\n\n" + USAGE);
            System.exit(-1);
        }
        try
        {
            resolutionMacroLevel = Double.parseDouble(args[argIndex++]);
            if (resolutionMacroLevel < 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error while processing command line argument <resolution_macro_level>: Value must be a non-negative number.\n\n" + USAGE);
            System.exit(-1);
        }
        try
        {
            pubThresholdMacroLevel = Integer.parseInt(args[argIndex++]);
            if (pubThresholdMacroLevel < 0)
                throw new NumberFormatException();
        }
        catch (NumberFormatException e)
        {
            System.err.println("Error while processing command line argument <pub_threshold_macro_level>: Value must be a positive integer number.\n\n" + USAGE);
            System.exit(-1);
        }

        // Read citation network from file or database.
        System.out.print("Reading citation network from " + ((useFiles) ? "file" : "database") + "... ");
        long startTimeCitNetwork = System.currentTimeMillis();
        Network citNetwork;
        if (useFiles)
            citNetwork = FileIO.readNetwork(pubFile, citLinkFile);
        else
            citNetwork = DatabaseIO.readNetwork(server, database, pubTable, citLinkTable);
        int nPubs = citNetwork.getNNodes();
        int[] pub = new int[citNetwork.getNNodes()];
        System.out.println("Finished!");
        System.out.println("Reading citation network from " + ((useFiles) ? "file" : "database") + " took " + formatDuration((System.currentTimeMillis() - startTimeCitNetwork) / 1000) + ".");
        System.out.println("Citation network:");
        System.out.println("\tNumber of publications: " + citNetwork.getNNodes());
        System.out.println("\tNumber of citation links: " + citNetwork.getNEdges());
        System.out.println("\tTotal publication weight: " + (int) (citNetwork.getTotalNodeWeight() + 0.5));
        System.out.println("\tTotal citation link weight: " + (int) (citNetwork.getTotalEdgeWeight() + 0.5));
        System.out.println();

        if (largestComponent)
        {
            // Identify largest connected component in citation network.
            System.out.print("Identifying largest connected component in citation network... ");
            long startTimeCoreCitNetwork = System.currentTimeMillis();
            Clustering clustering = citNetwork.identifyComponents();
            citNetwork = citNetwork.createSubnetwork(clustering, 0);
            pub = new int[citNetwork.getNNodes()];
            int[] cluster = clustering.getClusters();
            int i = 0;
            for (int j = 0; j < nPubs; j++)
                if (cluster[j] == 0)
                {
                    pub[i] = j;
                    i++;
                }
            System.out.println("Finished!");
            System.out.println("Identifying largest connected component in citation network took " + formatDuration((System.currentTimeMillis() - startTimeCoreCitNetwork) / 1000) + ".");
            System.out.println("Largest connected component:");
            System.out.println("\tNumber of publications: " + citNetwork.getNNodes());
            System.out.println("\tNumber of citation links: " + citNetwork.getNEdges());
            System.out.println("\tTotal publication weight: " + (int) (citNetwork.getTotalNodeWeight() + 0.5));
            System.out.println("\tTotal citation link weight: " + (int) (citNetwork.getTotalEdgeWeight() + 0.5));
            System.out.println();
        }

        // Create publication classification.
        System.out.println("Creating publication classification...");
        LeidenAlgorithm clusteringAlgorithm = new LeidenAlgorithm(LeidenAlgorithm.DEFAULT_RESOLUTION, nIterations, LeidenAlgorithm.DEFAULT_RANDOMNESS, new Random(0));
        System.out.println("\tClustering algorithm: Leiden algorithm");
        System.out.println("\tNumber of iterations: " + nIterations);
        System.out.println("\tRandom seed: 0");
        System.out.println();
        MultiLevelClustering multiLevelClustering = new MultiLevelClustering(citNetwork, clusteringAlgorithm);
        // Add micro-level classification.
        System.out.println("Adding micro-level classification...");
        long startTimeMicroLevel = System.currentTimeMillis();
        multiLevelClustering.addLevel(resolutionMicroLevel, pubThresholdMicroLevel, true);
        System.out.println("Adding micro-level classification took " + formatDuration((System.currentTimeMillis() - startTimeMicroLevel) / 1000) + ".");
        System.out.println("Micro-level classification:");
        System.out.println("\tResolution: " + resolutionMicroLevel);
        System.out.println("\tThreshold: " + pubThresholdMicroLevel);
        System.out.println("\tNumber of clusters: " + + multiLevelClustering.getNClusters(0));
        System.out.println();
        // Add meso-level classification.
        System.out.println("Adding meso-level classification...");
        long startTimeMesoLevel = System.currentTimeMillis();
        multiLevelClustering.addLevel(resolutionMesoLevel, pubThresholdMesoLevel, true);
        System.out.println("Adding meso-level classification took " + formatDuration((System.currentTimeMillis() - startTimeMesoLevel) / 1000) + ".");
        System.out.println("Meso-level classification:");
        System.out.println("\tResolution: " + resolutionMesoLevel);
        System.out.println("\tThreshold: " + pubThresholdMesoLevel);
        System.out.println("\tNumber of clusters: " + + multiLevelClustering.getNClusters(1));
        System.out.println();
        // Add macro-level classification.
        System.out.println("Adding macro-level classification...");
        long startTimeMacroLevel = System.currentTimeMillis();
        multiLevelClustering.addLevel(resolutionMacroLevel, pubThresholdMacroLevel, true);
        System.out.println("Adding macro-level classification took " + formatDuration((System.currentTimeMillis() - startTimeMacroLevel) / 1000) + ".");
        System.out.println("Macro-level classification:");
        System.out.println("\tResolution: " + resolutionMacroLevel);
        System.out.println("\tThreshold: " + pubThresholdMacroLevel);
        System.out.println("\tNumber of clusters: " + + multiLevelClustering.getNClusters(2));
        System.out.println();

        // Write publication classification to file or database.
        System.out.print("Writing publication classification to " + ((useFiles) ? "file" : "database") + "... ");
        long startTimePubClustering = System.currentTimeMillis();
        int nLevels = multiLevelClustering.getNLevels();
        if (nLevels == 0)
            return;
        int[][] cluster = new int[nLevels][];
        for (int i = 0; i < nLevels; i++)
            cluster[i] = multiLevelClustering.getClustering(i).getClusters();
        if (useFiles)
            FileIO.writeClassification(classificationFile, pub, cluster);
        else
            DatabaseIO.writeClassification(server, database, classificationTable, pub, cluster, new String[]{"micro", "meso", "macro"});
        System.out.println("Finished!");
        System.out.println("Writing publication classification to " + ((useFiles) ? "file" : "database") + " took " + formatDuration((System.currentTimeMillis() - startTimePubClustering) / 1000) + ".");
    }

    /**
     * Formats a given duration in seconds.
     *
     * @param s Duration in seconds
     * 
     * @return Formatted duration
     */
    private static String formatDuration(long s)
    {
        return String.format("%dh %dm %ds", s / 3600, (s % 3600) / 60, (s % 60));
    }
}
