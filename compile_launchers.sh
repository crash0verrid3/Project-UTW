#!/bin/bash
echo "Compiling..." &&
echo "<================>" &&
g++ easy_launcher.cpp -o runme_linux &&
echo "<================>" &&
i586-mingw32msvc-g++ -o launcher.exe easy_launcher.cpp &&
echo "<================>" &&
echo "Done." &&
exit 0
exit 1
