import csv
import json
import pymysql
import sys, getopt
from pymysql.cursors import SSDictCursor, DictCursor

def binaryFieldsToUTF8(row):
    for l in row:
        if isinstance(row[l], bytes):
            row[l] = row[l].decode("utf-8")
    return row

def writeToCSV(conn, sql, path, amount=None, msg=None):
    cursor = conn.cursor(cursor=SSDictCursor)
    cursor.execute(sql)
    header = [ele[0] for ele in cursor.description]
    file = open(path, "w", newline='', encoding="UTF-8")
    writer = csv.DictWriter(file, fieldnames=header, dialect=csv.unix_dialect)
    writer.writeheader()

    count = 0
    while True:
        row = cursor.fetchone()
        if row:
            binaryFieldsToUTF8(row)
            writer.writerow(row)
            count += 1
            if amount:
                updateProgress(count, amount, msg)
        else:
            break
    if amount:
        print()
    cursor.close()
    file.close()
    return count

def writeSubcategoriesAndGetCategories(conn, path, maxDepth, categorySet, categoryBlackset):
    visitedCategories = set(categorySet)
    if (maxDepth < 1):
        return visitedCategories
    
    newCategories = set(categorySet)
    depth = 0

    file = open(path, "w", newline='', encoding="utf-8")
    writer = csv.DictWriter(file, fieldnames=["category", "subcategory"], dialect=csv.unix_dialect)
    writer.writeheader()
    
    sql = """SELECT cl.cl_to AS "category", p.page_title AS "subcategory" FROM page AS p
    INNER JOIN categorylinks AS cl ON p.page_id = cl.cl_from
    WHERE cl_type = "subcat"
    AND NOT p.page_id IN (SELECT pp_page FROM page_props WHERE pp_propname = "hiddencat")
    AND cl.cl_to IN (%s);"""
    with conn.cursor(cursor=SSDictCursor) as cursor:
        updateProgress(0, maxDepth, "depths analized.")
        while depth < maxDepth and newCategories:
            cursor.execute(sql.replace("%s", '", "'.join(newCategories).join(['"', '"'])))
            newCategories = set()
            while True:
                row = cursor.fetchone()
                if row:
                    row = binaryFieldsToUTF8(row)
                    if row["subcategory"] in categoryBlackset:
                        continue
                    writer.writerow(row)
                    if row["subcategory"] not in visitedCategories:
                        visitedCategories.add(row["subcategory"])
                        newCategories.add(row["subcategory"])
                else:
                    break
            depth += 1
            updateProgress(depth, maxDepth, "depths analized.")
        print()

    file.close()
    return visitedCategories


def writePageCategoryMappingAndGetPageids(conn, path, uniqueCategories):
    file = open(path, "w", newline='', encoding="UTF-8")
    writer = csv.DictWriter(file, fieldnames=["category", "page_title"], dialect=csv.unix_dialect, extrasaction="ignore")
    writer.writeheader()

    pageIds = set()

    sql = """SELECT cl.cl_to AS "category", p.page_id, p.page_title FROM categorylinks AS cl
    INNER JOIN page AS p ON p.page_id = cl.cl_from
    WHERE p.page_namespace = 0 AND p.page_is_redirect = 0 AND cl.cl_type = "page" AND cl.cl_to IN (%s);
    """.replace("%s", '", "'.join(uniqueCategories).join(['"', '"']))
    with conn.cursor(cursor=SSDictCursor) as cursor:
        cursor.execute(sql)
        while True:
            row = cursor.fetchone()
            if row:
                row = binaryFieldsToUTF8(row)
                writer.writerow(row)
                pageIds.add(str(row["page_id"]))
            else:
                break

    file.close()
    return pageIds

def updateProgress(current, max, msg):
    print("\r(%i/%i) %.2f%% %s" % (current, max, current/max*100, msg), end="")

if __name__ == "__main__":
    try:
        opts, args = getopt.getopt(sys.argv[1:], "c:", ["connection="])
        if len(args) < 5:
            raise getopt.GetoptError("Missing args.")
    except getopt.GetoptError:
        print(sys.argv[0] + " [-c CONNECTION-FILE | --connection=CONNECTION-FILE] CATEGORY-FILE CATEGORY-PAGE-MAPPING-FILE PAGE-FILE MAXDEPTH CATEGORY...")
        sys.exit()

    if opts:
        print("file found")
        with open(opts[0][1], "r", encoding="UTF-8") as f:
            connectionDetails = json.load(f)
    else:
        connectionDetails = {"host":"localhost", "port":3306, "database":"enwiki", "username":"root", "password":None}

    conn = pymysql.Connect(host=connectionDetails["host"], port=connectionDetails["port"], db=connectionDetails["database"], user=connectionDetails["username"], passwd=connectionDetails["password"])
    categorySet = set(filter(lambda x: not x.startswith("-"), args[4:]))
    categoryBlackset = set(map(lambda x: x[1:], filter(lambda x: x.startswith("-"), args[4:])))

    print("Searching for subcategories...")
    uniqueCategories = writeSubcategoriesAndGetCategories(conn, args[0], int(args[3]), categorySet, categoryBlackset)
    del categorySet
    del categoryBlackset
    print("%i unique categories found." % len(uniqueCategories))
    print("Searching for pages in selected categories...")

    pageIds = writePageCategoryMappingAndGetPageids(conn, args[1], uniqueCategories)
    del uniqueCategories
    amount = len(pageIds)
    print("%i pages found." % amount)
    updateProgress(0, amount, "pages written.")

    sql="""SELECT p.page_title, t.old_text AS "text" FROM  page AS p
    INNER JOIN revision AS r ON r.rev_page = p.page_id
    INNER JOIN text AS t ON t.old_id = r.rev_text_id
    WHERE p.page_id IN (%s);
    """.replace("%s", ",".join(pageIds))
    
    count = writeToCSV(conn, sql, args[2], amount=amount, msg="pages written.")
    if count != amount:
        print("Missmatch between unique pages (%i) and written pages (%i)." % (amount, count))
    conn.close()
