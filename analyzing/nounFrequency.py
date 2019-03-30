import pandas as pd
import sys
import matplotlib.pyplot as plt
import numpy as np
from utils import getAmountByColumn

if __name__ == "__main__":
    if len(sys.argv) != 6:
        print(sys.argv[0], "DEPTH-FILE MAPPING-FILE NOUNS MAX-DEPTHS PLOT")
        sys.exit()
    
    df = pd.merge(pd.read_csv(sys.argv[1], ','),  pd.read_csv(sys.argv[2], ','), "inner", "category")
    df = df.drop("category", axis=1)

    df = pd.merge(df, pd.read_json(sys.argv[3], "columns"), "inner", "page_title")

    df = df[df["depth"] <= int(sys.argv[4])]

    df = df.drop("depth", axis=1)
    df = df.drop_duplicates(subset=["page_title"])

    df = getAmountByColumn(df, "nouns").tail(20)
    plot = df.plot(kind="barh", title="Noun frequency", legend=False, rot=0, color=[plt.cm.tab20(np.append(np.arange(1, df.size, 2), np.arange(0, df.size, 2)))])
    plot.set_xlabel("Frequency")
    plot.set_ylabel("Noun")

    plot.get_figure().savefig(sys.argv[5], bbox_inches='tight')
