import pandas as pd
import sys

class NDD:

    def __init__(self, resultDF, averageNounDf):
        self.resultDF = resultDF
        self.averageNounDf = averageNounDf

    def getResultDF(self):
        return self.resultDF
        
    def getAverageNounDf(self):
        return self.averageNounDf

    def getAverageNDDI(self):
        return self.resultDF["NDDI"].mean()

    def getNDDI(self, page_title):
        return self.resultDF.get(page_title)

    def getArticleWithBestNDDI(self):
        return self.resultDF[self.resultDF["NDDI"] == self.resultDF["NDDI"].max()]

    def getArticleWithWorstNDDI(self):
        return self.resultDF[self.resultDF["NDDI"] == self.resultDF["NDDI"].min()]

def determineNDD(nounDF, compAllWithMean=True):
    # determining averageNounSet
    averageNounSet = dict()
    for nounSet in nounDF["nouns"]:
        for noun in nounSet:
            # check if noun is in averageNounSet
            if noun in averageNounSet:
                # sum amounts if entry present
                averageNounSet[noun] += nounSet[noun]
            else:
                # add entry if absent
                averageNounSet[noun] = nounSet[noun]

    averageNounDf = pd.DataFrame.from_dict(averageNounSet, "index", columns=["amount"])
    amountNounSets =  len(nounDF["nouns"])
    averageNounDf["amount"] = averageNounDf["amount"].apply(lambda x: int(x / amountNounSets))
    averageNounDf = averageNounDf[averageNounDf["amount"] >= 1] 

    if compAllWithMean:
        nddData = nounDF.copy()
        nddData["nouns"] = nddData["nouns"].apply(lambda x: pd.DataFrame.from_dict(x, "index", columns=["amount"]))
        nddData["nouns"] = nddData["nouns"].apply(lambda x: compareArticles(x, averageNounDf))
        nddData = nddData.rename(index=str, columns={"nouns":"NDDI"})
        nddData = nddData.sort_values(by=['NDDI'], ascending=False).set_index("page_title")
    else:
        nddData = pd.DataFrame(columns=["NDDI"])
    
    return NDD(nddData, averageNounDf)

def compareArticles(art1, art2):
    if (art1.size == 0 or art2.size == 0):
        return 0.0

    # IoU calculation
    A = set(art1.index.to_list())
    B = set(art2.index.to_list())
    intersection = A & B
    union = A | B
    IoU = len(intersection) / len(union)

    if (IoU == 0.0):
        return 0.0

    # NCS calculation
    NCS = 0.0
    for noun in intersection:
        amount1 = art1["amount"][noun]
        amount2 = art2["amount"][noun]
        NCS += min(amount1, amount2) / max(amount1, amount2)
    NCS /= len(intersection)

    return IoU * NCS * 100

if __name__ == "__main__":
    if len(sys.argv) != 6:
        print(sys.argv[0], " DEPTH-FILE MAPPING-FILE NOUNS PLOT1 PLOT2")
        sys.exit()
    
    df = pd.merge(pd.read_csv(sys.argv[1], ','),  pd.read_csv(sys.argv[2], ','), "inner", "category")
    df = df.drop("category", axis=1)
    df = df.drop_duplicates()

    df = pd.merge(df, pd.read_json(sys.argv[3], "columns"), "inner", "page_title")

    depths = df["depth"].drop_duplicates().size
    ndds = []
    for x in range(depths):
        # determine the Noun Distribution Dispersion
        # for the selected set of articles 
        ndd = determineNDD(df[df["depth"] == x].drop("depth", axis=1))

        # show the resulting Data Frame
        print()
        print(x)
        print("Resulting NounDF:\n", ndd.getAverageNounDf(), "\n")
        print(ndd.getAverageNDDI())
        ndds.append(ndd)

    result = dict()
    for x in range(depths - 1):
        percent = compareArticles(ndds[x].getAverageNounDf(), ndds[x+1].getAverageNounDf())
        print("%i to %i: %.2f%%" % (x, x+1, percent))
        result["%i to %i" % (x, x+1)] = percent
    
    resultDF = pd.DataFrame.from_dict(result, "index", columns=["percent"])
    plot = resultDF.plot(kind="bar", title="Similarity between category levels using NDDI", legend=False, ylim=(0,100), rot=0)
    plot.set_xlabel("Category level")
    plot.set_ylabel("Similarity in percent")

    plot.get_figure().savefig(sys.argv[4])


    result = dict()

    for x in range(depths):
        result[x] = ndds[x].getAverageNDDI()
    
    resultDF = pd.DataFrame.from_dict(result, "index", columns=["percent"])
    plot = resultDF.plot(kind="bar", title="Similarity within category levels using NDDI", legend=False, ylim=(0,10), rot=0)
    plot.set_xlabel("Category level")
    plot.set_ylabel("Similarity in percent")

    plot.get_figure().savefig(sys.argv[5], bbox_inches='tight')
