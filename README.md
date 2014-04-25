feature-selection
=================
This implementation to calculate the feature selection score in three different algorithms:
pmi: Pointwise mutual information -  reference: http://en.wikipedia.org/wiki/Pointwise_mutual_information
avg: average rule.
chi2: Pearson's chi-squared test  - reference: http://en.wikipedia.org/wiki/Pearson%27s_chi-squared_test

works on binary datasets as input see below:
===========
A 1:1 2:1 3:0 4:1
B 1:0 2:1 3:0 4:0
C 1:0 2:1 3:0 4:0
A 1:1 2:1 3:0 4:0
A 1:1 2:1 3:1 4:0
C 1:0 2:1 3:1 4:0
============
The input file in Libsvm format - reference: http://www.csie.ntu.edu.tw/~cjlin/libsvm/ - http://www.csie.ntu.edu.tw/~cjlin/libsvmtools/datasets/
The first column is the label column, then each column after represents a feature values related to the label

"1:0": the first integer represents the key of the feature, then a seperator ":", then another binary value [0|1] represent if this feature is:    1: active      or       0: not active.




Usage:
>java eu.fbk.phd.featureSelection <arg1> <arg2> <arg3> [<arg4>]
<arg1> Input file name.
<arg2> Choose the feature selection metric[chi|avg|pmi].
<arg3> Output file name.
<arg4> Select the active feature index(s): '0,2,3', otherwise all the features will be considered.


Example: to run avg method: java eu.fbk.phd.featureSelection test/test avg test/oo.txt
The output file:
2	1.0
1	0.5
3	0.3333333432674408
4	0.1666666716337204


This first column has integers represent the feature column index, in the same row of each index you could see the score for it, depending on the running method.
