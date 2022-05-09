# TReSa Search Engine Project

**A client - server application written in Java using JavaFX and lucene libraries that looks and works like the Google Search Engine**
--------------------------------------
# Notes
*An important step for the project to work is to run the client and server simultaneously.If either the client or the server hasnt been launched ,the search engine wont work and a error message will pop up.Moreover,make sure to add the articles in the server.In order to do that ,press the option button ,then the button add file or add folder.The server is responsible for the storing and indexing of the articles with the help of the libraries from Lucene.Look for the docs under the tab "Preresequisites" for more information.

*Add the articles you are interested for index only once.There is a collection of articles at the path Server_Parsing/Reuters.If you have edited a file and you wish to index it, first delete it from the server's "database".Otherwise, the server will skip the file to save time and space.

*Search the file you are interested for with phrases, single word or with boolean expressions.Searching specific fields(ex Title) is supported too.

*There is a similarity score for every article that has been found with the query that has been typed from the client.The more relative is the article ,the higher it will be in the search result.

*The default number of the articles that has been successfully found is 50.For more results, press the button options and then the button k - most hits.

*Features like search history and article comparison are added in order to extend the usability of the search engine and make it more interesting.

# Final Notes

*This project demonstrates all the skills and knowledge I have obtained combined with clean code principles(Thanks Uncle Bob),during all the years of my graduation at the university of Peloponnese.

**Prerequisites**

Lucene Version 8.0.0 : https://archive.apache.org/dist/lucene/java/8.0.0/

Same classes as 8.11.0

Luke : https://github.com/DmitryKey/luke/releases/tag/luke-swing-8.0.0

Run .sh file for luke. (Usage: View the index file)

Luke runs ONLY in 8.0.0

Lucene Core Documentation : https://lucene.apache.org/core/8_11_0/core/index.html

Lucene QueryParses Documentation : https://lucene.apache.org/core/8_11_0/queryparser/index.html
