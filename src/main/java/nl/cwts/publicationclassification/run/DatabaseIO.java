package nl.cwts.publicationclassification.run;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nl.cwts.networkanalysis.Network;

public class DatabaseIO
{
    /**
     * Reads publications and citation links from an SQL Server database table
     * and creates a citation network.
     *
     * @param server       SQL Server server name
     * @param database     Database name
     * @param pubTable     Name of the publications table
     * @param citLinkTable Name of the citation links table
     *
     * @return Network
     */
    public static Network readNetwork(String server, String database, String pubTable, String citLinkTable)
    {
        double[] pubWeight = null;
        int[][] citLink = null;
        double[] citLinkWeight = null;

        Connection connection = null;
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection("jdbc:sqlserver://" + server + ":1433;databaseName=" + database + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true;");
    
            // Read number of publications.
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select count(*) from " + pubTable);
            resultSet.next();
            int nPubs = resultSet.getInt(1);
            statement.close();

            // Read publications. Core publications are given a weight of 1 and non-core publications are given a weight of 0.
            pubWeight = new double[nPubs];
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select pub_no, core_pub from " + pubTable + " order by pub_no");
            for (int i = 0; i < nPubs; i++)
            {
                resultSet.next();
                int pubNo = resultSet.getInt(1);
                if (pubNo != i)
                    throw new SQLException("Publication numbers must be integers starting at zero.");
                pubWeight[i] = resultSet.getBoolean(2) ? 1 : 0;
            }
            statement.close();

            // Read number of citation links.
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select count(*) from " + citLinkTable);
            resultSet.next();
            int nCitLinks = resultSet.getInt(1);
            statement.close();

            citLink = new int[2][nCitLinks];
            citLinkWeight = new double[nCitLinks];

            // Read citation links.
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select pub_no1, pub_no2, cit_weight from " + citLinkTable + " order by pub_no1, pub_no2");
            for (int i = 0; i < nCitLinks; i++)
            {
                resultSet.next();
                citLink[0][i] = resultSet.getInt(1);
                citLink[1][i] = resultSet.getInt(2);
                citLinkWeight[i] = resultSet.getDouble(3);
            }
            statement.close();

            connection.close();
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Error while reading from database: SQL Server Driver not found.");
            System.exit(-1);
        }
        catch (SQLException e)
        {
            System.err.println("Error while reading from database: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (connection != null)
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Error while reading from database: " + e.getMessage());
                    System.exit(-1);
                }
        }

        // Create citation network.
        Network citNetwork = null;
        try
        {
            citNetwork = new Network(pubWeight, citLink, citLinkWeight, true, true);
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Error while creating citation network: " + e.getMessage());
            System.exit(-1);
        }

        return citNetwork;
    }

    /**
     * Writes a publication classification to an SQL Server database table.
     *
     * @param server              SQL Server server name
     * @param database            Database name
     * @param classificationTable Name of the classification table
     * @param pub                 Publication numbers
     * @param cluster             Cluster numbers
     * @param level               Level labels
     */
    public static void writeClassification(String server, String database, String classificationTable, int[] pub, int[][] cluster, String[] level)
    {
        Connection connection = null;
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection("jdbc:sqlserver://" + server + ":1433;databaseName=" + database + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true;");

            String query = "create table " + classificationTable + "(pub_no int not null";
            int nLevels = cluster.length;
            for (int i = 0; i < nLevels; i++)
                query += ", " + level[i] + "_cluster_no int not null";
            query += ")";
            Statement statement = connection.createStatement();
            statement.executeUpdate("drop table if exists " + classificationTable);
            statement.executeUpdate(query);
            statement.close();

            query = "insert into " + classificationTable + " values (?";
            for (int i = 0; i < nLevels; i++)
                query += ", ?";
            query += ")";
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < pub.length; i++)
            {
                preparedStatement.setInt(1, pub[i]);
                for (int j = 0; j < nLevels; j++)
                    preparedStatement.setInt(j + 2, cluster[j][i]);
                preparedStatement.addBatch();
                if ((i + 1) % 1000 == 0)
                    preparedStatement.executeBatch();
            }
            preparedStatement.executeBatch();
            preparedStatement.close();
            connection.commit();

            connection.close();
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("Error while writing publication classification to database: SQL Server Driver not found.");
            System.exit(-1);
        }
        catch (SQLException e)
        {
            System.err.println("Error while writing publication classification to database: " + e.getMessage());
            System.exit(-1);
        }
        finally
        {
            if (connection != null)
                try
                {
                    connection.close();
                }
                catch (SQLException e)
                {
                    System.err.println("Error while writing publication classification to database: " + e.getMessage());
                    System.exit(-1);
                }
        }
    }
}
