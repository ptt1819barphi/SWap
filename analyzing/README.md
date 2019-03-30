# Build Instructions
## Requirements
* Python 3.7+
* pandas
* NumPy
* Matplotlib

## Usage
The required files are from the SWap and export application in this repo.
Before using one of these scripts make sure you have at least 6G RAM free.

```bash
$ python similarityBetweenDepths.py DEPTH-FILE MAPPING-FILE NOUNS PLOT1 PLOT2 PLOT3
$ python nounFrequency.py DEPTH-FILE MAPPING-FILE NOUNS MAX-DEPTHS PLOT
$ python nerFrequency.py NER PLOT1 PLOT2 PLOT3
$ python similarityBetweenCategories.py DEPTH-FILE DEPTH-FILE2 MAPPING-FILE NOUNS MAX-DEPTH PLOT
```