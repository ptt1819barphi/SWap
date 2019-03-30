import pandas as pd
import sys
from ndd import determineNDD, compareArticles
from utils import same, aggs

if __name__ == "__main__":
    if len(sys.argv) != 7:
        print(sys.argv[0], "DEPTH-FILE DEPTH-FILE2 MAPPING-FILE NOUNS MAX-DEPTH PLOT")
        sys.exit()

    depths = int(sys.argv[5])
    
    dfDepth1 = pd.read_csv(sys.argv[1], ',')
    dfDepth1 = dfDepth1[dfDepth1["depth"] <= depths]
    depth1Name = dfDepth1[dfDepth1["depth"] == 0]["category"].iloc[0]
    dfDepth1 = dfDepth1.drop("depth", axis=1)

    dfDepth2 = pd.read_csv(sys.argv[2], ',')
    dfDepth2 = dfDepth2[dfDepth2["depth"] <= depths]
    depth2Name = dfDepth2[dfDepth2["depth"] == 0]["category"].iloc[0]
    dfDepth2 = dfDepth2.drop("depth", axis=1)

    mapping = pd.read_csv(sys.argv[3], ',')

    dfDepth1 = pd.merge(dfDepth1, mapping, "inner", "category")
    dfDepth1 = dfDepth1.drop("category", axis=1)
    dfDepth1 = dfDepth1.drop_duplicates()

    dfDepth2 = pd.merge(dfDepth2, mapping, "inner", "category")
    dfDepth2 = dfDepth2.drop("category", axis=1)
    dfDepth2 = dfDepth2.drop_duplicates()

    dfNouns = pd.read_json(sys.argv[4], "columns")

    dfDepth1 = pd.merge(dfDepth1, dfNouns, "inner", "page_title")
    dfDepth2 = pd.merge(dfDepth2, dfNouns, "inner", "page_title")
    

    ndd1 = determineNDD(dfDepth1, compAllWithMean=False)
    ndd2 = determineNDD(dfDepth2, compAllWithMean=False)
   
    nouns1 = aggs(dfDepth1["nouns"])
    nouns2 = aggs(dfDepth2["nouns"])
    

    result = {
        "NDDI": compareArticles(ndd1.getAverageNounDf(), ndd2.getAverageNounDf()),
        "%s\nto\n%s" % (depth1Name, depth2Name): 100*same(nouns1, nouns2)/len(nouns1),
        "%s\nto\n%s" % (depth2Name, depth1Name): 100*same(nouns2, nouns1)/len(nouns2),
    }
    
    resultDF = pd.DataFrame.from_dict(result, "index", columns=["percent"])
    plot = resultDF.plot(kind="bar", title="Similarity between categories with depth %i" % depths, legend=False, ylim=(0,100), rot=0)
    plot.set_xlabel("Compare method")
    plot.set_ylabel("Similarity in percent")

    plot.get_figure().savefig(sys.argv[6], bbox_inches='tight')