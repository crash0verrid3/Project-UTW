#include <iostream>
#include <stdlib.h>
#include <string>

#define DEFAULT_EXE_FILE "JBrowser.jar"
#define DEFAULT_JAVA_PATH "java"

using namespace std;
string SplitFilename (const string str)
{
	std::size_t found = str.find_last_of("/\\");
	return str.substr(0,found);
}
int main(int argc, char *argv[]){
	string path = argv[0];
	string cmd;
	path = SplitFilename(path);
	string jpath = DEFAULT_JAVA_PATH;
	string jarfile = DEFAULT_EXE_FILE;
	for(int x=1; x<argc; x++){
		string c = argv[x];
		if(c.substr(0, ((string)"--java=").size()) == "--java="){
			jpath = c.substr(((string)"--java").size()+1);
		} else if(c.substr(0, ((string)("--run=")).size()) == "--run="){
			jarfile = c.substr(((string)("--run=")).size());
		} else if(c == "--help" || c == "-h" || c == "/?" || c == "-?"){
			cout << "Usage: " << argv[0] << " [--java=[java path]] [--run=[Jarfile path]]" << "\n";
			return 1;
		}
	}
	system((jpath + " -jar " + jarfile).c_str());
	return 0;
}
