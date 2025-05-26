#!/bin/bash

# Running the original version
java TestOriginal > original_output.txt 2> original_error.txt

# Running the refactored version
java Test > refactored_output.txt 2> refactored_error.txt

# Comparing outputs
cat original_output.txt refactored_output.txt
