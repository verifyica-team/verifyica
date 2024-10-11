#!/bin/bash

#
# Copyright (C) 2024-present Verifyica project authors and contributors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Function to check exit code
function check_exit_code () {
  if [ ! $? ];
  then
    echo "------------------------------------------------------------------------"
    echo "${1}"
    echo "------------------------------------------------------------------------"
    exit 1
  fi
}

# Function to emit an error message and exit
function emit_error () {
  echo "------------------------------------------------------------------------"
  echo "${1}"
  echo "------------------------------------------------------------------------"
  exit 1;
}

echo "Needs to be refactored, exiting"

exit 0

# Usage
if [ "$#" -ne 1 ];
then
  echo "Usage: ${0} <version>"
  exit 1
fi

VERSION="${1}"
PROJECT_ROOT_DIRECTORY=$(git rev-parse --show-toplevel)
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)

cd "${PROJECT_ROOT_DIRECTORY}"
check_exit_code "Failed to change to project root directory"

# Check for uncommitted changes
git diff --quiet HEAD
if [ ! $? -eq 0 ];
then
  echo "------------------------------------------------------------------------"
  echo "UNCOMMITTED CHANGES"
  echo "------------------------------------------------------------------------"
  echo ""
  git status
  exit 1
fi

# Verify the code builds
./mvnw -s ~/.m2/verifyica.settings.xml -P release clean verify
check_exit_code "Maven build failed"

# Checkout a release branch
git checkout -b "release-${VERSION}"
check_exit_code "Git checkout [${VERSION}] failed"

# Update the build versions
./mvnw versions:set -DnewVersion="${VERSION}" -DprocessAllModules
check_exit_code "Maven update versions [${VERSION}] failed"
rm -Rf $(find . -name "*versionsBackup")

# Add changed files
git add -u
check_exit_code "Git add failed"

# Commit the changed files
git commit -m "${VERSION}"
check_exit_code "Git commit failed"

# Build and deploy
./mvnw -s ~/.m2/verifyica.settings.xml -P release clean deploy
check_exit_code "Maven deploy [${VERSION}] failed"

# Push the branch
git push --set-upstream origin release-"${VERSION}"
check_exit_code "Git push [${VERSION}] failed"

# Tag the version
git tag "${VERSION}"
check_exit_code "Git tag [${VERSION}] failed"

# Push the tag
git push origin "${VERSION}"
check_exit_code "Git tag [${VERSION}] push failed"

# Checkout the main branch
git checkout "${CURRENT_BRANCH}"
check_exit_code "Git checkout [${CURRENT_BRANCH}] failed"

echo "------------------------------------------------------------------------"
echo "SUCCESS"
echo "------------------------------------------------------------------------"