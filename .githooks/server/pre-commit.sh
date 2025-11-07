#!/bin/bash

if ./gradlew ktlintMainSourceSetCheck; then
    echo
else
    exit 1
fi
