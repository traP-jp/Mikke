#!/bin/bash

if npx lint-staged; then
    echo
else
    exit 1
fi
