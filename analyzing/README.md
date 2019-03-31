# Build Instructions
## Requirements
* Python 3.7+
* pandas
* NumPy
* Matplotlib

## Usage
The required files are from the SWap and export application in this repo.
Before using one of these scripts make sure you have at least 8G RAM free.

```bash
$ python similarityBetweenDepths.py DEPTH-FILE MAPPING-FILE NOUNS PLOT1 PLOT2 PLOT3
$ python nounFrequency.py DEPTH-FILE MAPPING-FILE NOUNS MAX-DEPTHS PLOT
$ python nerFrequency.py NER PLOT1 PLOT2 PLOT3
$ python similarityBetweenCategories.py DEPTH-FILE DEPTH-FILE2 MAPPING-FILE NOUNS MAX-DEPTH PLOT
```

## Examples
```bash
$ python similarityBetweenDepths.py workspace/out/software_depth.csv workspace/combined/7/mapping.csv workspace/out/nouns.json workspace/plots/software/similarity_depths.svg workspace/plots/software/similarity_depths_nddi.svg workspace/plots/software/similarity_category_nddi.svg
$ python similarityBetweenDepths.py workspace/out/software_engineering_depth.csv workspace/combined/7/mapping.csv workspace/out/nouns.json workspace/plots/software_engineering/similarity_depths.svg workspace/plots/software_engineering/similarity_depths_nddi.svg workspace/plots/software_engineering/similarity_category_nddi.svg
$ python nounFrequency.py workspace/out/software_depth.csv workspace/combined/4/mapping.csv workspace/out/nouns.json 4 workspace/plots/software/noun_frequency.svg
$ python nounFrequency.py workspace/out/software_engineering_depth.csv workspace/combined/4/mapping.csv workspace/out/nouns.json 4 workspace/plots/software_engineering/noun_frequency.svg
$ python nerFrequency.py workspace/out/ner.json workspace/plots/organization_frequency.svg workspace/plots/person_frequency.svg workspace/plots/person_cleaned_frequency.svg
$ python similarityBetweenCategories.py workspace/out/software_depth.csv workspace/out/software_engineering_depth.csv workspace/combined/4/mapping.csv workspace/out/nouns.json 4 workspace/plots/similarity_software-software_engineering.svg
```