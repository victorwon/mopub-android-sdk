#!/usr/bin/python
import os_helper

def git_current_branch():
    return os_helper.system_stdout('git rev-parse --abbrev-ref HEAD').rstrip()
