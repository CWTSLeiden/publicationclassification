# publicationclassification

[![Build master branch](https://github.com/CWTSLeiden/publicationclassification/workflows/Build%20main%20branch/badge.svg?branch=main)](https://github.com/CWTSLeiden/publicationclassification/actions)
[![License: MIT](https://badgen.net/github/license/CWTSLeiden/publicationclassification?label=License&color=yellow)](https://github.com/CWTSLeiden/publicationclassification/blob/main/LICENSE)
[![Latest release](https://badgen.net/github/release/CWTSLeiden/publicationclassification?label=Release)](https://github.com/CWTSLeiden/publicationclassification/releases)
[![Maven Central version](https://badgen.net/maven/v/maven-central/nl.cwts/publicationclassification)](https://central.sonatype.com/artifact/nl.cwts/publicationclassification)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.8263452.svg)](https://doi.org/10.5281/zenodo.8263452)

## Introduction

This Java package can be used to create a multi-level classification of scientific publications based on citation links between publications.

The package uses the direct citation approach introduced by [Waltman and Van Eck (2012)](https://doi.org/10.1002/asi.22748) combined with the Leiden algorithm introduced by [Traag et al. (2019)](https://doi.org/10.1038/s41598-019-41695-z). The package also supports the extended direct citation approach introduced by [Waltman et al. (2020)](https://doi.org/10.1162/qss_a_00035).

The publicationclassification package was developed by [Nees Jan van Eck](https://orcid.org/0000-0001-8448-4521) at the [Centre for Science and Technology Studies (CWTS)](https://www.cwts.nl) at [Leiden University](https://www.universiteitleiden.nl/en). It relies on the [networkanalysis](https://github.com/CWTSLeiden/networkanalysis) package that was developed by [Nees Jan van Eck](https://orcid.org/0000-0001-8448-4521), [Vincent Traag](https://orcid.org/0000-0003-3170-3879), and [Ludo Waltman](https://orcid.org/0000-0001-8249-1752).

## Documentation

Documentation of the source code of publicationclassification is provided in the code in `javadoc` format. The documentation is also available in a [compiled format](https://CWTSLeiden.github.io/publicationclassification).

## Installation

### Maven

```
<dependency>
    <groupId>nl.cwts</groupId>
    <artifactId>publicationclassification</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Gradle

```
implementation group: 'nl.cwts', name: 'publicationclassification', version: '1.1.0'
```

## Usage

The publicationclassification package requires Java 8 or higher. The latest version of the package is available as a pre-compiled `jar` file on [Maven Central](https://central.sonatype.com/artifact/nl.cwts/publicationclassification) and [GitHub Packages](https://github.com/CWTSLeiden/publicationclassification/packages).
Instructions for compiling the source code of the package are provided [below](#development-and-deployment).

Use the command-line tool `PublicationClassificationCreator` to create a publication classification. The tool can be run as follows:

```
java -cp publicationclassification-1.1.0.jar nl.cwts.publicationclassification.PublicationClassificationCreator
```

If no further arguments are provided, the following usage notice will be displayed:

```
PublicationClassificationCreator version 1.1.0
By Nees Jan van Eck
Centre for Science and Technology Studies (CWTS), Leiden University

Usage: PublicationClassificationCreator
	<pub_file> <cit_link_file> <classification_file>
	<largest_component> <n_iterations>
	<resolution_micro_level> <pub_threshold_micro_level>
	<resolution_meso_level> <pub_threshold_meso_level>
	<resolution_macro_level> <pub_threshold_macro_level>
		(to create a publication classification based on data in text files)

   or  PublicationClassificationCreator
	<server> <database> <pub_table> <cit_link_table> <classification_table>
	<largest_component> <n_iterations>
	<resolution_micro_level> <pub_threshold_micro_level>
	<resolution_meso_level> <pub_threshold_meso_level>
	<resolution_macro_level> <pub_threshold_macro_level>
		(to create a publication classification based on data in an SQL Server database)

Arguments:
<pub_file>
	Name of the publications input file. This text file must contain two tab-separated
	columns (without a header line), first a column of publication numbers and then a
	column of core publication indicators (1 for core publications and 0 for non-core
	publications). Publication numbers must be integers starting at zero. Non-core
	publications are auxiliary publications that can be included to improve the clustering
	of core publications. The lines in the file must be sorted by the publication numbers
	in the first column.
<cit_link_file>
	Name of the citation links input file. This text file must contain three tab-separated
	columns (without a header line), first two columns of publication numbers and then a
	column of weights. Each citation link must be included only once in the file. The
	lines in the file must be sorted first by the publication numbers in the first column
	and then by the publication numbers in the second column.
<classification_file>
	Name of the classification output file. This text file will contain four tab-separated
	columns (without a header line), first a column of publication numbers and then three
	columns of cluster numbers at the micro, meso, and macro level. Cluster numbers are
	integers starting at zero.
<server>
	SQL Server server name. A connection will be made using integrated authentication.
<database>
	Database name.
<pub_table>
	Name of the publications input table. This table must have two columns: pub_no and
	core_pub. Publication numbers must be integers starting at zero. Non-core
	publications (core_pub = 0) are auxiliary publications that can be included to improve
	the clustering of core publications (core_pub = 1).
<cit_link_table>
	Name of the citation links input table. This table must have three columns: pub_no1,
	pub_no2, and cit_weight. Each citation link must be included only once in the table.
<classification_table>
	Name of the classification output table. This table will have four columns: pub_no,
	micro_cluster_no, meso_cluster_no, and macro_cluster_no. Cluster numbers are integers
	starting at zero.
<largest_component>
	Boolean indicating whether the publication classification should include only
	publications belonging to the largest connected component of the citation network
	('true') or all publications ('false').
<n_iterations>
	Number of iterations of the Leiden algorithm (e.g., 50).
<resolution_micro_level>
	Value of the resolution parameter at the micro level.
<pub_threshold_micro_level>
	Minimum number of publications per cluster at the micro level (excluding non-core
	publications).
<resolution_meso_level>
	Value of the resolution parameter at the meso level.
<pub_threshold_meso_level>
	Minimum number of publications per cluster at the meso level (excluding non-core
	publications).
<resolution_macro_level>
	Value of the resolution parameter at the macro level.
<pub_threshold_macro_level>
	Minimum number of publications per cluster at the macro level (excluding non-core
	publications).
```

### Example

The following example illustrates the use of the `PublicationClassificationCreator` tool. Suppose you have a text file `pubs.txt`:

```
0	0
1	0
2	0
3	0
4	0
5	0
6	0
7	0
8	0
9	0
10	0
…
```

You also have a text file `cit_links.txt`

```
1	1516	0.5
1	1988	1
1	25388	1
2	821	0.142857142857143
2	2504	0.0714285714285714
2	24459	0.5
2	24656	0.5
3	1841	0.2
3	2009	0.166666666666667
3	5337	0.0833333333333333
…
```
The `PublicationClassificationCreator` tool can then be run as follows:

```
java -cp publicationclassification-1.1.0.jar nl.cwts.publicationclassification.PublicationClassificationCreator pubs.txt cit_links.txt classification.txt true 100 4e-4 25 2e-4 250 7e-5 1000
```

The publication classification created by the tool can be found in the text file `classification.txt`:

```
1	83	9	3
2	1	1	2
3	43	14	2
4	1	1	2
5	7	7	1
18	49	2	0
19	4	5	0
20	24	0	1
21	33	20	0
22	2	3	0
…
```

The tool displays the following output:

```
PublicationClassificationCreator version 1.1.0
By Nees Jan van Eck
Centre for Science and Technology Studies (CWTS), Leiden University

Reading citation network from file... Finished!
Reading citation network from file took 0h 0m 0s.
Citation network:
	Number of publications: 26800
	Number of citation links: 150613
	Total publication weight: 18643
	Total citation link weight: 18321

Identifying largest connected component in citation network... Finished!
Identifying largest connected component in citation network took 0h 0m 0s.
Largest connected component:
	Number of publications: 20988
	Number of citation links: 150387
	Total publication weight: 17206
	Total citation link weight: 18131

Creating publication classification...
	Clustering algorithm: Leiden algorithm
	Number of iterations: 100
	Random seed: 0

Adding micro-level classification...
Creating clustering... Finished! 335 clusters created.
Reassigning small clusters... Finished! 98 clusters remaining.
Adding micro-level classification took 0h 0m 2s.
Micro-level classification:
	Resolution: 4.0E-4
	Threshold: 25
	Number of clusters: 98

Adding meso-level classification...
Creating clustering... Finished! 63 clusters created.
Reassigning small clusters... Finished! 25 clusters remaining.
Adding meso-level classification took 0h 0m 0s.
Meso-level classification:
	Resolution: 2.0E-4
	Threshold: 250
	Number of clusters: 25

Adding macro-level classification...
Creating clustering... Finished! 9 clusters created.
Reassigning small clusters... Finished! 4 clusters remaining.
Adding macro-level classification took 0h 0m 0s.
Macro-level classification:
	Resolution: 7.0E-5
	Threshold: 1000
	Number of clusters: 4

Writing publication classification to file... Finished!
Writing publication classification to file took 0h 0m 0s.
```

## License

The publicationclassification package is distributed under the [MIT license](LICENSE).

## Issues

If you encounter any issues, please report them using the [issue tracker](https://github.com/CWTSLeiden/publicationclassification/issues) on GitHub.

## Contribution

You are welcome to contribute to the development of the publicationclassification package. Please follow the typical GitHub workflow: Fork from this repository and make a pull request to submit your changes.
Make sure that your pull request has a clear description and that the code has been properly tested.

## Development and deployment

The latest stable version of the source code is available in the [`main`](https://github.com/CWTSLeiden/publicationclassification/tree/main) branch on GitHub. The most recent version of the source code, which may be under development, is available in the [`develop`](https://github.com/CWTSLeiden/publicationclassification/tree/develop) branch.

### Compilation

To compile the source code of the publicationclassification package, a [Java Development Kit](https://jdk.java.net) needs to be installed on your system (version 8 or higher). Having [Gradle](https://www.gradle.org) installed is optional as the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) is also included in this repository.

On Windows systems, the source code can be compiled as follows:

```
gradlew build
```

On Linux and MacOS systems, use the following command:

```
./gradlew build
```

The compiled `class` files can be found in the directory `build/classes`.
The compiled `jar` file can be found in the directory `build/libs`.
The compiled `javadoc` files can be found in the directory `build/docs`.

The class `nl.cwts.publicationclassification.run.PublicationClassificationCreator` has a `main` method. After compiling the source code, the `PublicationClassificationCreator` tool can be run as follows:

```
java -cp build/libs/publicationclassification-<version>.jar nl.cwts.publicationclassification.run.PublicationClassificationCreator
```

## References

> Traag, V.A., Waltman, L., & Van Eck, N.J. (2019). From Louvain to Leiden: Guaranteeing well-connected communities. *Scientific Reports*, *9*, 5233. https://doi.org/10.1038/s41598-019-41695-z

> Waltman, L., Boyack, K.W., Colavizza, G., & Van Eck, N.J. (2020). A principled methodology for comparing relatedness measures for clustering publications. *Quantitative Science Studies*, *1*(2), 691-713. https://doi.org/10.1162/qss_a_00035

> Waltman, L., & Van Eck, N.J. (2012). A new methodology for constructing a publication-level classification system of science. *Journal of the American Society for Information Science and Technology*, *63*(12), 2378-2392. https://doi.org/10.1002/asi.22748
