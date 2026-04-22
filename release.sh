#!/usr/bin/env bash

set -euo pipefail

readonly VERSION_PATTERN='^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9._-]+)?$'
readonly BASE_BRANCH='main'
readonly POST_SUFFIX='post'

usage() {
    echo "Usage: ./release.sh <version>" >&2
}

log() {
    echo "[INFO] $*"
}

fail() {
    echo "[ERROR] $*" >&2
    exit 1
}

require_clean_worktree() {
    local status

    status="$(git status --porcelain)"
    if [[ -n "${status}" ]]; then
        echo "[ERROR] Working tree is not clean" >&2
        echo "${status}" >&2
        exit 1
    fi
}

require_main_branch() {
    local branch

    branch="$(git branch --show-current)"
    if [[ "${branch}" != "${BASE_BRANCH}" ]]; then
        fail "Current branch is '${branch}', expected '${BASE_BRANCH}'"
    fi
}

detect_remote() {
    local remote

    remote="$(git config --get "branch.${BASE_BRANCH}.remote" || true)"
    if [[ -n "${remote}" ]]; then
        echo "${remote}"
        return
    fi

    echo "origin"
}

set_version() {
    local version="$1"

    ./mvnw -s ~/.m2/verifyica.settings.xml -B versions:set -DnewVersion="${version}" -DprocessAllModules -DgenerateBackupPoms=false
}

verify_release_build() {
    ./mvnw -s ~/.m2/verifyica.settings.xml -Prelease clean verify
}

deploy_release_build() {
    ./mvnw -s ~/.m2/verifyica.settings.xml -Prelease clean deploy
}

main() {
    local version post_version release_branch tag_name remote

    if [[ $# -ne 1 ]]; then
        usage
        exit 1
    fi

    version="$1"
    post_version="${version}-${POST_SUFFIX}"
    release_branch="release-${version}"
    tag_name="v${version}"

    [[ -f mvnw ]] || fail "mvnw not found in current directory"

    git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "Not a git repository"

    if [[ ! "${version}" =~ ${VERSION_PATTERN} ]]; then
        fail "Invalid version format '${version}'. Expected MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH-label"
    fi

    if [[ "${version}" == *"-${POST_SUFFIX}" ]]; then
        fail "Version must not end with '-${POST_SUFFIX}'"
    fi

    require_clean_worktree
    require_main_branch

    if git rev-parse -q --verify "refs/heads/${release_branch}" >/dev/null 2>&1; then
        fail "Release branch already exists: ${release_branch}"
    fi

    if git rev-parse -q --verify "refs/tags/${tag_name}" >/dev/null 2>&1; then
        fail "Tag already exists: ${tag_name}"
    fi

    remote="$(detect_remote)"

    if git ls-remote --exit-code --heads "${remote}" "refs/heads/${release_branch}" >/dev/null 2>&1; then
        fail "Remote release branch already exists: ${release_branch}"
    fi

    if git ls-remote --exit-code --tags "${remote}" "refs/tags/${tag_name}" >/dev/null 2>&1; then
        fail "Remote tag already exists: ${tag_name}"
    fi

    log "Validating baseline build"
    verify_release_build

    log "Creating release branch ${release_branch}"
    git checkout -b "${release_branch}"

    log "Setting release version ${version}"
    set_version "${version}"

    log "Validating release build"
    verify_release_build

    log "Deploying release build"
    deploy_release_build

    git add -u
    git commit -s -m "Release ${version}"

    log "Creating tag ${tag_name}"
    git tag -a "${tag_name}" -m "Release ${version}"

    log "Pushing branch ${release_branch}"
    git push "${remote}" "${release_branch}"

    log "Switching back to ${BASE_BRANCH}"
    git checkout "${BASE_BRANCH}"

    log "Setting post-release version ${post_version}"
    set_version "${post_version}"

    log "Validating post-release build"
    verify_release_build

    git add -u
    git commit -s -m "Prepare for development"

    log "Pushing ${BASE_BRANCH}"
    git push "${remote}" "${BASE_BRANCH}"

    log "Pushing tag ${tag_name}"
    git push "${remote}" "${tag_name}"
}

main "$@"
