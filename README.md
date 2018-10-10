# SearchAndReplaceGenerator

 This repository provides the accompanying code of "Automatic Search-and-Replace from Examples with Coevolutionary Genetic Programming"

This project contains the source code of a tool for generating Search-and-Replace (SR) expressions:

1. automatically,
2. based only on examples of the desired behavior,
3. without any external hint about how the target search and replace should look like.

SR expressions are pairs of one search expression (regular expression) and one replace expression, they are used to automatize text manipulation tasks.

SearchAndReplaceGenerator was developed at the [Machine Learning Lab, University of Trieste, Italy](http://machinelearning.inginf.units.it).

We are preparing a wiki to provide installation walkthrough and a brief guide.

The provided engine is a developement-research release(1) that extends the work in our previous articles(2):

* Bartoli, De Lorenzo, Medvet, Tarlao, Inference of Regular Expressions for Text Extraction from Examples, IEEE Transactions on Knowledge and Data Engineering, 2016
* Bartoli, De Lorenzo, Medvet, Tarlao, Can a machine replace humans in building regular expressions? A case study, IEEE Intelligent Systems, 2016
* Bartoli, De Lorenzo, Medvet, Tarlao, Virgolin, Evolutionary Learning of Syntax Patterns for Genic Interaction Extraction, ACM Genetic and Evolutionary Computation Conference (GECCO), 2015, Madrid (Spain)

More details about the original project can be found on [Machine Learning Lab news pages](http://machinelearning.inginf.units.it/news/newregexgeneratortoolonline).

We hope that you find this code instructive and useful for your research or study activity.

If you use our code in your reasearch please cite our work and please share back your enhancements, fixes and 
modifications.

## Project Structure

The SearchAndReplaceGenerator project is organized in two NetBeans Java subprojects:

* MaleRegexTurtle:       provides the search(regular expression) and replace trees representation
* RandomSearchReplaceTurtle:     GP search engine 

## Other Links

Machine Learning Lab, [Twitter account](https://twitter.com/MaleLabTs)



---

(1) This is a developement release--very experimental for research purposes--, this is not intended to be a "production ready" release but is a tool for experimental assessment, 
for this reason the code may include unused classes and code documentation is partial.

(2) BibTeX format:

    @article{bartoli2016inference, 
	  author={A. Bartoli and A. De Lorenzo and E. Medvet and F. Tarlao}, 
	  journal={IEEE Transactions on Knowledge and Data Engineering}, 
	  title={Inference of Regular Expressions for Text Extraction from Examples}, 
	  year={2016}, 
	  volume={28}, 
	  number={5}, 
	  pages={1217-1230}, 
	  doi={10.1109/TKDE.2016.2515587}, 
	  ISSN={1041-4347}, 
	  month={May},
    }
    @inproceedings{bartoli2015evolutionary,
      title={Evolutionary Learning of Syntax Patterns for Genic Interaction Extraction},
      author={Bartoli, Alberto and De Lorenzo, Andrea and Medvet, Eric and
      Tarlao, Fabiano and Virgolin, Marco},
      booktitle={Proceedings of the 2015 on Genetic and Evolutionary Computation Conference},
      pages={1183--1190},
      year={2015},
      organization={ACM}
    }
    @article{bartoli2016can,
      title={Can a machine replace humans in building regular expressions? A case study},
      author={Bartoli, Alberto and De Lorenzo, Andrea and Medvet, Eric and Tarlao, Fabiano},
      journal={IEEE Intelligent Systems},
      volume={31},
      number={6},
      pages={15--21},
      year={2016},
      publisher={IEEE}
    }

