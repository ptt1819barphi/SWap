import pandas as pd
import sys
import matplotlib.pyplot as plt
import numpy as np
from utils import getAmountByColumn

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print(sys.argv[0], "NER PLOT1 PLOT2 PLOT3")
        sys.exit()
    
    df = pd.read_json(sys.argv[1], "columns")
   
    dfOrg = getAmountByColumn(df.drop("people", axis=1), "organizations").tail(20)
    dfPeo = getAmountByColumn(df.drop("organizations", axis=1), "people")
    blacklist = ["He", "he", "She", "she", "His", "his", "Her", "her", "Him", "him"]
    dfPeoB = dfPeo.loc[~dfPeo.index.isin(blacklist)].tail(20)
    dfPeo = dfPeo.tail(20)

    plot = dfOrg.plot(kind="barh", title="Organization frequency", legend=False, rot=0, color=[plt.cm.tab20(np.append(np.arange(1, dfOrg.size, 2), np.arange(0, dfOrg.size, 2)))])
    plot.set_xlabel("Frequency")
    plot.set_ylabel("Organization")
    plot.get_figure().savefig(sys.argv[2], bbox_inches='tight')

    plot = dfPeo.plot(kind="barh", title="Person frequency", legend=False, rot=0, color=[plt.cm.tab20(np.append(np.arange(1, dfPeo.size, 2), np.arange(0, dfPeo.size, 2)))])
    plot.set_xlabel("Frequency")
    plot.set_ylabel("Person")
    plot.get_figure().savefig(sys.argv[3], bbox_inches='tight')

    plot = dfPeoB.plot(kind="barh", title="Person frequency", legend=False, rot=0, color=[plt.cm.tab20(np.append(np.arange(1, dfPeoB.size, 2), np.arange(0, dfPeoB.size, 2)))])
    plot.set_xlabel("Frequency")
    plot.set_ylabel("Person")
    plot.get_figure().savefig(sys.argv[4], bbox_inches='tight')
