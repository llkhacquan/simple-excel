# HOW TO BUILD

`./gradlew copyLib build`

# HOW TO RUN

From terminal:
- Use `stdin` as input:`bash run.sh`
- Use `file` as input `bash run.sh /path/to/file`

# NOTE
- Does not handle zero division error
- If a non-existing cell is required for computation, the application crashes (rather than use `0` value for non-existing cells)