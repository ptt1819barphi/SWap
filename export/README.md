# Build Instructions
## Requirements
* Python 3.7+
* Java
* PyMySQL (https://github.com/PyMySQL/PyMySQL)
* MariaDB / MySQL Server
* MWDumper (https://github.com/wikimedia/mediawiki-tools-mwdumper)

## Prepare
### Downloads
Download the latest WikipediaDump from:  
https://dumps.wikimedia.org/backup-index.html

You'll need the following files:
* enwiki-${date}-pages-articles-multistream.xml.bz2
* enwiki-${date}-categorylinks.sql.gz
* enwiki-${date}-page_props.sql.gz  

### SQL Server
Make sure to provide your server with enough working memory and disk space. 
It doesn't matter to which adress or port you host your server but avoid high latency.
We **recommend** to use a fresh database setup.

### Files
Decompress the files as follows:
```bash
$ gzip -d enwiki-${date}-categorylinks.sql.gz
$ gzip -d enwiki-${date}-page_props.sql.gz

# Note: the MWDumper is (theoretically) capable of decompressing
# the file on load, sadly this feature didn't seem to work for us :/
$ bzip2 -d enwiki-${date}-pages-articles-multistream.xml.bz2
```

The following instruction returns a line number, which will be used as <code>${line}</code> later on:
```bash
$ grep -n -m 1 "-- Dumping data for table" enwiki-${date}-categorylinks.sql
> 41:-- Dumping data for table `categorylinks`
```

This command cuts the create instructions from this table, as there are too many keys by default, which in turn slow down the database import.
```bash
$ tail -n +${line} enwiki-${date}-categorylinks.sql > enwiki-${date}-categorylinks-data.sql
```

```bash
$  java -jar mwdumper.jar --format=mysql:1.25 enwiki-${date}-pages-articles-multistream.xml > enwiki-${date}-pages-articles.sql
```

### Table Creation

Create a file named **tables.sql** with this content:
```sql
CREATE TABLE categorylinks (
  cl_from int unsigned NOT NULL default 0,
  cl_to varchar(255) binary NOT NULL default '',
  cl_sortkey varbinary(230) NOT NULL default '',
  cl_sortkey_prefix varchar(255) binary NOT NULL default '',
  cl_timestamp timestamp NOT NULL,
  cl_collation varbinary(32) NOT NULL default '',
  cl_type ENUM('page', 'subcat', 'file') NOT NULL default 'page',
  PRIMARY KEY (cl_from,cl_to)
) ENGINE=InnoDB DEFAULT CHARSET=binary;


CREATE TABLE page (
  page_id int unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT,
  page_namespace int NOT NULL,
  page_title varchar(255) binary NOT NULL,
  page_restrictions tinyblob NOT NULL,
  page_is_redirect tinyint unsigned NOT NULL default 0,
  page_is_new tinyint unsigned NOT NULL default 0,
  page_random real unsigned NOT NULL,
  page_touched binary(14) NOT NULL default '',
  page_latest int unsigned NOT NULL,
  page_len int unsigned NOT NULL,
  page_content_model varbinary(32) DEFAULT NULL,
  page_lang varbinary(35) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=binary;

CREATE TABLE redirect (
  rd_from int unsigned NOT NULL default 0 PRIMARY KEY,
  rd_namespace int NOT NULL default 0,
  rd_title varchar(255) binary NOT NULL default '',
  rd_interwiki varchar(32) default NULL,
  rd_fragment varchar(255) binary default NULL
) ENGINE=InnoDB DEFAULT CHARSET=binary;

CREATE TABLE revision (
  rev_id int unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT,
  rev_page int unsigned NOT NULL,
  rev_text_id int unsigned NOT NULL default 0,
  rev_comment varbinary(767) NOT NULL default '',
  rev_user int unsigned NOT NULL default 0,
  rev_user_text varchar(255) binary NOT NULL default '',
  rev_timestamp binary(14) NOT NULL default '',
  rev_minor_edit tinyint unsigned NOT NULL default 0,
  rev_deleted tinyint unsigned NOT NULL default 0,
  rev_len int unsigned,
  rev_parent_id int unsigned default NULL,
  rev_sha1 varbinary(32) NOT NULL default '',
  rev_content_model varbinary(32) DEFAULT NULL,
  rev_content_format varbinary(64) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=binary;

CREATE TABLE text (
  old_id int unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT,
  old_text mediumblob NOT NULL,
  old_flags tinyblob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=binary;
```

Keep in mind to add the connection details to the <code>mysql</code> command.
```bash
$ cat tables.sql | mysql ${database}
```

### Data Import
The following commands, may have an excessive execution time (depending on the machine).

```bash
$ cat enwiki-${date}-page_props.sql | mysql ${database}
$ cat enwiki-${date}-categorylinks.sql | mysql ${database}
$ cat enwiki-${date}-pages-articles.sql | mysql ${database}
```

### Adding missing Indices
Create a file named **indices.sql** with this content:
```sql
CREATE INDEX rev_page_id ON revision (rev_page, rev_id);
```
```bash
$ cat indices.sql | mysql ${database}
```

## Using the export.py

```bash
export.py [-c CONNECTION-FILE | --connection=CONNECTION-FILE] CATEGORY-FILE CATEGORY-PAGE-MAPPING-FILE PAGE-FILE MAXDEPTH CATEGORY...
```
* **CONNECTION-FILE**: JSON-File that contains connection details to your SQL-Server. See sample-connection.json.

* **CATEGORY-FILE**: Outputs a CSV-File, which contains the category-subcategory relations. 

* **CATEGORY-PAGE-MAPPING-FILE**: Outputs a CSV-File, which contains the category-pagename relations.

* **PAGE-FILE**: Outputs a CSV-File, which contains the pageTitle-pageContent relations.

* **MAXDEPTH**: Integer parameter, which defines the max depth based on the start categories.

* **CATEGORY...** : String... parameter, which defines the start categories, ignores categories which start with a "-".

**Example:**
```bash
export.py -c sample-connection.json categories.csv mapping.csv pages.csv 5 Software -Software_engineering
```