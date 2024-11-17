#!/bin/bash

# Remove various build and IDE directories
rm -rf .gradle
rm -rf build
rm -rf .idea
rm -rf *.iml
rm -rf node_modules
rm -rf .kotlin
rm -rf target

# Remove specific files
rm -f gradlew
rm -f LICENSE
rm -f package-lock.json
rm -f combined_files.txt

# Run Python script
python3 code.py

# Reset Git repository
git reset --hard

# Only run npm install if package.json exists
if [ -f "package.json" ]; then
    echo "package.json found, running npm install..."
    npm install
else
    echo "No package.json found, skipping npm install"
fi