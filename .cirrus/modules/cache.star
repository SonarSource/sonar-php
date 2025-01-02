def cleanup_orchestrator_cache():
  # language=bash
  return [
    "cd $ORCHESTRATOR_HOME",
    '''
find . -type f -name '*.zip' | while read -r file; do
filename=$(basename "$file")
prefix="${filename%-*}"

echo Checking file $file of size $(du -h $file | cut -f1)

# Find the latest file by creation date
latest_file=$(find . -type f -name "${prefix}-*.zip" -printf "%T@ %p\n" | sort -n | tail -n 1 | cut -d' ' -f2-)

# Remove all but the latest file
find . -type f -name "${prefix}-*.zip" ! -path "$latest_file" -exec sh -c "echo \\"Removing old file {} from orchestrator's cache\\" && rm {}" \\;
done
    '''
  ]
