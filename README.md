# MultiGraphMatch

Subgraph matching is the problem of finding all the occurrences of a small graph, called the query, in a larger graph, called the target. While the problem has been widely studied in simple graphs, few solutions have been proposed for multi-relational graphs, in which two nodes can be connected by multiple edges, each denoting a different type of relationship. In our new algorithm MultiGraphMatch, nodes and edges can be associated with labels, denoting classes, and multiple properties. MultiGraphMatch introduces a novel data structure called a "bit signature" to efficiently index both the query and the target and filter the set of target edges that are
matchable with each query edge. In addition, the algorithm proposes a new order of processing query edges based on the cardinalities of the sets of matchable edges and defines symmetry breaking conditions on nodes and edges to filter out redundant matches. By using the CYPHER query definition language, MultiGraphMatch
is able to perform queries with logical conditions on node and edge labels. We compare MultiGraphMatch to SumGra and graph database systems Memgraph and Neo4J, showing comparable or better performance in all queries on a wide variety of synthetic and real-world graphs.


# Networks 

The networks used for tests and comparisons are downloadable through the following link:

https://zenodo.org/records/10401776?token=eyJhbGciOiJIUzUxMiJ9.eyJpZCI6IjAwZThjMDZkLTdiZjQtNGYwYS04ZDZiLThhN2VkZTZhMTVhNyIsImRhdGEiOnt9LCJyYW5kb20iOiI2MjAwNmQ4ZmExN2U4YTc2YWQ5YjgxNGNhYWFjNTU0NyJ9.1xNfcXD4xgT8zg7Dwj29IOLPSxVKXDWNYxIxlg1DY5iZXv4iCF2wTBXio_1TsNZPgVDSDqB1698CFDEbYk4dXA
