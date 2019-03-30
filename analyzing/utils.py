import pandas as pd

def aggs(l):
    s = set()
    for x in l:
        s.update(x)
    return s

def same(small, big):
    amount = 0
    for word in small:
        if word in big:
            amount += 1
    return amount

def getAmountByColumn(df, column):
    df = df[df.astype(str)[column] != "[]"]
   
    columnAmount = dict()
    for columnSet in df[column]:
        for c in columnSet:
            if c not in columnAmount:
                columnAmount[c] = 0
            columnAmount[c] += 1

    return pd.DataFrame.from_dict(columnAmount, "index", columns=["amount"]).sort_values("amount")
