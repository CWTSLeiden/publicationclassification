package nl.cwts.publicationclassification;

import java.util.ArrayList;

import nl.cwts.networkanalysis.Clustering;
import nl.cwts.networkanalysis.IterativeCPMClusteringAlgorithm;
import nl.cwts.networkanalysis.Network;

/**
 * Multi-level clustering of a network.
 * 
 * @author Nees Jan van Eck
 */
public class MultiLevelClustering 
{
    /**
     * Clustering of the reduced network at a specific level of the multi-level
     * clustering, the corresponding value of the resolution parameter, and the
     * corresponding minimum number of nodes per cluster.
     * 
     * @author Nees Jan van Eck
     */
    private class SingleLevelClustering
    {
        /**
         * Clustering of the reduced network.
         */
        private Clustering reducedClustering;

        /**
         * Value of the resolution parameter.
         */
        private double resolution;

        /**
         * Minimum number of nodes per cluster.
         */
        private double threshold;

        /**
         * Constructs a SingleLevelClustering object.
         * 
         * @param reducedClustering Clustering of the reduced network
         * @param resolution        Value of the resolution parameter
         * @param threshold         Minimum number of nodes per cluster
         */
        public SingleLevelClustering(Clustering reducedClustering, double resolution, double threshold)
        {
            this.reducedClustering = reducedClustering;
            this.resolution = resolution;
            this.threshold = threshold;
        }
    }

    /**
     * Network.
     */
    private Network network;

    /**
     * Clustering algorithm.
     */
    private IterativeCPMClusteringAlgorithm clusteringAlgorithm;

    /**
     * Multi-level clustering (represented by a list of single-level
     * clusterings).
     */
    private ArrayList<SingleLevelClustering> singleLevelClustering;

    /**
     * Constructs a multi-level clustering of a network.
     * 
     * @param network             Network
     * @param clusteringAlgorithm Clustering algorithm
     */
    public MultiLevelClustering(Network network, IterativeCPMClusteringAlgorithm clusteringAlgorithm)
    {
        this.singleLevelClustering = new ArrayList<SingleLevelClustering>();
        this.network = network;
        this.clusteringAlgorithm = clusteringAlgorithm;
    }

    /**
     * Returns the number of levels of the multi-level clustering.
     * 
     * @return Number of levels
     */
    public int getNLevels()
    {
        return singleLevelClustering.size();
    }

    /**
     * Adds a level to the multi-level clustering.
     * 
     * @param resolution Value of the resolution parameter
     * @param threshold  Minimum number of nodes per cluster
     * 
     * @throws IllegalArgumentException Value of the resolution parameter is
     *                                  illegal.
     */
    public void addLevel(double resolution, double threshold)
    {
        addLevel(resolution, threshold, false);
    }

    /**
     * Adds a level to the multi-level clustering.
     * 
     * @param resolution Value of the resolution parameter
     * @param threshold  Minimum number of nodes per cluster
     * @param printInfo  Print progress and result information to the standard
     *                   output
     * 
     * @throws IllegalArgumentException Value of the resolution parameter is
     *                                  illegal.
     */
    public void addLevel(double resolution, double threshold, boolean printInfo)
    {
        int nLevels = singleLevelClustering.size();
        if (nLevels > 0)
            if (singleLevelClustering.get(nLevels - 1).resolution < resolution)
                throw new IllegalArgumentException("The value of the resolution parameter must be lower than the value at the highest (i.e., least granular) level of the multi-level clustering.");

        Network reducedNetwork = getReducedNetwork(nLevels);

        // Create clustering.
        if (printInfo)
            System.out.print("Creating clustering... ");
        clusteringAlgorithm.setResolution(resolution);
        Clustering reducedClustering = clusteringAlgorithm.findClustering(reducedNetwork);
        reducedClustering.orderClustersByWeight(reducedNetwork.getNodeWeights());
        reducedClustering.removeEmptyClusters();
        if (printInfo)
            System.out.println("Finished! " + reducedClustering.getNClusters() + " clusters created.");

        // Reassign small clusters.
        if (printInfo)
            System.out.print("Reassigning small clusters... ");
        clusteringAlgorithm.removeSmallClustersBasedOnWeight(reducedNetwork, reducedClustering, threshold);
        reducedClustering.orderClustersByWeight(reducedNetwork.getNodeWeights());
        reducedClustering.removeEmptyClusters();
        if (printInfo)
            System.out.println("Finished! " + reducedClustering.getNClusters() + " clusters remaining.");

        // Add final clustering to the multi-level clustering.
        singleLevelClustering.add(new SingleLevelClustering(reducedClustering, resolution, threshold));
    }

    /**
     * Removes a level and all higher (i.e., less granular) levels from the
     * multi-level clustering.
     * 
     * @param level Level of the multi-level clustering
     */
    public void removeLevel(int level)
    {
        while (singleLevelClustering.size() > level)
            singleLevelClustering.remove(singleLevelClustering.size() - 1);
    }

    /**
     * Returns the value of the resolution parameter at a specific level of the
     * multi-level clustering.
     * 
     * @param level Level of the multi-level clustering
     * 
     * @return Value of the resolution parameter
     */
    public double getResolution(int level)
    {
        return singleLevelClustering.get(level).resolution;
    }

    /**
     * Returns the minimum number of nodes per cluster at a specific level of
     * the multi-level clustering.
     * 
     * @param level Level of the multi-level clustering
     * 
     * @return Minimum number of nodes per cluster
     */
    public double getThreshold(int level)
    {
        return singleLevelClustering.get(level).threshold;
    }

    /**
     * Returns the number of clusters at a specific level of the multi-level
     * clustering.
     * 
     * @param level Level of the multi-level clustering
     * 
     * @return Number of clusters
     */
    public int getNClusters(int level)
    {
        return singleLevelClustering.get(level).reducedClustering.getNClusters();
    }

    /**
     * Returns the clustering of the network at a specific level of the
     * multi-level clustering.
     * 
     * @param level Level of the multi-level clustering
     * 
     * @return Clustering of the network
     */
    public Clustering getClustering(int level)
    {
        Clustering clustering = singleLevelClustering.get(0).reducedClustering.clone();

        for (int i = 1; i <= level; i++)
            clustering.mergeClusters(singleLevelClustering.get(i).reducedClustering);

        return clustering;
    }

    /**
     * Returns the reduced network at a specific level of the multi-level
     * clustering.
     * 
     * @param level Level of the multi-level clustering
     * 
     * @return Reduced network
     */
    public Network getReducedNetwork(int level)
    {
        if (level == 0)
            return network;
        else
            return getReducedNetwork(level - 1).createReducedNetwork(singleLevelClustering.get(level - 1).reducedClustering);
    }

    /**
     * Returns the clustering of the reduced network at a specific level of the
     * multi-level clustering.
     * 
     * @param level Level of the multi-level clustering
     * 
     * @return Clustering of the reduced network
     */
    public Clustering getReducedClustering(int level)
    {
        return singleLevelClustering.get(level).reducedClustering;
    }
}
