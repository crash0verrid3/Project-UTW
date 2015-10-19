#!/bin/bash
echo "Compiling launcher executables..."
echo "< Compiling for 64-bit linux ================>"
g++ -m64 easy_launcher.cpp -o launcher64_linux &&
echo "Succeeded." || echo "Failed."
echo "< Compiling for 32-bit linux ================>"
g++  -m32 easy_launcher.cpp -o launcher32_linux &&
echo "Succeeded." || echo "Failed."
echo "< Compiling for Windows (32-bit or 64-bit) ================>"
i586-mingw32msvc-g++ -o launcher.exe easy_launcher.cpp &&
echo "Succeeded." || echo "Failed."
echo "<================>"
echo "Done."
exit 0
