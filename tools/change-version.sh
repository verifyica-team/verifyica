#!/bin/bash

#
# Copyright (C) 2024 The Verifyica project authors
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
  if [ ! $? -eq 0 ];
  then
    echo "------------------------------------------------------------------------"
    echo "${1}"
    echo "------------------------------------------------------------------------"
    exit 1
  fi
}

# Usage
if [ "$#" -ne 1 ];
then
  echo "Usage: ${0} <version>"
  exit 1
fi

NEW_VERSION="${1}"

./mvnw versions:set -DnewVersion="${NEW_VERSION}" -DprocessAllModules >> /dev/null
check_exit_code "Maven update versions [${NEW_VERSION}] failed"
rm -Rf $(find . -name "*versionsBackup")

echo "------------------------------------------------------------------------"
echo "SUCCESS"
echo "------------------------------------------------------------------------"