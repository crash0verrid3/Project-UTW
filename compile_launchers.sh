#!/bin/bash
echo "Compiling..." &&
echo "<================>" &&
g++ easy_launcher.cpp -o launcher_linux &&
echo "<================>" &&
i586-mingw32msvc-g++ -o launcher.exe easy_launcher.cpp &&
echo "<================>" &&
echo "Done - Success." &&
exit 0
echo "Failed."
exit 1
