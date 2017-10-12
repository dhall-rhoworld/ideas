#! /usr/bin/python

import sys
from scipy.io import arff
import pandas as pd
import itertools
import random
from scipy import stats
import numpy as np
from sklearn.neighbors import NearestNeighbors
from sklearn.preprocessing import MinMaxScaler
from sklearn import metrics


def knn(df,k):
	nbrs = NearestNeighbors(n_neighbors=3)
	nbrs.fit(df)
	distances, indices = nbrs.kneighbors(df)
	return distances, indices
	
def reachDist(df,MinPts,knnDist):
	nbrs = NearestNeighbors(n_neighbors=MinPts)
	nbrs.fit(df)
	distancesMinPts, indicesMinPts = nbrs.kneighbors(df)
	distancesMinPts[:,0] = np.amax(distancesMinPts,axis=1)
	distancesMinPts[:,1] = np.amax(distancesMinPts,axis=1)
	distancesMinPts[:,2] = np.amax(distancesMinPts,axis=1)
	return distancesMinPts, indicesMinPts

def comboGenerator(startPoint, space, n):
	combosFinal = []
	for item in itertools.combinations(list(set(space)-set(startPoint)),(n - len(startPoint))):
		combosFinal.append(sorted(startPoint + list(item)))
	return combosFinal
	
def ird(MinPts,knnDistMinPts):
	return (MinPts/np.sum(knnDistMinPts,axis=1))

def lof(Ird,MinPts,dsts):
	lof=[]
	for item in dsts:
		tempIrd = np.divide(Ird[item[1:]],Ird[item[0]])
		lof.append(tempIrd.sum()/MinPts)
	return lof

# Retrieve file name from command line
numArgs = len(sys.argv)
if numArgs != 2:
	print "Usage: hics.py fname"
	sys.exit(1)
fname = sys.argv[1]

# Load data
print "Loading file " + fname
data, meta = arff.loadarff(fname)
df = pd.DataFrame(data)

index_df = (df.rank() / df.rank().max()).iloc[:,:-1]
listOfCombos = comboGenerator([], df.columns[:-1], 2)
testedCombos = []
selection = []


while(len(listOfCombos) > 0):
	if listOfCombos[0] not in testedCombos:
		alpha1 = pow(0.2,(float(1)/float(len(listOfCombos[0]))))
		pvalue_Total =0
		pvalue_cnt = 0
		avg_pvalue=0
		for i in range(0,50):
			lband = random.random()
			uband = lband+alpha1
			v = random.randint(0,(len(listOfCombos[0])-1))
			rest = list(set(listOfCombos[0])-set([listOfCombos[0][v]]))
			k, pvalue=stats.ks_2samp(df[listOfCombos[0][v]].values, df[((index_df[rest]<uband) & (index_df[rest]>lband)).all(axis=1)][listOfCombos[0][v]].values)
			#print "iter:{4},lband:{0},uband:{1},v:{2},pvalue:{3},length:{5},rest:{6}".format(lband,uband,v,k.pvalue,i,len(df[((index_df[rest]<uband) & (index_df[rest]>lband)).all(axis=1)][listOfCombos[0][v]]),rest)
			if not(np.isnan(pvalue)):
				pvalue_Total = pvalue_Total + pvalue
				pvalue_cnt = pvalue_cnt + 1
		if pvalue_cnt > 0:
			avg_pvalue = pvalue_Total/pvalue_cnt
			print avg_pvalue
		if (1.0-avg_pvalue) > 0.75:
			selection.append(listOfCombos[0])
			listOfCombos = listOfCombos + comboGenerator(listOfCombos[0],df.columns[:-1],(len(listOfCombos[0])+1))
		testedCombos.append(listOfCombos[0])
		listOfCombos.pop(0)
		listOfCombos = [list(t) for t in set(map(tuple,listOfCombos))]
	else:
		listOfCombos.pop(0)
		
print "Selections:"
print selection
		
scoresList=[]
for item in selection:
	m=50
	knndist, knnindices = knn(df[item],3)
	reachdist, reachindices = reachDist(df[item],m,knndist)
	irdMatrix = ird(m,reachdist)
	lofScores = lof(irdMatrix,m,reachindices)
	scoresList.append(lofScores)
	
print "Scores:"
#print scoresList

avgs = np.nanmean(np.ma.masked_invalid(np.array(scoresList)),axis=0)

scaled_avgs = MinMaxScaler().fit_transform(avgs.reshape(-1,1))

print "HCiS AUC Score"
print metrics.roc_auc_score(pd.to_numeric(df['class'].values),scaled_avgs)

m=50
knndist, knnindices = knn(df.iloc[:,:-1],3)
reachdist, reachindices = reachDist(df.iloc[:,:-1],m,knndist)
irdMatrix = ird(m,reachdist)
lofScores = lof(irdMatrix,m,reachindices)
ss=MinMaxScaler().fit_transform(np.array(lofScores).reshape(-1,1))
print "LOF AUC Score"
print metrics.roc_auc_score(pd.to_numeric(df['class'].values),ss)