#!/bin/bash

# Check if sufficient arguments are provided
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <replacement_namespace>"
    exit 1
fi

# Read arguments
replacement_namespace="$1"

# Function to replace '' with the provided replacement text in all files under the specified directory
replace_text() {
    local path="$1"

    # Detect OS and set the appropriate sed command
    case "$(uname)" in
        "Darwin")  # macOS
            LC_ALL=C find "$path" -type f -exec sed -i '' "s/poly.web/${replacement_namespace}/g" {} +
            ;;
        "Linux")   # Linux
            LC_ALL=C find "$path" -type f -exec sed -i "s/poly.web/${replacement_namespace}/g" {} +
            ;;
        *)         # Unsupported OS
            echo "Unsupported operating system."
            exit 1
            ;;
    esac
}

echo "Replacing 'poly.web' with '$replacement_namespace' in all source files under the current directory..."

replace_text "./development"
replace_text "./bases"
replace_text "./components"
replace_text "./projects"
case "$(uname)" in
    "Darwin")  # macOS
        LC_ALL=C sed -i '' "s/poly.web/${replacement_namespace}/g" workspace.edn
        ;;
    "Linux")   # Linux
        LC_ALL=C sed -i "s/poly.web/${replacement_namespace}/g" workspace.edn
        ;;
    *)         # Unsupported OS
        echo "Unsupported operating system."
        exit 1
        ;;
esac

# awk -v new_text="$text" '{ gsub(/poly.web/, new_text); print > FILENAME }' workspace.edn \;

echo "Renaming 'poly' directories to '${replacement_namespace}'..."

find ./components -type d -name "poly" -exec sh -c 'mv "$0/web" "${0%/*}/'"${replacement_namespace}"'"' {} \;
find ./bases -type d -name "poly" -exec sh -c 'mv "$0/web" "${0%/*}/'"${replacement_namespace}"'"' {} \;
find ./projects -type d -name "poly" -exec sh -c 'mv "$0/web" "${0%/*}/'"${replacement_namespace}"'"' {} \;

echo "Done."
