import pandas as pd
import sys
from ndd import determineNDD, compareArticles
from utils import same, aggs

if __name__ == "__main__":
    if len(sys.argv) != 7:
        print(sys.argv[0], "DEPTH-FILE MAPPING-FILE NOUNS PLOT1 PLOT2 PLOT3")
        sys.exit()
    
    df = pd.merge(pd.read_csv(sys.argv[1], ','),  pd.read_csv(sys.argv[2], ','), "inner", "category")
    df = df.drop("category", axis=1)
    df = df.drop_duplicates()

    df = pd.merge(df, pd.read_json(sys.argv[3], "columns"), "inner", "page_title")

    dfSim = df.drop("page_title", axis=1)
    dfSim = dfSim.groupby("depth").agg(aggs)["nouns"]

    result = dict()
    for x in range(dfSim.size - 1):
        matches = same(dfSim[x], dfSim[x + 1])
        percent = matches/len(dfSim[x]) * 100
        result["%i to %i" % (x, x+1)] = percent

    resultDF = pd.DataFrame.from_dict(result, "index", columns=["percent"])
    plot = resultDF.plot(kind="bar", title="Similarity between category levels", legend=False, ylim=(0,100), rot=0)
    plot.set_xlabel("Category level")
    plot.set_ylabel("Similarity in percent")
    plot.get_figure().savefig(sys.argv[4], bbox_inches='tight')
    
    #NDDI:
    depths = df["depth"].drop_duplicates().size
    ndds = []
    for x in range(depths):
        ndd = determineNDD(df[df["depth"] == x].drop("depth", axis=1))
        ndds.append(ndd)

    result = dict()
    for x in range(depths - 1):
        percent = compareArticles(ndds[x].getAverageNounDf(), ndds[x+1].getAverageNounDf())
        result["%i with %i" % (x, x+1)] = percent
    
    resultDF = pd.DataFrame.from_dict(result, "index", columns=["percent"])
    plot = resultDF.plot(kind="bar", title="Similarity between category levels using NDDI", legend=False, ylim=(0,100), rot=0)
    plot.set_xlabel("Category level")
    plot.set_ylabel("Similarity in percent")
    plot.get_figure().savefig(sys.argv[5])

    result = dict()
    for x in range(depths):
        result[x] = ndds[x].getAverageNDDI()
    
    resultDF = pd.DataFrame.from_dict(result, "index", columns=["percent"])
    plot = resultDF.plot(kind="bar", title="Similarity within category levels using NDDI", legend=False, ylim=(0,10), rot=0)
    plot.set_xlabel("Category level")
    plot.set_ylabel("Similarity in percent")
    plot.get_figure().savefig(sys.argv[6], bbox_inches='tight')
