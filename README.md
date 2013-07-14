ClodHopper: A High Performance Java Library for Data Clustering
===============================================================

MISSION STATEMENT:
------------------

ClodHopper is a open source Java library for high-performance clustering of numerical data.  
It contains clustering implementations such as K-Means, K-Means++, X-Means, G-Means, Fuzzy C-Means, 
and various forms of hierarchical clustering. ClodHopper's clustering implementations take advantage 
of the host system's concurrent processing ability in order to speed up clustering. The data 
structures are also very lean in order to conserve on memory usage.  ClodHopper is also 
very extensible.  If you are developing a new clustering algorithm, you may save yourself an 
enormous amount of work by extending a ClodHopper base class.

LICENSING:
----------

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

PRIMARY CONTACT:
----------------  

Randall Scarberry, email: drrandys@yahoo.com

How to get started with ClodHopper:
-----------------------------------

1. Download a copy of the source code from the Git bash prompt or the Git 
UI of your choice
	> `git clone https://github.com/rscarberry-wa/clodhopper.git`
	
2. In the newly-created clodhopper directory, you will find the subdirectories
clodhopper and clodhopper_examples.  The first contains a maven project for
clodhopper proper.  The second contains a project of numerous examples. 
I recommend importing both projects into eclipse or the IDE of your choice.

3. The simplest example shows you how to use k-means to cluster a csv
file containing numeric data.  The example is contained in the file:

  org/battelle/clodhopper/examples/kmeans/SimpleKMeansDemo.java
  
4. Also check out the following demos:

	a. org.battelle.clodhopper.examples.multiple.GeneratedDataPanel

	This example runs several of the clustering algorithms in sequence on generated data. As they
	complete, it display scatter plots with the clusters collapsed into 2 dimensions.
	You can drag your mouse to select clusters and points in any of the plots and
	the selections propagate to the others, so you can see how they correspond.
	
	b.  org.battelle.clodhopper.examples.ui.ClodHopperUI

	This example permits you to read in a csv data file and cluster the data using many
	of the algorithms in the library using just about any parameter setting you please.  Then you
	can save the clustering results in a simple csv file.
	
5. Watch for more! ClodHopper is just getting started.
